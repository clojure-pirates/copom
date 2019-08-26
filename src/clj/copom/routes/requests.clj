(ns copom.routes.requests
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as string]
    [copom.db.core :as db]
    [copom.db.queries :as qu]
    [copom.db.queries.common :as q]
    [copom.db.queries.columns :as c]
    [ring.util.http-response :as response]))

(defn m->upper-case 
  ([m] (m->upper-case m (keys m)))
  ([m ks]
   (reduce (fn [acc k]
             (if-let [v (get acc k)]
               (assoc acc k (string/upper-case v))
               acc))
           m ks)))
#_(m->upper-case {:a "a" :ab "ab" :c nil})

(defn get-req-ents-by-rid [rid]
  (db/parser [{[:request/by-id rid]
               [{:request/entities
                 (conj c/entity-columns
                       {:entity/role c/request-role-columns})}]}]))
 
(defn get-reid 
  "Takes a request/id and entity/id and returns a request-entity/id"
  [rid eid]
  (-> (db/parser [{(list [:request-entity/by-request-id rid]
                         {:filters [:and [:= :request-entity/entity-id 
                                             eid]]})
                   [:request-entity/id]}])
      first :request-entity/id)) 


(def req-core-keys 
  [:request/complaint :request/summary :request/event-timestamp
   :request/status :request/measures])

(defn req-ents [params]
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
  (let [sup-params (-> params
                       (dissoc  
                           :superscription/neighborhood :superscription/route)
                       (m->upper-case [:superscription/num :superscription/complement 
                                       :superscription/reference
                                       :superscription/city :superscription/state]))
        rid (get-in params [:superscription/route :route/id]) 
        nid (get-in params [:superscription/neighborhood :neighborhood/id]) 
        sid (q/create! {:table "superscription" :params sup-params})]
    (q/create! 
      {:table "superscription_neighborhood" 
       :params {:superscription-id sid, :neighborhood-id nid}}) 
    (q/create! {:table "superscription_route" 
                :params {:superscription-id sid, :route-id rid}})
    sid))

(defn create-or-update-sup! [params]
  (when (seq params)
    (if-let [sid (:superscription/id params)]
      (do (q/update! {:table "superscription" :params params
                      :where ["id = ?" sid]})
          sid)
      (create-superscription! params))))

(defn create-returning! [table params]
  (when (seq params)
    (q/create! {:table table :params params})))
            
(defn create-address! [address]
  (when (min-address-params? address)
    (let [rparams (select-keys (:superscription/route address) 
                               [:route/id :route/name :route/type])
          nparams (select-keys (:superscription/neighborhood address) 
                               [:neighborhood/id :neighborhood/name])
          sparams (select-keys address [:superscription/id
                                        :superscription/num
                                        :superscription/complement
                                        :superscription/reference
                                        :superscription/city
                                        :superscription/state])
          rid (or (:route/id rparams) 
                  (create-returning! "route" (m->upper-case rparams)))
          nid (or (:neighborhood/id nparams)
                  (create-returning! "neighborhood" (m->upper-case nparams)))]
      (-> sparams
          (assoc-in [:superscription/neighborhood :neighborhood/id] nid)
          (assoc-in [:superscription/route :route/id] rid)
          create-or-update-sup!))))

;;; Entity

(defn min-entity-params? 
  [{:entity/keys [name phone superscription] :as params}]
  (or name phone 
      (min-address-params? superscription)))

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
          entity-params (-> params 
                            (select-keys entity-keys)
                            (m->upper-case [:entity/name :entity/doc-type 
                                            :entity/doc-issuer :entity/doc-number
                                            :entity/father :entity/mother]))
          entity-id (q/create! {:table "entity" :params entity-params})]
      {(:entity/role params) entity-id})))

(defn create-req-ent-sup! [reid sid]
  (q/create! {:table "request_entity_superscription"
              :params {:request-entity-id reid
                       :superscription-id sid}}))

(defn create-ents! [params]
  (let [[requester suspect witness victim :as entities-params] 
        (req-ents params)]
    ;; Return a map of the entities' roles with the respect ids.
    (reduce (fn [acc entity-params]
              (merge acc (create-entity! entity-params)))
            {} entities-params)))

(defn create-req-ent! [request-id entity-id]
  (q/create! {:table "request_entity"
              :params {:request-id request-id
                       :entity-id entity-id}}))

