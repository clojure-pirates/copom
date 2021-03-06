(ns copom.routes.requests
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as string]
    [copom.db.core :as db]
    [copom.db.queries :as qu]
    [copom.db.queries.common :as q]
    [copom.db.queries.columns :as c]
    [copom.routes.entity :refer [create-ent-sup! create-entity!]]
    [copom.routes.superscription :refer [create-sup!]]
    [copom.utils :refer [m->upper-case str->java-date]]
    [ring.util.http-response :as response]))
 
(defn get-reid 
  "Takes a request/id and entity/id and returns a request-entity/id"
  [rid eid]
  (-> (db/parser [{(list [:request-entity/by-request-id rid]
                         {:filters [:and [:= :request-entity/entity-id 
                                             eid]]})
                   [:request-entity/id]}])
      first :request-entity/id)) 

(defn get-request-delicts [rid]
  (-> (db/parser [{[:request/by-id rid]
                   [{:request/delicts [:request-delict/request-id
                                       :request-delict/delict-id]}]}])
      :request/delicts))                    

(def req-core-keys 
  [:request/complaint :request/summary :request/event-timestamp
   :request/status :request/measures])

(defn req-ents [params]
  (let [ks [:request/requester :request/suspect 
            :request/witness :request/victim]
        f (apply juxt ks)]
    (->> (f params)
         (remove nil?))))
            
;;; Entity

(defn create-req-ent-sup! [reid sid]
  (q/create! {:table "request_entity_superscription"
              :params {:request-entity-id reid
                       :superscription-id sid}}))

(defn create-req-ent! [request-id entity-id]
  (q/create! {:table "request_entity"
              :params {:request-id request-id
                       :entity-id entity-id}}))

(defn create-req-ent-role! [req-ent-id role]
  (let [role-map (if (map? role)
                    role
                    (db/parser [{[:request-role/by-role role]
                                 [:request-role/id]}]))]
    (q/create! {:table "request_entity_role"
                :params {::request-entity-id req-ent-id
                         ::role-id (:request-role/id role-map)}})))
;;; Delicts

(defn create-req-delicts! [rid did]
  (doseq [id did]
    (q/create! {:table "request_delict"
                :params {:request-id rid :delict-id id}})))

;;; Request

(defn min-request-params? [params]
  (and (:request/complaint params)
       (:request/status params)))

(defn create-request! [params]
  (if (min-request-params? params)
    (let [request-keys (->> ["complaint" "summary" "event-timestamp" "status" "measures"]
                            (mapv (partial keyword "request")))
          request-params (-> params
                             (select-keys request-keys)
                             (m->upper-case [:request/complaint :request/summary
                                             :request/measures])
                             (assoc :request/event-timestamp 
                              (str->java-date 
                                :date-hour-minute-second
                                (:request/event-timestamp params))))
          request-id (q/create! {:table "request" :params request-params})]
      request-id)
    (println "Required fields missing for REQUESTS!"))) 

(defn create-req-sup! [rid sid]
  (q/create! {:table "request_superscription"
              :params {:request-id rid
                       :superscription-id sid}}))


(defn create-request-superscription! [rid sparams]
  (when-let [sid (create-sup! sparams)]
    (q/create! {:table "request_superscription"
                :params {:request-id rid
                         :superscription-id sid}})
    sid))

(defn create-req-ent-relations! 
  "Creates the relations request-entity, request-entity-role, and 
  request-entity-superscription, for each entity."
  [rid entities]
  (doseq [e entities]
    (when-let [eid (:entity/id e)]
      (let [reid (create-req-ent! rid (:entity/id e))]
        (create-req-ent-role! reid (:entity/role e))
        (when-let [sid (get-in e [:entity/superscription :superscription/id])]
          (create-req-ent-sup! reid sid))))))

;;; DELETE

(defn delete-req-sup! [params]
  (let [{rid :request/id
         sid :superscription/id} params]
    (q/delete! {:table "request_superscription" 
                :where ["request_id = ? AND superscription_id = ?" rid sid]})))

(defn delete-request-entity! [{rid :request/id eid :entity/id}]
  (q/delete! {:table "request_entity"
              :where ["request_id = ? AND entity_id = ? " rid eid]}))

(defn delete-entity! [eid]
  (q/delete! {:table "entity" :where ["id = ?" eid]}))  

;;; Update

(defn update-request! [params]
  (q/update! {:table "request"
              ;; we don't want :event-timestamp (date) to be cast to string
              :params (-> (select-keys params req-core-keys)
                          m->upper-case
                          (assoc :request/event-timestamp 
                            (str->java-date 
                              :date-hour-minute-second
                              (:request/event-timestamp params))))
              :where ["id = ?" (:request/id params)]}))

