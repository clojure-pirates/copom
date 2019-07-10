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
  (let [{neighborhood :superscription/neighborhood
         route :superscription/route} address]
    (and (:neighborhood/name neighborhood)
         (:route/name route)
         (:route/type route)
         (:superscription/city address)
         (:superscription/state address))))

;; Create superscription and rows for neighborhood and route join tables.
;; Returns superscription/id
(defn create-superscription! [params]
  (let [sup-params (dissoc params 
                           :superscription/neighborhood :superscription/route)        
        rid (get-in params [:superscription/route :route/id]) 
        nid (get-in params [:superscription/neighborhood :neighborhood/id]) 
        sup-id (q/create! {:table "superscription" :params sup-params})]
    (q/create! 
      {:table "superscription_neighborhood" 
       :params {:superscription-id sup-id, :neighborhood-id nid}}) 
    (q/create! {:table "superscription_route" 
                :params {:superscription-id sup-id, :route-id rid}})
    sup-id))

(defn create-or-update-superscription! [params]
  (prn params)
  (when (seq params)
    (if-let [id (:superscription/id params)]
      (do (q/update! {:table "superscription" :params params
                      :where ["id = ?" id]})
          id)
      (create-superscription! params))))

(defn create-returning! [table params]
  (when (seq params)
    (q/create! {:table table :params params})))
            
(defn create-address! [address]
  (prn 'create-address! address)
  (when (min-address-params? address)
    (let [route-params (select-keys (:superscription/route address) 
                                    [:route/id :route/name :route/type])
          neighborhood-params (select-keys (:superscription/neighborhood address) 
                                           [:neighborhood/id :neighborhood/name])
          superscription-params (select-keys address [:superscription/id
                                                      :superscription/num
                                                      :superscription/complement
                                                      :superscription/reference
                                                      :superscription/city
                                                      :superscription/state])
          rid (or (:route/id route-params) 
                  (create-returning! "route" route-params))
          nid (or (:neighborhood/id neighborhood-params)
                  (create-returning! "neighborhood" neighborhood-params))]
      (-> superscription-params
          (assoc-in [:superscription/neighborhood :neighborhood/id] nid)
          (assoc-in [:superscription/route :route/id] rid)
          create-or-update-superscription!))))

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
    (db/parser [{[:request/by-id (get-in req [:parameters :path :request/id])]
                 c/request-query}])))    
#_(get-request {:parameters {:path {:request/id 1}}})
              
; UPDATE
(defn update-request [req])

; DELETE
(defn delete-request [req])

(comment
  (db/parser [{:superscriptions/all
               c/superscription-query}]))