(defn create-req-ent-role! [req-ent-id role]
  (let [role-id (-> (db/parser [{[:request-role/by-role role]
                                 [:request-role/id]}]))]
    (q/create! {:table "request_entity_role"
                :params {::request-entity-id req-ent-id
                         ::role-id (:request-role/id role-id)}})))
;;; Delicts

(defn create-req-delicts! [request-id delicts-id]
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
          request-params (-> params
                             (select-keys request-keys)
                             (m->upper-case [:request/complaint :request/summary
                                             :request/measures]))
          request-id (q/create! {:table "request" :params request-params})]
      request-id)
    (println "Required fields missing for REQUESTS!"))) 

(defn create-req-sup! [rid sup-params]
  (when-let [sid (create-address! sup-params)]
    (q/create! {:table "request_superscription"
                :params {:request-id rid
                         :superscription-id sid}})
    sid))

(defn create-request-superscription! [rid sparams]
  (when-let [sid (create-sup! sparams)]
    (q/create! {:table "request_superscription"
                :params {:request-id rid
                         :superscription-id sid}})
    sid))

(defn create-ent-sup! [eid sid]
  (q/create! {:table "entity_superscription"
              :params {:entity-id eid
                       :superscription-id sid}}))

(defn create-req-ent-relations! [rid ents params]
  (doseq [[role eid] ents]
    (let [reid (create-req-ent! rid eid)
          rr (keyword "request" role)
          ent-sup-params (get-in params [rr :entity/superscription])] 
      (create-req-ent-role! reid role)
      (when-let [sid (create-address! ent-sup-params)]
        (create-ent-sup! eid sid)
        (create-req-ent-sup! reid sid)))))

;;; DELETE

(defn delete-req-sup! [params]
  (let [{rid :request/id
         sid :superscription/id} params]
    (q/delete! {:table "request_superscription" 
                :where ["request_id = ? AND superscription_id = ?" rid sid]})))

(defn delete-entity! [eid]
  (q/delete! {:table "entity" :where ["id = ?" eid]}))  

;;; Update

(defn update-request! [params]
  (q/update! {:table "request"
              :params (select-keys params req-core-keys)
              :where ["id = ?" (:request/id params)]}))

(defn update-req-sup! [{rid :request/id :as params}]
  (let [{sid :superscription/id :as s1} 
        (get-in params [:request/superscription])]
    (cond sid
          (let [s2 (db/parser [{[:superscription/by-id sid]
                                c/superscription-query}])]
            (when (not= s1 s2)
              (delete-req-sup! 
                {:request/id rid :superscription/id sid})
              (create-req-sup! rid)))
          (seq s1)
          (create-req-sup! rid s1))))

(defn find-entity [id role entities]
  (some #(and (= id (:entity/id %))
              (= role (get-in % [:entity/role :request-role/role]))
              %)
        entities))  

(defn update-request-entities-relations! [{rid :request/id :as params}]
  (let [request-entities (get-req-ents-by-rid rid)]        
    (doseq [{eid :entity/id
             role :entity/role 
             :as e1} (req-ents params)]
      (cond eid
            (let [e2 (find-entity eid role)
                  reid (get-reid rid eid)] 
              (when (not= (select-keys e1 c/entity-columns)
                          (select-keys e2 c/entity-columns))
                (delete-entity! eid)
                (create-req-ent-relations!
                  rid
                  (create-entity! e1)
                  params)))))))
              
    
; -----------------------------------------------------------------------------
; Entity Handlers

(def cols
  (conj c/entity-columns
        {:entity/superscriptions c/superscription-query}))

(defn get-names [{{query :query} :params}]
  (response/ok
    (db/parser
      [{(list :entities/all
              {:filters (when query 
                          [:like :entity/name (str "%" query "%")])})
        cols}])))
         
(defn get-phones [{{query :query} :params}]
  (response/ok
    (db/parser
      [{(list :entities/all
              {:filters (when query 
                          [:like :entity/phone (str "%" query "%")])})
        cols}])))

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

(defn create-entity [{:keys [params]}]
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
    (let [[role eid :as e] (first (create-entity! params))
          sid (create-address! (:entity/superscription params))]
      (create-ent-sup! eid sid))))
  (response/ok
    {:result :ok}))

