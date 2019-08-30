(ns copom.events.request
  (:require
    [ajax.core :as ajax]
    [copom.db :refer [app-db]]
    [copom.events.utils :refer [base-interceptors]]
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

(defn edit-mode [req]
  (let [;; :request/requester, :request/suspect, :request/witness, 
        ;; :request/victim
        req-entities
        (reduce (fn [m {{r :request-role/role} :entity/role :as e}]
                  (assoc m (keyword "request" r) e))
                {} (:request/entities req))
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
  (fn [_ [{:keys [params handler] rid :request/id eid :entity/id}]]
    (ajax/POST (str "/api/requests/" rid "/entities/" eid)
               {:params params
                :handler handler})
    nil))

(rf/reg-event-fx
  :request.entity/delete
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id}]]
    (ajax/DELETE (str "/api/requests/" rid
                      "/entities/" eid))
    nil))

(rf/reg-event-fx
  :entity/create
  base-interceptors
  (fn [_ [{:keys [params handler]}]]
    (ajax/POST "/api/entities"
               {:params params
                :handler (when handler handler)})
    nil))

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