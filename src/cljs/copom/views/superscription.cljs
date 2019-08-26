(ns copom.views.superscription
  (:require
    [clojure.string :as string]
    [copom.forms :as rff :refer [input select]]
    [copom.views.components :as comps :refer [form-group]] 
    [reagent.core :as r]
    [re-frame.core :as rf]))

(def route-types
  (->> ["Rua", "Avenida", "Rodovia", "Linha", "Travessa", "Praça"]
       (mapv string/upper-case)
       r/atom))

(defn create-route-modal []
  (let [doc (r/atom nil)]
    (fn []
      [comps/modal
       {:header [:h3 "Novo logradouro"]
        :body [:div
               [select {:name :route/type
                        :class "form-control"
                        :doc doc
                        :default-value "RUA"}
                 (for [r @route-types]
                   ^{:key r}
                   [:option {:value r} r])]
               [input {:type :text
                       :name :route/name
                       :placeholder "Logradouro"
                       :doc doc
                       :class "form-control"}]]
        :footer [:div
                 [:button.btn.btn-success
                  {:on-click #(rf/dispatch [:route/create doc])}
                  "Criar"]
                 [:button.btn.btn-danger
                  {:on-click #(rf/dispatch [:remove-modal])}
                  "Cancelar"]]}])))


(defn create-neighborhood-modal []
  (let [doc (r/atom nil)]
    (fn []
      [comps/modal
       {:header [:h3 "Novo bairro"]
        :body [:div
               [input {:type :text
                       :name :neighborhood/name
                       :placeholder "Bairro"
                       :doc doc
                       :class "form-control"}]]
        :footer [:div
                 [:button.btn.btn-success
                  {:on-click #(rf/dispatch [:neighborhood/create doc])}
                  "Criar"]
                 [:button.btn.btn-danger
                  {:on-click #(rf/dispatch [:remove-modal])}
                  "Cancelar"]]}])))

;; TODO: route-types sub
;; TODO: typeheads for neighborhood, route-name, city, state
(defn address-form [{:keys [doc path opts]}]
  (let [disabled? 
        (or (:disabled? opts)
            ;; If there's a superscription the form is disabled and the user
            ;; can only alter it through the buttons.
            (get-in @doc (conj path :superscription/id)))] 
                                         
    [:div
     [form-group
      [:span "Logradouro"]
      [:div.form-row.align-items-center
       
       [:div.col>div.input-group.border
        (when-not disabled?
          [:div.input-group-prepend
           [:div.input-group-text
            [:button.btn.btn-sm.btn-default 
             {:on-click #(rf/dispatch [:modal create-neighborhood-modal])}
             "+"]]])
        (let [path (conj path :superscription/neighborhood)]
          [comps/input
           {:type :typehead
            :name (conj path :neighborhood/name)
            :class "form-control"
            :doc doc
            :disabled disabled?
            :getter :neighborhood/name
            :handler #(swap! doc assoc-in (conj path :neighborhood/id)
                             (:neighborhood/id %))
            :data-source {:uri "/api/neighborhoods"}
            :placeholder "Bairro"}])]
               
       [:div.col-md-2
        [select {:name (conj path :superscription/route :route/type)
                 :class "form-control"
                 :doc doc
                 :disabled disabled?
                 :default-value "RUA"}
         (for [r @route-types]
           ^{:key r}
           [:option {:value r} r])]]
       
       [:div.col>div.input-group.border
        (when-not disabled?
          [:div.input-group-prepend
           [:div.input-group-text
            [:button.btn.btn-sm.btn-default 
             {:on-click #(rf/dispatch [:modal create-route-modal])}
             "+"]]])
        [:div
         ;; TODO: typehead
         (let [path (conj path :superscription/route)]
           [comps/input
            {:type :typehead
             :class "form-control"
             :doc doc
             :disabled disabled?
             :name (conj path :route/name)
             :getter :route/name
             :handler #(swap! doc assoc-in path %)
             :data-source {:uri "/api/routes"}
             :placeholder "Logradouro"}])]]
        
       [:div.col
        [input {:type :text
                :class "form-control"
                :doc doc
                :disabled disabled?
                :name (conj path :superscription/num)
                :placeholder "Número"}]]]]
     
     [:div.form-row
      [:div.col
       [form-group
        "Complemento"
        [input {:type :text
                :class "form-control"
                :doc doc
                :disabled disabled?
                :name (conj path :superscription/complement)
                :placeholder "Apartamento, Quadra, Lote, etc."}]]]
      [:div.col
       [form-group
        "Ponto de referência"
        [input {:type :text
                :class "form-control"
                :doc doc
                :disabled disabled?
                :name (conj path :superscription/reference)}]]]]
     ;; TODO: typehead
     [:div.form-row
      [:div.col
       [form-group
        "Município"
        [input {:type :text
                :class "form-control"
                :doc doc
                :disabled disabled?
                :name (conj path :superscription/city)
                :default-value "Guarantã do Norte"}]]]
      [:div.col
       ;; TODO: typehead
       [form-group
        "Estado"
        [input {:type :text
                :class "form-control"
                :doc doc
                :disabled disabled?
                :name (conj path :superscription/state)
                :default-value "Mato Grosso"}]]]]]))

(defn superscription-modal 
  [{:keys [doc path header footer]}]
  [comps/modal
   {:header header
    :body [address-form {:doc doc :path []}]
    :footer footer}])

(defn create-superscription-modal
  [{:keys [doc path] rid :request/id eid :entity/id sid :superscription/id
    :as kwargs}]
  (let [temp-doc (r/atom (-> (get-in @doc path) (dissoc :superscription/id)))]
    (fn []
      [superscription-modal
       {:doc temp-doc
        :path path
        :header [:h3 "Novo Endereço"]
        :footer [:div
                 [:button.btn.btn-success 
                  {:on-click
                   #(do (swap! doc assoc-in path @temp-doc)
                       (cond (and rid eid)
                             (rf/dispatch 
                               [:request.entity.superscription/create kwargs])
                             eid
                             (rf/dispatch 
                               [:entity.superscription/create kwargs])
                             rid
                             (rf/dispatch
                               [:request.superscription/create kwargs])))}
                  "Criar"]
                 [:button.btn.btn-danger 
                  {:on-click #(rf/dispatch [:remove-modal])}
                  "Cancelar"]]}])))

(defn edit-superscription-modal
  [{:keys [doc path] rid :request/id eid :entity/id sid :superscription/id
    :as kwargs}]
  (let [temp-doc (r/atom (-> (get-in @doc path) (dissoc :superscription/id)))]
    (fn []
      [superscription-modal
       {:doc temp-doc
        :path path
        :header [:h3 "Alterar endereço"]
        :footer [:div
                 [:button.btn.btn-success 
                  {:on-click
                   #(do (swap! doc assoc-in path @temp-doc)
                       (cond (and rid eid)
                             (do (rf/dispatch 
                                   [:request.entity.superscription/delete kwargs])
                                 (rf/dispatch
                                   [:request.entity.superscription/create kwargs]))
                             eid
                             (rf/dispatch 
                               [:entity.superscription/create kwargs])
                             rid
                             (do (rf/dispatch
                                   [:request.superscription/delete kwargs])
                                 (rf/dispatch
                                   [:request.superscription/create kwargs]))))}
                         
                  "Salvar"]
                 [:button.btn.btn-danger 
                  {:on-click #(rf/dispatch [:remove-modal])}
                  "Cancelar"]]}])))

(defn create-superscription-button 
  [{:keys [doc path] rid :request/id eid :entity/id sid :superscription/id
    :as kwargs}]
  [:button.btn.btn-success 
   {:on-click #(rf/dispatch 
                 [:modal (partial create-superscription-modal kwargs)])}
   "Novo"])

(defn edit-superscription-button 
  [{:keys [doc path] rid :request/id eid :entity/id sid :superscription/id
    :as kwargs}]
  [:button.btn.btn-success 
   {:on-click #(rf/dispatch 
                 [:modal (partial edit-superscription-modal kwargs)])}
   "Alterar"])

(defn delete-superscription-button 
  [{rid :request/id eid :entity/id sid :superscription/id 
    :keys [doc path] :as kwargs}]
  [:button.btn.btn-danger
   {:on-click
    #(do (swap! doc assoc-in path nil)
        (cond (and rid eid sid)
              (rf/dispatch [:request.entity.superscription/delete kwargs])
              (and eid sid)
              (rf/dispatch [:entity.superscription/delete kwargs])
              (and rid sid)
              (rf/dispatch [:request.superscription/delete kwargs])))}
   "Excluir"])
