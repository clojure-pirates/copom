(ns copom.events.entity
  (:require
    [ajax.core :as ajax]
    [copom.db :refer [app-db]]
    [copom.events.utils :refer [base-interceptors]]
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :entity/query
  base-interceptors
  (fn [_ [query]]
    (let [{:entity/keys [name phone]} @query
          col (if name "names" "phones")]
      (ajax/GET (str "/api/entities/" col)
                {:params {:query (or name phone)}
                 :handler #(rf/dispatch [:assoc-in! query [:items] %])
                 :error-handler #(prn %)
                 :response-format :json
                 :keywords? true}))
    nil))

;; - When and rid eid, delete the request-entity
;; - Create a new entity (and request-entity, when rid).
;; - assoc the returned entity/id into the doc's entity path.
(rf/reg-event-fx
  :entity.edit-entity-modal/save
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id sid :superscription/id 
           :keys [doc path temp-doc]}]]
    (let [handle (fn [val]
                   (rf/dispatch [:assoc-in! doc path val])
                   (rf/dispatch [:remove-modal]))]
      (cond (and rid eid)
            (do (rf/dispatch
                  [:request.entity/delete
                   {:request/id rid
                    :entity/id eid}])
                (rf/dispatch
                  [:entity/create
                   {:params @temp-doc
                    :handler
                    (fn [ret]
                      (when sid
                        (rf/dispatch
                          [:entity.superscription/create
                           {:entity/id (:entity/id ret)
                            :superscription/id sid
                            :params @temp-doc}]))
                      (rf/dispatch
                        [:request.entity/create
                         {:params (merge @temp-doc ret)
                          :request/id rid
                          :entity/id (:entity/id ret)
                          :handler
                          (fn [_]
                            (handle (merge @temp-doc ret)))}]))}]))
            :else
            (rf/dispatch
              [:entity/create
               {:params @temp-doc
                :handler
                (fn [ret]
                  (handle (merge @temp-doc ret)))}])))
    nil))

;; assoc the selected superscription (by its :superscription/id)
;; to :entity/superscription of the main doc.
(rf/reg-event-fx
  :entity.entity-pick-modal/select
  base-interceptors
  (fn [_ [{rid :request/id eid :entity/id :keys [doc temp-doc path entity]}]]
    ;; Create a request-entity of the selected entity:
    (when rid
      (rf/dispatch
        [:request.entity/create
         {:request/id rid
          :entity/id eid
          :params entity}]))
    ;; Assoc the selected entity's fields to the doc:
    (rf/dispatch 
      [:assoc-in! doc path
       (assoc @temp-doc :entity/superscription
          (->> (:entity/superscriptions entity)
               (some #(and (= (:superscription/id %)
                              (get-in @temp-doc [:entity/superscription])) 
                           %))))])
    (rf/dispatch [:remove-modal]) 
    nil))