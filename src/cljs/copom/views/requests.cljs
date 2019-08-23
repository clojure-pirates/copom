(ns copom.views.requests
  (:require
    [clojure.string :as string]
    [copom.db :refer [app-db]]
    [copom.views.components :as comps :refer 
     [card card-nav checkbox-input form-group radio-input]]
    [copom.router :as router]
    [reagent.core :as r]
    [reagent.session :as session]
    [re-frame.core :as rf]
    [copom.forms :as rff :refer [input select textarea]]))

(defn to-iso-date [d]
  (-> d
      .toISOString
      (.split "T")
      first))

(defn to-time-string [d]
  (-> d
      .toTimeString
      (.split " ")
      first))

(defn calculate-priority [r]
  (if-let [delicts (seq (:request/delicts r))]
    (->> delicts (map :delict/weight) (reduce +))
    0))

(defn list-requests [requests]
  [:ul.list-group.list-group-flush
    (for [r @requests]
      ^{:key (:request/id r)}
      [:a {:href (str "#/requisicoes/" (:request/id r) "/editar")}
        [:li.list-group-item
          (calculate-priority r) " | "
          (:request/complaint r) " | "
          (:request/created-at r)]])])

(def route-types (atom (mapv string/upper-case ["Rua", "Avenida", "Rodovia", "Linha", "Travessa", "Praça"])))

(def docs-type ["CNH", "CNPJ" "CPF" "RG" "PASSAPORTE"])

;; -------------------------
;; Create Request Page

;; `m` is {:(table-name)/id oid :superscription/id sid}
(defn clear-address-form-button [m path]
  [:button.btn.btn-warning.float-right 
   {:on-click #(rf/dispatch [:superscriptions/remove m path])}
   "Limpar endereço"])

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

(declare entity-form entity-core-form entity-docs-form)


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
       ;[comps/pretty-display @query]
       [query-items (assoc kwargs :query query)]])))

(defn entity-form-wrapper [{:keys [doc path role] :as kwargs}]
  (let [query (r/atom nil)]
    (fn []
      (if-let [eid (get-in @doc (conj path :entity/id))]
        [entity-form (assoc kwargs :opts {:disabled? true})]
        [entity-form-search kwargs]))))

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


(defn create-superscription-modal [{:keys [doc ent-path]}]
  (let [spath (conj ent-path :entity/superscription) 
        temp-doc (r/atom (-> @doc (get-in spath) (dissoc :superscription/id)))
        rid (:request/id @doc)
        sid (get-in @doc (conj spath :superscription/id))
        eid (get-in @doc (conj ent-path :entity/id))]
    (fn []
      [comps/modal
       {:header [:h3 "Novo endereço"]
        :body [address-form {:doc temp-doc :path []}]
        :footer [:div
                 [:button.btn.btn-success 
                  {:on-click #(do (swap! doc assoc-in spath @temp-doc)
                                  (rf/dispatch
                                    [:request.entity.superscription/delete
                                      {:doc doc
                                       :path nil 
                                       :request/id rid
                                       :entity/id eid 
                                       :superscription/id sid}])
                                  (rf/dispatch 
                                    [:request.entity.superscription/create
                                      {:doc doc 
                                       :sup-path spath
                                       :request/id rid
                                       :entity/id eid}]))}
                  "Criar"]
                 [:button.btn.btn-danger 
                  {:on-click #(rf/dispatch [:remove-modal])}
                  "Cancelar"]]}])))

(defn entity-form [{:keys [doc path role opts] :as kwargs}]
  (let [address-path (conj path :entity/superscription)
        rid (:request/id @doc)
        eid (get-in @doc (conj path :entity/id))
        sid (get-in @doc (conj address-path :superscription/id))]
    [:div
     [entity-core-form kwargs]
     [:fieldset
      [:legend "Endereço" " "
       (when sid
         [:span
           [:button.btn.btn-warning 
            {:on-click #(do
                          (rf/dispatch [:modal 
                                        (partial create-superscription-modal 
                                               {:doc doc :ent-path path})]))}
            "Alterar"]
           [:button.btn.btn-danger 
            {:on-click #(rf/dispatch [:request.entity.superscription/delete
                                      {:doc doc 
                                       :path address-path 
                                       :request/id rid 
                                       :entity/id eid 
                                       :superscription/id sid}])}
            "Excluir"]])]
      [address-form 
       {:doc doc :path address-path}]]
     [entity-docs-form kwargs]]))

