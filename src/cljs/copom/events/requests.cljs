(ns copom.events.requests
  (:require
    [ajax.core :as ajax]
    [clojure.pprint]
    [copom.db :refer [app-db]]
    [copom.events.utils :refer [base-interceptors]]
    [reagent.session :as session]
    [re-frame.core :as rf]))

;; -------------------------
;; Helpers

(def core-superscription-keys
  [:superscription/num :superscription/complement :superscription/reference
   :superscription/city :superscription/state])
  

(def core-request-keys
  [:request/complaint :request/summary :request/event-timestamp 
   :request/status :request/measures])

(def requests-interceptos 
  (conj base-interceptors (rf/path :requests)))

(defn event-timestamp [date time]
  (-> date (.split "T") first
      (str "T" time ".000")))

(defn superscription-coercions [m]
  (merge (select-keys m core-superscription-keys)
         {:neighborhood/id (get-in m [:superscription/neighborhood :neighborhood/id])
          :route/id (get-in m [:superscription/route :route/id])}))

(defn request-coercions [m]
  (-> m
      (update :request/delicts #(->> % (filter (fn [[k v]] v)) (map (fn [[k v]] k))))
      (assoc :request/event-timestamp (event-timestamp (:request/date m) (:request/time m)))
      (dissoc :request/date :request/time)))
  

(rf/reg-event-db
  :requests/clear-form
  requests-interceptos
  (fn [reqs [doc]]
    (reset! doc nil)))

(def required-request-fields
  #{:request/complaint :request/summary :request/status})

(def field-translation
  {:request/complaint "natureza"
   :request/summary "resumo da requisição"
   :request/status "status"})

(defn min-request-params? [params]
  (let [[complaint summary status] ((apply juxt required-request-fields) params)]
    (and complaint summary status)))

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
                (clojure.string/split s #"T"))
        req-datetime #:request{:date d :time t}]
    (-> req
        (assoc :request/delicts req-delicts)
        (merge req-datetime
               req-entities))))
           

;; -------------------------
;; Handlers

(defn create-request! [fields]
  (ajax/POST "/api/requests"
             {:params (request-coercions fields)
              :handler #(do 
                            (rf/dispatch [:navigate/by-path "/#/"])
                            (rf/dispatch [:requests/clear-form]))
              :error-handler #(prn %)})
  nil)

(rf/reg-event-fx
  :requests/create
  base-interceptors
  (fn [{:keys [db]} [fields]]
    (if (min-request-params? fields)
      (create-request! fields)
      {:db (assoc-in db [:requests :new :request/errors]
                     (request-error-msg fields))})))
        
(rf/reg-event-fx
  :requests/load-delicts
  base-interceptors
  (fn [_ _]
    (ajax/GET "/api/delicts"
              {:handler #(rf/dispatch [:rff/set [:delicts/all] %])
               :error-handler #(prn %)
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
               :error-handler #(prn %)
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
  (fn [_ [fields]]
    (let [r (rf/subscribe [:requests/request])]
      (when (not= (select-keys r core-request-keys) 
                  (select-keys fields core-request-keys))
        (ajax/PUT (str "/api/requests/" (:request/id fields))
                  {:params (request-coercions fields)
                   :handler #(rf/dispatch [:navigate/by-path "/#/"])
                   :error-handler #(prn %)}))
      nil)))

(rf/reg-event-fx
  :request.entity.superscription/delete
  base-interceptors
  (fn [_ [{doc :doc path :path rid :request/id eid :entity/id 
           sid :superscription/id :as params}]]
    ;; Set the entity/superscription to nil
    (swap! doc assoc-in path nil)
    (when (and rid eid sid)
      (ajax/DELETE (str "/api/requests/" rid 
                        "/entities/" eid 
                        "/superscriptions/" sid)
                   {:error-handler #(prn %)}))
    nil))

(rf/reg-event-fx
  :request.superscription/delete
  base-interceptors
  (fn [_ [{rid :request/id sid :superscription/id}]]
    (ajax/DELETE (str "/api/requests/" rid
                      "/superscriptions/" sid)
                 {:error-handler #(prn %)})
    nil))

(rf/reg-event-fx
  :entity.superscription/delete
  base-interceptors
  (fn [_ [{doc :doc path :path eid :entity/id 
           sid :superscription/id :as params}]]
    ;; Set the entity/superscription to nil
    (swap! doc assoc-in path nil)
    (ajax/DELETE (str "/api/entities/" eid 
                      "/superscriptions/" sid)
                 {:error-handler #(prn %)})
    nil))

; - Creates superscription.
; - Assocs the :superscription/id returned to the doc.
(defn create-superscription! [{:keys [doc path uri]}]
  (ajax/POST uri
             {:handler #(do (swap! doc assoc-in 
                                   (conj path :superscription/id) 
                                   (:superscription/id %)) 
                            (rf/dispatch [:remove-modal]))
              :error-handler #(prn %)
              :params (-> (get-in @doc path) superscription-coercions)
              :response-format :json
              :keywords? true}))

(rf/reg-event-fx
  :request.entity.superscription/create
  base-interceptors
  (fn [_ [{rid :request/id :keys [doc path]}]]
    (create-superscription!
      {:uri (str "/api/requests/" rid "/superscriptions")
       :doc doc 
       :path path})
    nil))

(rf/reg-event-fx
  :request.entity.superscription/create
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id :keys [doc path]}]]
    (create-superscription!
      {:uri (str "/api/requests/" rid
                 "/entities/" eid "/superscriptions")
       :doc doc 
       :path path})
    nil))

(rf/reg-event-fx
  :entity.superscription/create
  base-interceptors
  (fn [_ [{eid :entity/id :keys [doc path]}]]
    (create-superscription!
      {:uri (str "/api/entities/" eid "/superscriptions")
       :doc doc 
       :path path})
    nil))

(rf/reg-event-fx
  :request.superscription/create
  base-interceptors
  (fn [_ [{rid :request/id :keys [doc path]}]]
    (create-superscription!
      {:uri (str "/api/requests/" rid "/superscriptions")
       :doc doc 
       :path path})
    nil))


(rf/reg-event-fx
  :neighborhood/create
  base-interceptors
  (fn [_ [doc]]
    (ajax/POST "/api/neighborhoods"
               {:params @doc
                :handler #(rf/dispatch [:remove-modal])
                :error-handler #(prn %)
                :response-format :json
                :keywords? true})
    nil))

(rf/reg-event-fx
  :route/create
  base-interceptors
  (fn [_ [doc]]
    (ajax/POST "/api/routes"
               {:params @doc
                :handler #(rf/dispatch [:remove-modal])
                :error-handler #(prn %)
                :response-format :json
                :keywords? true})
    nil))

(rf/reg-event-fx
  :entity/query
  base-interceptors
  (fn [_ [query]]
    (let [{:entity/keys [name phone]} @query
          col (if name "names" "phones")]
      (ajax/GET (str "/api/entities/" col)
                {:params {:query (or name phone)}
                 :handler #(do (swap! query assoc :items %))
                 :error-handler #(prn %)
                 :response-format :json
                 :keywords? true}))
    nil))

(rf/reg-event-fx
  :entity/create
  base-interceptors
  (fn [_ [doc]]
    (ajax/POST "/api/entities"
               {:params @doc
                :handler #(rf/dispatch [:remove-modal])
                :error-handler #(prn %)})
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