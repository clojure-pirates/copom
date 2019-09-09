(ns copom.events.request
  (:require
    [ajax.core :as ajax]
    [copom.db :refer [app-db]]
    [copom.events.utils :refer [base-interceptors superscription-coercions]]
    [re-frame.core :as rf]))

;; -------------------------
;; Helpers

(def requests-interceptos 
  (conj base-interceptors (rf/path :requests)))

(defn event-timestamp [date time]
  (str (-> date (.split "T") first) " "
       time))

(defn request-coercions [m]
  (-> m
      (dissoc :request/date :request/time)
      (update :request/delicts #(->> % (filter (fn [[k v]] v)) (map (fn [[k v]] k))))
      (assoc :request/event-timestamp (event-timestamp (:request/date m) (:request/time m)))))
  

(rf/reg-event-db
  :requests/clear-form
  requests-interceptos
  (fn [_ [doc]]
    (reset! doc nil)))

(def required-request-fields
  #{:request/complaint :request/status})

(def field-translation
  {:request/complaint "natureza"
   :request/summary "resumo da requisição"
   :request/status "status"})

(defn min-request-params? [params]
  (let [[complaint status] ((apply juxt required-request-fields) params)]
    (and complaint status)))

(defn missing-fields [fields required-fields]
  (clojure.set/difference required-fields (set (keys fields))))

(defn request-error-msg [fields]
  (str
    (->> (missing-fields fields required-request-fields)
         (select-keys field-translation)
         vals
         (clojure.string/join ", ")
         (str "Campos obrigatórios: "))
    "."))

(defn- request-entities-edit-mode 
  "Group :request/entities by their role."
  [req-ents]
  (-> (group-by #(get-in % [:entity/role :request-role/role])
                req-ents)
      (clojure.set/rename-keys {"requester" :request/requester
                                "victim"    :request/victim
                                "suspect"   :request/suspect
                                "witness"   :request/witness})))
  
(defn edit-mode [req]
  (let [req-entities (-> req :request/entities request-entities-edit-mode)
        req-delicts
        (reduce (fn [m d]
                  (assoc m (:delict/id d) true))
                {} (:request/delicts req))
        [d t] (when-let [s (:request/event-timestamp req)]
                (clojure.string/split s #" "))
        req-datetime #:request{:date d :time t}]
    (-> req
        (assoc :request/delicts req-delicts)
        (merge req-datetime
               req-entities))))
           

;; -------------------------
;; Handlers

(rf/reg-event-fx
  :clear-form
  base-interceptors
  (fn [_ [{:keys [doc path]}]]
    (swap! doc assoc-in path nil)
    nil))

(defn create-request! [doc]
  (ajax/POST "/api/requests"
             {:params (request-coercions @doc)
              :handler #(do 
                            (rf/dispatch [:navigate/by-path "/#/"])
                            (rf/dispatch [:requests/clear-form doc]))})
  nil)

(rf/reg-event-fx
  :requests/create
  base-interceptors
  (fn [{:keys [db]} [doc]]
    (if (min-request-params? @doc)
      (create-request! doc)
      {:db (assoc-in db [:requests :new :request/errors]
                     (request-error-msg @doc))})))
        
(rf/reg-event-fx
  :requests/load-delicts
  base-interceptors
  (fn [_ _]
    (ajax/GET "/api/delicts"
              {:handler #(rf/dispatch [:rff/set [:delicts/all] %])
               :response-format :json
               :keywords? true})
    nil))

(rf/reg-event-fx
  :requests/load-request
  base-interceptors
  (fn [_ [id]]
    (ajax/GET (str "/api/requests/" id)
              {:handler #(let [r (edit-mode %)]
                           (swap! app-db assoc :request r)
                           (rf/dispatch-sync [:rff/set [:requests/request] r])
                           (rf/dispatch-sync [:rff/set [:requests/edit] r]))
               :response-format :json
               :keywords? true})
    nil))

#_
(rf/reg-event-fx
  :requests/load-request
  base-interceptors
  (fn [_ [id]]
    (ajax/GET (str "/api/requests/" id)
              {:handler #(let [r (edit-mode %)]
                           (rf/dispatch-sync [:rff/set [:requests/request] r])
                           (rf/dispatch-sync [:rff/set [:requests/edit] r]))
               :error-handler #(prn %)
               :response-format :json
               :keywords? true})
    nil))

(rf/reg-event-fx
  :requests/load-requests
  base-interceptors
  (fn [_ _]
    (ajax/GET "/api/requests"
              {:handler #(rf/dispatch [:rff/set [:requests/all] %])
               :error-handler #(prn %)
               :response-format :json
               :keywords? true})
    nil))

(rf/reg-event-fx
  :requests/update
  base-interceptors
  (fn [_ [doc]]
    (ajax/PUT (str "/api/requests/" (:request/id @doc))
              {:params (request-coercions @doc)
               :handler #(rf/dispatch [:navigate/by-path "/#/"])
               :error-handler #(prn %)})
    nil))

(rf/reg-event-fx
  :request.entity/create
  base-interceptors
  (fn [_ [{:keys [params handler error-handler] rid :request/id eid :entity/id}]]
    (let [uri (cond-> ["/api" "requests" rid "entities"]
                      eid  (conj eid)
                      true (->> (clojure.string/join "/")))]
      (ajax/POST uri
                 {:params params
                  :handler handler
                  :error-handler error-handler}))
    nil))

(rf/reg-event-fx
  :request.entity.create/handler
  base-interceptors
  (fn [_ [{rid :request/id :keys [doc temp-doc path]}]]
    (let [save-to-doc! (fn [entity]
                         (if (get-in @doc path)
                           (rf/dispatch [:update-in! doc path #(conj % entity)])
                           (rf/dispatch [:assoc-in! doc path [entity]])))
          f (fn [eid-map]
              (save-to-doc! (merge @temp-doc eid-map))
              (rf/dispatch [:clear-form! temp-doc])
              (rf/dispatch [:remove-modal]))
          params {:request/id rid
                  :params @temp-doc
                  :handler f}]
      (if rid
        (rf/dispatch [:request.entity/create params])
        (rf/dispatch [:entity/create params]))
      nil)))

(rf/reg-event-fx
  :request.entity/delete
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id}]]
    (ajax/DELETE (str "/api/requests/" rid
                      "/entities/" eid))
    nil))

(rf/reg-event-fx
  :request.entity.superscription.create/handler
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id
           :keys [doc temp-doc path]}]]
    (let [f (fn [sid-map]
              ;; Assoc the vals to the main doc.
              (rf/dispatch-sync
                [:assoc-in! doc path 
                  (merge @temp-doc sid-map)])
              (rf/dispatch [:clear-form! temp-doc])
              (rf/dispatch [:remove-modal]))
          params {:request/id rid
                  :entity/id eid
                  :params @temp-doc
                  :handler f}]
      (cond
        (and rid eid)
        (rf/dispatch [:request.entity.superscription/create params])
        
        eid
        (rf/dispatch [:entity.superscription/create params]))
      nil)))