(defn request-form [doc path]
  (r/with-let [fields (rf/subscribe [:rff/query path])
               delicts (rf/subscribe [:delicts/all])
               priority-score (rf/subscribe [:requests/priority-score path])]
    [card-nav
     [{:nav-title "Fato"
       :body 
             [:div
               ;[comps/pretty-display @doc]
               [form-group
                [:span "Natureza"
                 [:span.text-danger " *obrigatório"]]
                [comps/input
                 {:name :request/complaint
                  :type :typehead
                  :class "form-control"
                  :doc doc
                  :data-source {:uri "/api/requests/complaints/all"}}]]
               [form-group
                [:span "Resumo da requisição"
                 [:span.text-danger " *obrigatório"]]
                [textarea {:name :request/summary
                           :doc doc
                           :class "form-control"}]]
               [:div.form-group
                 [:label "Prioridade"
                  ": " (@priority-score (:request/delicts @doc))]
                 (for [{:delict/keys [id name weight]} @delicts]
                   ^{:key id}
                   [checkbox-input {:name [:request/delicts id]
                                    :doc doc
                                    :label (string/capitalize name)}])]
               [form-group
                "Data"
                [input {:type :date
                        :class "form-control"
                        :doc doc
                        :name :request/date
                        :default-value (to-iso-date (js/Date.))}]]
               [form-group
                "Hora"
                [input {:type :time
                        :class "form-control"
                        :doc doc
                        :name :request/time
                        :default-value (to-time-string (js/Date.))}]]]}
      {:nav-title "Endereço do fato"
       :body [:fieldset
              [:legend "Endereço"]
              [address-form {:doc doc :path [:request/superscription]}]]}
      {:nav-title "Solicitante(s)"
       ; TODO: (button to add another)]}]]
       :body [entity-form-wrapper 
              {:doc doc :path [:request/requester] :role "requester"}]}
      {:nav-title "Suspeito(s)"
       ; TODO: (button to add another)]
       :body [entity-form-wrapper 
              {:doc doc :path [:request/suspect] :role "suspect"}]}
      {:nav-title "Testemunha(s)"
       ; TODO: (button to add another)]
       :body [entity-form-wrapper 
              {:doc doc :path [:request/witness] :role "witness"}]}
      {:nav-title "Vítima(s)"
       ; TODO: (button to add another)]
       :body [entity-form-wrapper 
              {:doc doc :path [:request/victim] :role "victim"}]}
      {:nav-title "Providências"
       :body [:div
              [form-group
                "Providências"
                [textarea {:name :request/measures
                           :class "form-control"
                           :doc doc}]
               [form-group
                 [:span "Status"
                  [:span.text-danger " *obrigatório"]]
                 [radio-input {:name :request/status
                               :class "form-check-input"
                               :doc doc
                               :value "pending"
                               :label "Em aberto"
                               :checked? (when (-> @doc :request/status nil?) true)}]
                 [radio-input {:name :request/status
                               :class "form-check-input"
                               :doc doc
                               :value "dispatched"
                               :label "Despachado"}]
                 [radio-input {:name :request/status
                               :class "form-check-input"
                               :doc doc
                               :value "done"
                               :label "Finalizado"}]]]]}]]))      

(defn create-request-page []
  (r/with-let [doc (r/cursor app-db [:request])
               errors (rf/subscribe [:rff/query [:requests :new :request/errors]])]
    [:section.section>div.container>div.content
     [card
      {:title [:h4 "Nova requisição"
               (when @errors [:span.alert.alert-danger @errors])
               [:div.btn-group.float-right
                 [:button.btn.btn-success
                  {:on-click #(rf/dispatch [:requests/create @doc])}
                  "Criar"]
                 [:a.btn.btn-danger
                  {:href (router/href :requests)
                   :on-click #(rf/dispatch [:requests/clear-form doc])} 
                  "Cancelar"]]]
       :body [request-form doc [:requests :new]]}]]))

(defn create-request-button []
  [:a.btn.btn-success
   {:href "/#/requisicoes/criar"}
   "Nova requisição"])

(defn request-page []
  (let [doc (r/cursor app-db [:request])
        errors (rf/subscribe [:rff/query [:requests :new :request/errors]])]
    (fn []      
      (when (seq @doc)
        [:section.section>div.container>div.content
         [:a {:href (str "#/requisicoes/" (:request/id @doc) "/editar")} "Request"]
         [card
          {:title [:h4 "Requisição #" (:request/id @doc)
                   (when @errors [:span.alert.alert-danger @errors])
                   " "
                   [:div.btn-group
                     [:button.btn.btn-success
                      {:on-click #(rf/dispatch [:requests/update @doc])}
                      "Salvar"]
                     [:a.btn.btn-danger
                      {:href (router/href :requests)
                       :on-click #(rf/dispatch [:requests/clear-form doc])} 
                      "Cancelar"]]]
           :body [request-form doc [:requests/edit]]}]]))))

; TODO: pagination (show only n requests per page)
(defn requests-page []
  (r/with-let [requests (rf/subscribe [:requests/all])]
    [:section.section>div.container>div.content
     [card
      {:title [:h4 "Requisições"
               [:span.float-right 
                [create-request-button]]]
       :body (if (seq @requests)
               [list-requests requests]
               "Não há requisições.")}]]))


;; -------------------------
;; Dashboard Page


(defn latest-requests []
  (r/with-let [requests (rf/subscribe [:requests/latest])]
    [card 
     {:title [:h4 "Últimas Requisições"]
      :body (if (seq @requests)
              [list-requests requests]
              [:h6 "Não há requisições"])}]))

(defn pending-requests []
  (r/with-let [requests (rf/subscribe [:requests/pending])]
    [card
     {:title [:h4 "Requisições em aberto"]
      :body (if (seq @requests)
              [list-requests requests]
              [:h6 "Não há requisições em aberto."])}]))

(defn manage-requests-button []
  [:a.btn.btn-primary
   {:href "#/requisicoes"}
   "Gerenciar requisições"])

(defn dashboard []
  [:section.section>div.container>div.content
   [:div.row>div.col-md-12
     [latest-requests]]
   [:div.row>div.col-md-12
     [pending-requests]]
   [:div.row>div.col-md-12
     ; TODO: search requests
     [manage-requests-button] " "
     [create-request-button]]])