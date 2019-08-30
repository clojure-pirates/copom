(ns copom.events.superscription
  (:require
    [ajax.core :as ajax]
    [copom.events.utils :refer [base-interceptors]]
    [re-frame.core :as rf]))

(def core-superscription-keys
  [:superscription/num :superscription/complement :superscription/reference
   :superscription/city :superscription/state])

(defn superscription-coercions [m]
  (merge (select-keys m core-superscription-keys)
         {:neighborhood/id (get-in m [:superscription/neighborhood :neighborhood/id])
          :route/id (get-in m [:superscription/route :route/id])}))

(rf/reg-event-fx
  :superscription.create-superscription-modal/success
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id :keys [doc temp-doc path]}]]
    (let [handler (fn [ret]
                    (rf/dispatch [:assoc-in! doc path 
                                  (merge @temp-doc ret)])
                    (rf/dispatch [:remove-modal]))
          params {:request/id rid
                  :entity/id eid
                  :params @temp-doc
                  :handler handler}]
     (cond (and rid eid)
           (rf/dispatch 
             [:request.entity.superscription/create params])
           eid
           (do (rf/dispatch 
                 [:entity.superscription/create params]))
           rid
           (do (rf/dispatch
                 [:request.superscription/create params]))
           :else
           (rf/dispatch
             [:superscription/create params]))
     nil)))

(rf/reg-event-fx
  :superscription.edit-superscription-modal/success
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id sid :superscription/id
           :keys [doc temp-doc path] :as kwargs}]]
    (let [handler (fn [ret]
                    (rf/dispatch [:assoc-in! doc path
                                  (merge @temp-doc ret)])
                    (rf/dispatch [:remove-modal]))
          params {:request/id rid
                  :entity/id eid
                  :params @temp-doc
                  :handler handler}]
     (cond (and rid eid)
           (do (rf/dispatch 
                 [:request.entity.superscription/delete kwargs])
               (rf/dispatch
                 [:request.entity.superscription/create params]))
           eid
           (rf/dispatch 
             [:entity.superscription/create params])
           rid
           (do (rf/dispatch
                 [:request.superscription/delete kwargs])
               (rf/dispatch
                 [:request.superscription/create params]))))))


(rf/reg-event-fx
  :superscription/create
  base-interceptors
  (fn [_ [{:keys [handler params]}]]
    (ajax/POST "/api/superscriptions"
               {:params (superscription-coercions params)
                :handler handler})
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

; When there's an eid and sid, will create an entity-superscription.
; When there's only the eid, will create a sup with the given params, 
; and then create an entity-superscription.
(rf/reg-event-fx
  :entity.superscription/create
  base-interceptors
  (fn [_ [{eid :entity/id sid :superscription/id :keys [params handler]}]]
    (let [base (str "/api/entities/" eid "/superscriptions")
          uri (if sid (str base "/" sid) base)]
      (ajax/POST uri
                 {:handler handler
                  :params (superscription-coercions params)
                  :response-format :json
                  :keywords? true})
      nil)))


(rf/reg-event-fx
  :request.entity.superscription/delete
  base-interceptors
  (fn [_ [{doc :doc path :path rid :request/id eid :entity/id 
           sid :superscription/id :as params}]]
    ;; Set the entity/superscription to nil
    ;(swap! doc assoc-in path nil)
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

(rf/reg-event-fx
  :request.superscription/create
  base-interceptors
  (fn [_ [{rid :request/id :keys [handler params]}]]
    (ajax/POST (str "/api/requests/" rid "/superscriptions")
               {:params (superscription-coercions params)
                :handler handler})
    nil))