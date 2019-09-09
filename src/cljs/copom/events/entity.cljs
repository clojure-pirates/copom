(ns copom.events.entity
  (:require
    [ajax.core :as ajax]
    [copom.db :refer [app-db]]
    [copom.events.utils :refer [base-interceptors superscription-coercions]]
    [copom.views.components :as comps]
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

(rf/reg-event-fx
  :entity/create
  base-interceptors
  (fn [_ [{:keys [params handler]}]]
    (ajax/POST "/api/entities"
               {:params params
                :handler handler})
    nil))

(rf/reg-event-fx
  :entity.create/handler
  base-interceptors
  (fn [_ [doc]]
    (rf/dispatch [:entity/create
                  {:params doc
                   :handler #(rf/dispatch [:remove-modal])}])
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
  :entity.superscription/delete
  base-interceptors
  (fn [_ [{:keys [handler] eid :entity/id sid :superscription/id}]]
    (ajax/DELETE (str "/api/entities/" eid 
                      "/superscriptions/" sid)
                 {:handler handler})
    nil))


(rf/reg-event-fx
  :entity/dissoc!
  base-interceptors
  (fn [_ [{:keys [doc path]}]]
    (let [parent-path (pop path)
          v (get-in @doc parent-path)
          i (last path)
          before (subvec v 0 i)
          after (subvec v (inc i))]
      (rf/dispatch [:assoc-in! doc parent-path (into before after)]))
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
          :params entity
          :handler
          (fn [_]
            ;; Assoc the selected entity's fields to the doc.
            ;; We either conj it to the path, or assoc it to the path 
            ;; inside a vector.
            (let [v (assoc @temp-doc :entity/superscription
                      (->> (:entity/superscriptions entity)
                           (some #(and (= (:superscription/id %)
                                          (get-in @temp-doc [:entity/superscription])) 
                                       %))))
                  parent-path (if (int? (last path)) 
                                (pop path) path)
                  entities (get-in @doc parent-path)]
              (if (seq entities)
                (rf/dispatch [:update-in! doc parent-path conj v])
                (rf/dispatch [:assoc-in! doc parent-path [v]]))))
          :error-handler #(js/alert %)}]))
    (rf/dispatch [:remove-modal]) 
    nil))