(defn min-superscription-params? [params]
  (and (:neighborhood/id params)
       (:route/id params)
       (:superscription/city params)
       (:superscription/state params)))

(defn create-sup! [params]
  (when (min-superscription-params? params)
    (let [sup-params (-> params
                         (dissoc :neighborhood/id :route/id)
                         m->upper-case)
          sid (q/create! {:table "superscription" :params sup-params})]
      (q/create! {:table "superscription_neighborhood"
                  :params {:superscription-id sid
                           :neighborhood-id (:neighborhood/id params)}})
      (q/create! {:table "superscription_route"
                  :params {:superscription-id sid
                           :route-id (:route/id params)}})
      sid)))

(defn create-entity-superscription [{:keys [params path-params]}]
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
     (let [sid (create-sup! params)]
       (create-ent-sup! (:entity/id path-params) sid)
       (response/ok 
         {:superscription/id sid})))))

(defn delete-entity-superscription 
  [{:keys [path-params]}]
  (let [{eid :entity/id sid :superscription/id} path-params]
    (q/delete! {:table "entity_superscription"
                :where ["entity_id = ? AND superscription_id = ?" eid sid]})))

; -----------------------------------------------------------------------------
; Neighborhood Handlers

(defn get-neighborhoods [{{query :query} :params}]
  (response/ok
    (db/parser
      [{(list :neighborhoods/all
              {:filters (when query [:like :neighborhood/name (str "%" query "%")])
               :limit 10})
        c/neighborhood-columns}])))

(defn create-neighborhood [{:keys [params]}]
  (q/create! {:table "neighborhood"
              :params (m->upper-case params)})
  (response/ok {:result :ok}))

; -----------------------------------------------------------------------------
; Route Handlers

(defn get-routes [{{query :query} :params}]
  (response/ok
    (db/parser
      [{(list :routes/all
              {:filters (when query [:like :route/name (str "%" query "%")])
               :limit 10})
        c/route-columns}])))

(defn create-route [{:keys [params]}]
  (q/create! {:table "route"
              :params (m->upper-case params)})
  (response/ok {:result :ok}))


; -----------------------------------------------------------------------------
; Request Handlers

; CREATE

(defn create-request [{:keys [params]}]
  (response/ok
    (jdbc/with-db-transaction [conn db/*db*]
     (binding [db/*db* conn]
       (let [request-id (create-request! params)
             entities (create-ents! params)]
         (create-req-sup! request-id (:request/superscription params))
         (create-req-ent-relations! request-id entities params)
         (create-req-delicts! request-id (:request/delicts params))
         {:request/id request-id})))))

(defn create-request-entity-superscription [{:keys [path-params params]}]
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
     (let [reid (get-reid (:request/id path-params) (:entity/id path-params))
           sid (create-sup! params)]
       (create-ent-sup! (:entity/id path-params) sid)
       (create-req-ent-sup! reid sid)
       (response/ok 
         {:superscription/id sid})))))

(defn create-request-superscription [{:keys [path-params params]}]
  (prn path-params)
  (clojure.pprint/pprint params)
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
     (response/ok
       {:superscription/id
        (create-request-superscription! (:request/id path-params) params)}))))
       
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
       (update-req-sup! params)
       (update-request-entities-relations! params)
       ; update-request-delicts!
       {:result :ok}))))

; DELETE
(defn delete-request [req])

(defn delete-request-superscription [{:keys [path-params]}]
  (delete-req-sup! path-params)
  (response/ok {:result :ok}))


(defn delete-request-entity-superscription [{:keys [path-params]}]
  (let [{rid :request/id, eid :entity/id, sid :superscription/id} path-params
        reid (get-reid rid eid)
        where ["request_entity_id = ? AND superscription_id = ?" reid sid]]
    ; delete request_entity_superscription, reid, sid
    (q/delete! {:table "request_entity_superscription" :where where}) 
    (response/ok {:result :ok})))
  
(comment
  (db/parser [{:entities/all
               (conj c/entity-columns
                     {:entity/superscriptions c/superscription-query})}])
  
  (db/parser [{:superscriptions/all
               c/superscription-query}])
  
  (db/parser [{:requests/all
               [:request/id
                {:request/entities
                 [:entity/id
                  {:entity/superscriptions [:superscription/id]}]}]}]))
                ;{:request/entities c/entity-query}]}]))