(rf/reg-event-fx
  :request.entity.superscription.edit/handler
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id sid :superscription/id
           :keys [doc path temp-doc] :as kwargs}]]
    (when (and rid eid)
      (rf/dispatch [:request.entity.superscription/delete
                    {:request/id rid
                     :entity/id eid
                     :superscription/id sid}]))
    (rf/dispatch [:request.entity.superscription.create/handler kwargs])
    nil))

(rf/reg-event-fx
  :request.entity.superscription.delete/handler
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id sid :superscription/id 
           :keys [doc path]}]]
    (let [params {:request/id rid
                  :entity/id eid
                  :superscription/id sid
                  :handler #(rf/dispatch [:assoc-in! doc path nil])}]
      (cond
        (and rid eid sid)
        (rf/dispatch [:request.entity.superscription/delete params])
        
        (and eid sid)
        (rf/dispatch [:entity.superscription/delete params]))
      nil)))

(rf/reg-event-fx
  :request.superscription/create
  base-interceptors
  (fn [_ [{rid :request/id :keys [handler params]}]]
    (ajax/POST (str "/api/requests/" rid "/superscriptions")
               {:params (superscription-coercions params)
                :handler handler})
    nil))

(rf/reg-event-fx
  :request.superscription/delete
  base-interceptors
  (fn [_ [{rid :request/id sid :superscription/id :keys [handler]}]]
    (ajax/DELETE (str "/api/requests/" rid
                      "/superscriptions/" sid)
                 {:handler handler})
    nil))