(defn delete-request-delict! [rid did]
  (q/delete! {:table "request_delict"
              :where ["request_id = ? AND delict_id = ?" rid did]}))

(defn update-request-delicts! [{rid :request/id delicts :request/delicts}]
  (let [delicts1 (set delicts)
        delicts2 (->> (get-request-delicts rid)
                      (map :request-delict/delict-id)
                      set)]
    (when (not= delicts1 delicts2)
      (let [new (clojure.set/difference delicts1 delicts2)
            delete (clojure.set/difference delicts2 delicts1)]
        (create-req-delicts! rid new)
        (doseq [d delete]
          (delete-request-delict! rid d))))))

; -----------------------------------------------------------------------------
; Request Handlers

; CREATE

(defn create-request [{:keys [params]}]
  (if-not (min-request-params? params)
    (response/bad-request "Missing required param.")
    (jdbc/with-db-transaction [conn db/*db*]
     (binding [db/*db* conn]
       (let [rid (create-request! params)
             sid (get-in params [:request/superscription :superscription/id])]
         (when sid                        
           (create-req-sup! rid sid))
         (create-req-ent-relations! rid (req-ents params))
         (create-req-delicts! rid (:request/delicts params))
         (response/ok
          {:request/id rid}))))))

(defn create-request-entity [{:keys [parameters params]}]
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
     (create-req-ent-relations! 
       (get-in parameters [:path :request/id]) [params])
     (response/ok
       {:entity/id (get-in parameters [:path :entity/id])}))))

;; Same as `create-request-entity`, but creates an entity first, returning
;; its id.
(defn create-request-entity* [{:keys [parameters params]}]
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
     (let [eid (create-entity! params)]
       (create-req-ent-relations! (get-in parameters [:path :request/id])
                                  [(assoc params :entity/id eid)])
       (response/ok
         {:entity/id eid})))))


(defn create-request-entity-superscription [{:keys [parameters params]}]
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
     (let [rid (get-in parameters [:path :request/id])
           eid (get-in parameters [:path :entity/id])
           reid (get-reid rid eid)
           sid (create-sup! params)]
       (create-ent-sup! eid sid)
       (create-req-ent-sup! reid sid)
       (response/ok 
         {:superscription/id sid})))))


(defn create-request-superscription [{:keys [parameters params]}]
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
     (response/ok
       {:superscription/id
        (create-request-superscription! 
          (get-in parameters [:path :request/id]) params)}))))
       
; READ
(defn get-requests [req]
  (response/ok
    (db/parser
     [{(list :requests/all
             {:order-by [:request/event-timestamp :asc]})
       c/request-query}])))
#_(get-requests nil)

(defn get-request [req]
  (response/ok
    (db/parser [{[:request/by-id (get-in req [:parameters :path :request/id])]
                 c/request-query}])))    
#_(get-request {:parameters {:path {:request/id 1}}})  

(defn get-complaints [{{query :query} :params}]
  (response/ok
    (->>
      (db/parser
       [{(list :requests/all
               {:distinct [:request/complaint]
                :filters (when query [:like :request/complaint (str "%" query "%")])
                :limit 10})
         [:request/complaint]}])
      (map :request/complaint)
      (into #{}))))

; UPDATE
(defn update-request [{:keys [params]}]
  (response/ok
    (jdbc/with-db-transaction [conn db/*db*]
     (binding [db/*db* conn]
       (update-request! params)
       (update-request-delicts! params)
       {:result :ok}))))

; DELETE
(defn delete-request-entity [{:keys [parameters]}]
  (delete-request-entity! (:path parameters))
  (response/ok
    {:result :ok}))

(defn delete-request-superscription [{:keys [parameters]}]
  (delete-req-sup! (:path parameters))
  (response/ok {:result :ok}))


(defn delete-request-entity-superscription 
  [{{{rid :request/id eid :entity/id sid :superscription/id} :path} :parameters}]
  (let [reid (get-reid rid eid)
        where ["request_entity_id = ? AND superscription_id = ?" reid sid]]
    ; delete request_entity_superscription, reid, sid
    (q/delete! {:table "request_entity_superscription" :where where}) 
    (response/ok {:result :ok})))
  
(comment
  (db/parser [{:entities/all
               (conj c/entity-columns
                     {:entity/superscriptions c/superscription-query})}]
    (def r
      (first
        (db/parser
         [{:requests/all
           c/request-query}])))
    (->> (:request/summary r)
         .getCharacterStream
         line-seq
         (clojure.string/join "\n")))

  (clojure.repl/doc sort-by)
  (sort-by :a > [{:a 1} {:a 2} {:a 9}])

  (->
    (io/input-stream (java.io.ByteArrayInputStream. (.getBytes "text")))
    slurp))
