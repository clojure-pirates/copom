(ns copom.events.superscription
  (:require
    [ajax.core :as ajax]
    [copom.events.utils :refer [base-interceptors superscription-coercions]]
    [re-frame.core :as rf]))

;; NOTE: this event is being decentralized
(rf/reg-event-fx
  :superscription.create-superscription-modal/success
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id :keys [doc temp-doc path]}]]
    (let [handler (fn [ret]
                    ;; Assoc the vals to the main doc.
                    (rf/dispatch-sync
                      [:assoc-in! doc path 
                        (merge @temp-doc ret)])
                    (rf/dispatch [:clear-form! temp-doc])
                    (rf/dispatch [:remove-modal]))
          params {:request/id rid
                  :entity/id eid
                  :params @temp-doc
                  :handler handler}]
     (cond (and rid eid)
           (rf/dispatch-sync
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

;; NOTE: this event is being decentralized
(rf/reg-event-fx
  :superscription.edit-superscription-modal/success
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id sid :superscription/id
           :keys [doc temp-doc path] :as kwargs}]]
    (let [handler (fn [ret]
                    ;; Assoc the vals to the main doc.
                    (rf/dispatch-sync [:assoc-in! doc path
                                        (merge @temp-doc ret)])
                    (rf/dispatch [:clear-form! temp-doc])
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
                 [:request.superscription/create params]))
           :else
           (rf/dispatch
             [:superscription/create params])))))


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