(rf/reg-event-fx
  :request.entity.superscription/create
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id :keys [params handler]}]]
    (ajax/POST (str "/api/requests/" rid
                    "/entities/" eid "/superscriptions")
               {:handler handler
                :params (superscription-coercions params)
                :response-format :json
                :keywords? true})
    nil))

(rf/reg-event-fx
  :request.entity.superscription/delete
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id sid :superscription/id
           :keys [handler]}]]
    ;; Set the entity/superscription to nil
    ;(swap! doc assoc-in path nil)
    (when (and rid eid sid)
      (ajax/DELETE (str "/api/requests/" rid 
                        "/entities/" eid 
                        "/superscriptions/" sid)
                   {:handler handler}))
    nil))


(rf/reg-event-fx
  :request.create-superscription/handler
  base-interceptors
  (fn [_ [{rid :request/id :keys [doc temp-doc path]}]]
    (let [f (fn [ret]
              ;; Assoc the vals to the main doc.
              (rf/dispatch-sync
                [:assoc-in! doc path 
                  (merge @temp-doc ret)])
              (rf/dispatch [:clear-form! temp-doc])
              (rf/dispatch [:remove-modal]))
          params {:request/id rid
                  :params @temp-doc
                  :handler f}]
      ;; If there's a request/id we have to create a request-superscription
      ;; relation.
      (if rid
        (rf/dispatch [:request.superscription/create params])
        (rf/dispatch [:superscription/create params])))
    nil))

(rf/reg-event-fx
  :request.edit-superscription/handler
  base-interceptors
  (fn [_ [{rid :request/id sid :superscription/id 
           :keys [doc temp-doc path]
           :as kwargs}]]
    ;; If there's a request/id we have to delete the request-superscription
    ;; relation before creating another one.
    (when rid
      (rf/dispatch [:request.superscription/delete
                    {:request/id rid
                     :superscription/id sid}]))
    (rf/dispatch [:request.create-superscription/handler kwargs])
    nil))

(rf/reg-event-fx
  :request.delete-superscription/handler
  base-interceptors
  (fn [_ [{rid :request/id sid :superscription/id
           :keys [doc path] :as kwargs}]]
    (let [f #(rf/dispatch [:assoc-in! doc path nil])]
      ;; If there's a request/id we have to delete the request-superscription
      ;; relation.
      (if rid
        (rf/dispatch [:request.superscription/delete
                      {:request/id rid
                       :superscription/id sid
                       :handler f}])
        (f))
      nil)))

;; -------------------------
;; Subs

(rf/reg-sub
  :delicts/all
  (fn [db] (:delicts/all db)))

(rf/reg-sub
  :requests/all
  (fn [db _] (:requests/all db)))

(rf/reg-sub
  :requests/latest
  :<- [:requests/all]
  (fn [reqs _]
    (take 10 reqs)))

(rf/reg-sub
  :requests/pending
  :<- [:requests/all]
  (fn [reqs _]
    (filter #(not= (:status %) "done") reqs)))

(rf/reg-sub
  :requests/request
  (fn [db _] (:requests/request db))) 
        
(rf/reg-sub
  :requests.new/delicts
  (fn [db _]
    (get-in db [:requests :new :request/delicts])))

(rf/reg-sub 
  :requests/priority-score
  (fn [db [_ path]]
    (let [delicts (:delicts/all db)]
          ;checked (get-in db (conj path :request/delicts))]
      (fn [checked]
        (->> checked
             (filter (fn [[id bool]] bool))
             (reduce (fn [acc [id bool]] 
                       (+ acc (some (fn [d] 
                                      (and (= id (:delict/id d)) 
                                           (:delict/weight d)))
                                    delicts)))
                     0))))))