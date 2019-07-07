(ns copom.routes.requests
  (:require
    [clojure.java.jdbc :as jdbc]
    [copom.db.core :as db]
    [copom.db.queries :as qu]
    [copom.db.queries.common :as q]
    [copom.db.queries.columns :as c]
    [ring.util.http-response :as response]))

;;; Address

(defn min-address-params? [address]
  (and (:neighborhood/name address)
       (:route/name address)
       (:route/type address)
       (:superscription/city address)
       (:superscription/state address)))

;; Create superscription and rows for neighborhood and route join tables.
(defn create-superscription! [params]
  (prn "create-superscription!" params)
  (let [sup-params (dissoc params :neighborhood/id :route/id)
        sup-id (q/create! {:table "superscription" :params sup-params})]
    (prn 'superscription sup-id 'params params)
    (q/create! {:table "superscription_neighborhood" 
                :params {:superscription-id sup-id 
                         :neighborhood-id (:neighborhood/id params)}})
    (q/create! {:table "superscription_route" 
                :params {:superscription-id sup-id
                         :route-id (:route/id params)}})
    (q/by-id {:table "superscription"
              :superscription/id sup-id})))

;; For address entities only.
(defn get-update-create! [table params]
  (prn "get-update-create!" table params)
  (cond ;; No params? return nil
        (empty? params) nil
        ;; id present? update its fields.
        ((keyword table "id") params)
        (do (q/update! {:table table
                        :params params 
                        :where ["id = ?" ((keyword table "id") params)]})
            params)
        ;; No id? create a new row.
        :else (if (= "superscription" table) 
                (create-superscription! params)
                (q/by-id
                  {:table table
                   (keyword table "id") (q/create! {:table table :params params})}))))                

(defn create-address! [address]
  (when (min-address-params? address)
    (prn) (prn "create-address!" address)
    (let [route-params (select-keys address [:route/id :route/name :route/type])
          neighborhood-params (select-keys address [:neighborhood/id :neighborhood/name])
          superscription-params (select-keys address [:superscription/id
                                                      :superscription/num
                                                      :superscription/complement
                                                      :superscription/reference
                                                      :superscription/city
                                                      :superscription/state])
          route (get-update-create! "route" route-params)
          neighborhood (get-update-create! "neighborhood" neighborhood-params)
          superscription (get-update-create! "superscription" 
                                             (assoc superscription-params
                                                :neighborhood/id (:neighborhood/id neighborhood)
                                                :route/id (:route/id route)))]
      (:superscription/id superscription))))

;;; Entity

(defn min-entity-params? [params]
  (let [{:entity/keys [name phone superscription]} params]
    (or name phone (min-address-params? superscription))))

;; Document fields will only be created if all the fields are filled.
(defn entity-doc-keys [params]
  (let [{:entity/keys [doc-type doc-issuer doc-number]} params]
    (when (and doc-type doc-issuer doc-number)
      [:entity/doc-type :entity/doc-issuer :entity/doc-number])))

(defn create-entity! [params]
  (println "create-entity!" params)
  (when (min-entity-params? params)
    (let [superscription-id (create-address! (:entity/superscription params))
          doc-keys (entity-doc-keys params)
          entity-keys (into (mapv (partial keyword "entity") 
                                  ["name" "father" "mother" "phone"])
                            doc-keys)
          entity-params (select-keys params entity-keys)
          entity-id (q/create! {:table "entity" :params entity-params})]
      (when superscription-id
        (q/create! {:table "entity_superscription" 
                    :params {:entity-id entity-id :superscription-id superscription-id}}))
      {(:entity/role params) entity-id})))

(defn create-entities! [params]
  (let [entities [:request/requester :request/suspect :request/witness :request/victim]
        [requester suspect witness victim :as entities-params] ((apply juxt entities) params)]
    ;; Return a map of the entities' roles with the respect ids.
    (reduce (fn [acc entity-params]
              (merge acc (create-entity! entity-params)))
            {} entities-params)))

(defn create-request-entities-role! [request-id entities-role+id]
  (doseq [[role id] entities-role+id]
    (let [role (-> (db/parser [{[:request-role/by-role role]
                                [:request-role/id]}]))
          request-entity-id (q/create! {:table "request_entity"
                                        :params {::request-id request-id
                                                 ::entity-id id}})]
      (prn 'role role 'request-entity-id request-entity-id)
      (q/create! {:table "request_entity_role"
                  :params {::request-entity-id request-entity-id
                           ::role-id (:request-role/id role)}}))))

;;; Delicts

(defn create-request-delicts! [request-id delicts-id]
  (doseq [id delicts-id]
    (q/create! {:table "request_delict"
                :params {:request-id request-id :delict-id id}})))

;;; Request

(defn min-request-params? [params]
  (and (:request/complaint params) (:request/summary params)
       (:request/status params)))

(defn create-request! [params entities-role+id]
  (if (min-request-params? params)
    (let [request-keys (->> ["complaint" "summary" "event-timestamp" "status" "measures"]
                            (mapv (partial keyword "request")))
          superscription-id (create-address! (:request/superscription params))
          request-params (select-keys params request-keys)
          request-id (q/create! {:table "request" :params request-params})]
      (when superscription-id
        (q/create! {:table "request_superscription"
                    :params {:request-id request-id :superscription-id superscription-id}}))
      (create-request-entities-role! request-id entities-role+id)
      (create-request-delicts! request-id (:request/delicts params))
      request-id)
    (println "Required fields missing for REQUESTS!"))) 
          
; -----------------------------------------------------------------------------
; Handlers

; CREATE
(defn create-request [req]
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
     (let [entities-role+id (create-entities! (:params req))
           request-id (create-request! (:params req) entities-role+id)]
       (response/ok {:request/id request-id})))))
              
; READ
(defn get-requests [req]
  (response/ok
    (db/parser
     [{:requests/all
       c/request-query}])))
#_(get-requests nil)

(defn get-request [req]
  (response/ok
    (db/parser [{[:request/by-id (get-in req [:params :request/id])]
                 c/request-query}])))    
#_(get-request {:params {:request/id 1}})
              
; UPDATE
(defn update-request [req])

; DELETE
(defn delete-request [req])
