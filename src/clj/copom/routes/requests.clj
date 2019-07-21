(ns copom.routes.requests
  (:require
    [clojure.java.jdbc :as jdbc]
    [copom.db.core :as db]
    [copom.db.queries :as qu]
    [copom.db.queries.common :as q]
    [copom.db.queries.columns :as c]
    [ring.util.http-response :as response]))

(def request-core-keys 
  [:request/complaint :request/summary :request/event-timestamp
   :request/status :request/measures])

(defn request-entities [params]
  (let [ks [:request/requester :request/suspect :request/witness :request/victim]]
    ((apply juxt ks) params)))

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

(defn create-entity! [params]
  (when (min-entity-params? params)
    (let [;; Document fields will only be created if all the fields are filled.
          entity-doc-keys 
          (fn [{:entity/keys [doc-type doc-issuer doc-number]}]
            (when (and doc-type doc-issuer doc-number)
              [:entity/doc-type :entity/doc-issuer :entity/doc-number]))
          entity-keys (into (mapv (partial keyword "entity") 
                                  ["name" "father" "mother" "phone"])
                            (entity-doc-keys params))
          entity-params (select-keys params entity-keys)
          entity-id (q/create! {:table "entity" :params entity-params})]
      {(:entity/role params) entity-id})))

(defn create-request-entity-superscription! [req-ent-id ent-sup-params]
  (when-let [superscription-id (create-address! ent-sup-params)]
    (q/create! {:table "request_entity_superscription"
                :params {:request-entity-id req-ent-id
                         :superscription-id superscription-id}})))

(defn create-entities! [params]
  (let [[requester suspect witness victim :as entities-params] 
        (request-entities params)]
    ;; Return a map of the entities' roles with the respect ids.
    (reduce (fn [acc entity-params]
              (merge acc (create-entity! entity-params)))
            {} entities-params)))

(defn create-request-entity! [request-id entity-id]
  (q/create! {:table "request_entity"
              :params {:request-id request-id
                       :entity-id entity-id}}))

(defn create-request-entity-role! [req-ent-id role]
  (let [role-id (-> (db/parser [{[:request-role/by-role role]
                                 [:request-role/id]}]))]
    (q/create! {:table "request_entity_role"
                :params {::request-entity-id req-ent-id
                         ::role-id (:request-role/id role-id)}})))
;;; Delicts

(defn create-request-delicts! [request-id delicts-id]
  (doseq [id delicts-id]
    (q/create! {:table "request_delict"
                :params {:request-id request-id :delict-id id}})))

;;; Request

(defn min-request-params? [params]
  (and (:request/complaint params) (:request/summary params)
       (:request/status params)))

(defn create-request! [params]
  (if (min-request-params? params)
    (let [request-keys (->> ["complaint" "summary" "event-timestamp" "status" "measures"]
                            (mapv (partial keyword "request")))
          request-params (select-keys params request-keys)
          request-id (q/create! {:table "request" :params request-params})]
      request-id)
    (println "Required fields missing for REQUESTS!"))) 

(defn create-request-superscription! [rid sup-params]
  (when-let [sid (create-address! sup-params)]
    (q/create! {:table "request_superscription"
                :params {:request-id rid
                         :superscription-id sid}})))

(defn create-request-entities-relations! [rid entities params]
  (doseq [[role eid] entities]
    (let [reid (create-request-entity! rid eid)]
      (create-request-entity-role! reid role)
      (create-request-entity-superscription! 
         reid (get-in params [(keyword "request" role) 
                              :entity/superscription])))))

;;; DELETE

(defn delete-request-superscription! [params]
  (let [{rid :request/id
         sid :superscription/id} params
        where ["request_id = ? AND superscription_id = ?" rid sid]]
    (q/delete! {:table "request_superscription" :where where})))

(defn delete-request-entity-relations [rid eid roleid])
  ; delete request-entity
  ; dele
  

;;; Update

(defn update-request! [params]
  (q/update! {:table "request"
              :params (select-keys params request-core-keys)
              :where ["id = ?" (:request/id params)]}))

(defn update-request-superscription! [{rid :request/id :as params}]
  (let [{sid :superscription/id :as s1} 
        (get-in params [:request/superscription :superscription/id])]
    (cond sid
          (let [s2 (db/parser [{[:superscription/by-id sid]
                                c/superscription-query}])]
            (when (not= s1 s2)
              (delete-request-superscription! 
                {:request/id rid :superscription/id sid})
              (create-request-superscription! rid s1)))
          (seq s1)
          (create-request-superscription! rid s1))))

(defn find-entity [id role entities]
  (some #(and (= id (:entity/id %))
              (= role (get-in % [:entity/role :request-role/role]))
              %)
        entities))

(defn update-request-entities-relations! [{rid :request/id :as params}]
  (let [request-entities
        (db/parser [{[:request/by-id rid]
                     [{:request/entities
                       (conj c/entity-columns
                             {:entity/role c/request-role-columns})}]}])]
        
    (doseq [{eid :entity/id
             role :entity/role 
             :as e1} (request-entities params)]
      (cond eid
            (let [e2 (find-entity eid role request-entities)
                  reid (-> [{(list [:request-entity/by-request-id rid]
                                   {:filters [:= :request-entity/entity-id eid]})
                             [:request-entity/id]}]
                           db/parser :request-entity/id)] 
              (when (not= (select-keys e1 c/entity-columns)
                          (select-keys e2 c/entity-columns))
                ()))))))
                
              
                                                       
; -----------------------------------------------------------------------------
; Handlers

; CREATE

(defn create-request [{:keys [params]}]
  (response/ok
    (jdbc/with-db-transaction [conn db/*db*]
     (binding [db/*db* conn]
       (let [request-id (create-request! params)
             entities (create-entities! params)]
         (create-request-superscription! request-id (:request/superscription params))
         (create-request-entities-relations! request-id entities params)
         (create-request-delicts! request-id (:request/delicts params))
         {:request/id request-id})))))
              
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
(defn update-request [{:keys [params]}]
  (response/ok
    (jdbc/with-db-transaction [conn db/*db*]
     (binding [db/*db* conn]
       (update-request! params)
       (update-request-superscription! params)
       (update-request-entities-relations! params)))
       ; update-request-delicts!
    {:result :ok}))

; DELETE
(defn delete-request [req])

(defn delete-request-superscription [{:keys [params]}]
  (delete-request-superscription! params)
  (response/ok {:result :ok}))

(defn delete-request-entity-superscription [{:keys [params]}]
  (let [{rid :request/id, eid :entity/id, sid :superscription/id} params
        reid (-> (db/parser [{(list [:request-entity/by-request-id rid]
                                    {:filters [:and [:= :request-entity/entity-id 
                                                        eid]]})
                              [:request-entity/id]}])
                 first :request-entity/id)    
        where ["request_entity_id = ? AND superscription_id = ?" reid sid]]
    ; delete request_entity_superscription, reid, sid
    (q/delete! {:table "request_entity_superscription" :where where}) 
    (response/ok {:result :ok})))
  
(comment
  (db/parser [{:superscriptions/all
               c/superscription-query}]))