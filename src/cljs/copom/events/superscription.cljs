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
  (fn [_ [{rid :request/id :keys [doc path]}]]
    (create-superscription!
      {:uri (str "/api/requests/" rid "/superscriptions")
       :doc doc 
       :path path})
    nil))
