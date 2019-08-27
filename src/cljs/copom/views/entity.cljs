(ns copom.views.entity
  (:require
    [copom.forms :as rff :refer [input select]]
    [copom.views.components :as comps :refer [form-group]]
    [copom.views.superscription :as sup :refer 
     [address-form]]
    [reagent.core :as r]
    [re-frame.core :as rf]))
    

(def docs-type ["CNH", "CNPJ" "CPF" "RG" "PASSAPORTE"])

(declare entity-pick-modal)
   
(defn query-items [{:keys [query] :as kwargs}]
  (let [page (r/atom 0)]
    (fn []
      (when-let [items (-> @query :items comps/partition-links)]
        [:div.row>div.col-md-12>div.list-group
         [comps/pager (count items) page]
         (for [item (get items @page)]
           ^{:key (:entity/id item)}
           [:a.list-group-item.list-group-item-action
            ;; open modal with entity fields
            {:on-click #(rf/dispatch 
                          [:modal (partial entity-pick-modal
                                           (assoc kwargs :entity item))])}
            (:entity/name item)])]))))

(declare create-entity-modal)

(defn entity-form-search [{:keys [doc path role] :as kwargs}]
  (let [query (r/atom nil)]
    (fn []
      [:div
       [input {:type :hidden
               :doc doc
               :name (conj path :entity/role)
               :default-value role}]
       [:div.form-row
        [:div.col
         [form-group
          [:span "Nome"
           [:span.text-danger " *obrigatório"]]
          [input {:type :text
                  :class "form-control"
                  :on-focus #(swap! query assoc :entity/phone nil)
                  :doc query
                  :name :entity/name}]]]
        [:div.col
         [form-group
          [:span "Telefone"
           [:span.text-danger " *obrigatório"]]
          [input {:type :number
                  :class "form-control"
                  :on-focus #(swap! query assoc :entity/name nil)
                  :doc query
                  :name :entity/phone}]]]
        [:div.col
         [:button.btn.btn-primary
          {:on-click #(rf/dispatch [:entity/query query])}
          "Buscar"]
         [:button.btn.btn-danger
          {:on-click #(swap! doc assoc-in path nil)}
          "Limpar"]
         (when (:items @query)
           [:button.btn.btn-success
            {:on-click #(rf/dispatch [:modal (partial create-entity-modal role)])}
            "+"])]]
       [query-items (assoc kwargs :query query)]])))

(defn entity-core-form [{:keys [doc path role opts]}]
  (let [disabled? (:disabled? opts)]
    (fn []
      [:div
       [input {:type :hidden
               :class "form-control"
               :doc doc
               :name (conj path :entity/role)
               :default-value role}]
       [:div.form-row
        [:div.col
         [form-group
          [:span "Nome"
           [:span.text-danger " *obrigatório"]]
          [input {:type :text
                  :class "form-control"
                  :doc doc
                  :disabled disabled?
                  :name (conj path :entity/name)}]]]
        [:div.col
         [form-group
          [:span "Telefone"
           [:span.text-danger " *obrigatório"]]
          [input {:type :number
                  :class "form-control"
                  :doc doc
                  :disabled disabled?
                  :name (conj path :entity/phone)}]]]]])))

(defn entity-docs-form [{:keys [doc path opts]}]
  (let [disabled? (:disabled? opts)]
    (fn []
      [:div
       [:fieldset
         [:legend "Documento de identidade"]
         [:div.form-row
          [:div.col
           [form-group
            "Tipo de documento"
            [select {:name (conj path :entity/doc-type)
                     :class "form-control"
                     :doc doc
                     :disabled disabled?
                     :default-value "CPF"}
             (for [d docs-type]
               ^{:key d}
               [:option {:value d} d])]]]
          [:div.col
           [form-group
            "Órgão emissor"
            [input {:type :text
                    :class "form-control"
                    :doc doc
                    :disabled disabled?
                    :name (conj path :entity/doc-issuer)}]]]
          [:div.col
           [form-group
            "Número do documento"
            [input {:type :text
                    :class "form-control"
                    :doc doc
                    :disabled disabled?
                    :name (conj path :entity/doc-number)}]]]]]
       ;; TODO: on-click expand
       [:fieldset
        [:legend "Filiação"]
        [:div.form-row
         [:div.col
          [form-group
           "Pai"
           [input {:type :text
                   :class "form-control"
                   :doc doc
                   :disabled disabled?
                   :name (conj path :entity/father)}]]]
         [:div.col
          [form-group
           "Mãe"
           [input {:type :text
                   :class "form-control"
                   :doc doc
                   :disabled disabled?
                   :name (conj path :entity/mother)}]]]]]])))

(defn entity-form [{:keys [doc path role opts] :as kwargs}]
  (let [address-path (conj path :entity/superscription)
        rid (:request/id @doc)
        eid (get-in @doc (conj path :entity/id))
        sid (get-in @doc (conj address-path :superscription/id))]
    [:div
     [entity-core-form kwargs]
     [:fieldset
      [:legend "Endereço" " "
       (when-not sid
         [:span
          [:button.btn.btn-primary 
           {:on-click #(rf/dispatch 
                         [:modal (partial entity-pick-modal
                                   (-> (select-keys kwargs [:doc :path :role])
                                       (assoc :entity (get-in @doc path))))])} 
           "Selecionar"]
          [sup/create-superscription-button
           {:doc doc
            :path address-path
            :request/id rid
            :entity/id eid}]])
       (when sid
         [:span
           [sup/edit-superscription-button
            {:doc doc
             :path address-path
             :request/id rid
             :entity/id eid
             :superscription/id sid}] 
           [sup/delete-superscription-button
            {:doc doc 
             :path address-path 
             :request/id rid 
             :entity/id eid 
             :superscription/id sid}]])]
      (when sid
        [address-form 
         {:doc doc :path address-path}])]
     [entity-docs-form kwargs]]))

(defn entity-pick-modal [{:keys [doc entity path role]}]
  (let [;; args for entity form, including a temporary doc.
        kwargs2 {:doc (r/atom entity) :path [] :role role :opts {:disabled? true}}
        ;; assoc the selected superscription (by its :superscription/id)
        ;; to :entity/superscription of the main doc.
        f (fn []
           (let [d @(:doc kwargs2)
                 p (conj (:path kwargs2) :entity/superscription)]
             (->> (:entity/superscriptions entity)
                  (some #(and (= (:superscription/id %)
                                 (get-in d p))
                              %))
                  (assoc-in d p)
                  (swap! doc assoc-in path))))]                
    (fn []
      [comps/modal
       {:header [:h3 (:entity/name entity)]
        :body [:div
               [entity-core-form kwargs2]
               [entity-docs-form kwargs2]
               (map-indexed 
                  (fn [i s]
                    ^{:key (:superscription/id s)}
                    [:fieldset
                     [:legend
                       [input
                        {:type :radio
                         :name (conj (:path kwargs2) :entity/superscription)
                         :doc (:doc kwargs2)
                         :value (:superscription/id s)
                         :checked? (when (zero? i) true)}]
                       " Endereço " (inc i)]
                     ;[:legend "Endereço " (inc i)]
                     [address-form
                      {:doc (r/atom s) :path [] :opts {:disabled? true}}]])
                  (->> (:entity/superscriptions entity)
                       (sort-by :superscription/created-at >)))]
        :footer [:div
                 [:button.btn.btn-success 
                  ;; assoc the vals from the temp doc to the main doc
                  {:on-click #(do (f)
                                  (rf/dispatch [:remove-modal]))}
                  "Selecionar"]
                 [:button.btn.btn-danger 
                  {:on-click #(rf/dispatch [:remove-modal])}
                  "Cancelar"]]}])))
    
(defn create-entity-modal [role]
  (let [doc (r/atom nil)]
    (fn []
      [comps/modal
       {:header [:h3 "Criar entidade"]
        :body [entity-form {:doc doc :path [] :role role}]
        :footer [:div
                 [:button.btn.btn-success
                  {:on-click #(rf/dispatch [:entity/create doc])}
                  "Criar"]
                 [:button.btn.btn-danger
                  {:on-click #(rf/dispatch [:remove-modal])}
                  "Cancelar"]]}])))


(defn entity-form-wrapper [{:keys [doc path role] :as kwargs}]
  (let [query (r/atom nil)]
    (fn []
      (if-let [eid (get-in @doc (conj path :entity/id))]
        [entity-form (assoc kwargs :opts {:disabled? true})]
        [entity-form-search kwargs]))))
