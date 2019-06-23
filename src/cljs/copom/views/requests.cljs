(ns copom.views.requests
  (:require
    [clojure.string :as string]
    [copom.views.components :as comps :refer 
     [card card-nav checkbox-input form-group radio-input]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [reframe-forms.core :as rff :refer [input select textarea]]))

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

(defn list-requests [requests]
  [:ul.list-group.list-group-flush
    (for [r @requests]
      ^{:key (:request-id r)}
      [:a {:href (str "/requisicao/" (:request-id r))}
        [:li.list-group-item
          (:priority r) " | "
          (:crime r) " | "
          (:created-at r)]])])

;; -------------------------
;; Create Request Page

;; TODO: route-types sub
;; TODO: typeheads for neighborhood, route-name, city, state
(defn address-form [path]
  (r/with-let [route-types (atom #{"Rua", "Avenida", "Rodovia", "Linha", "Travessa", "Praça"})
               fields (rf/subscribe [:rff/query path])]
    [:div
     
     [form-group
      "Logradouro"
      [:div.form-row
       [:div.col
        ;; TODO: typehead
        [input {:type :text
                :class "form-control"
                :name (conj path :neighborhood)
                :placeholder "Bairro"}]]
       [:div.col-2
        [select {:name (conj path :route-type)
                 :class "form-control"}
         (for [r @route-types]
           ^{:key r}
           [:option {:value r} r])]]
       [:div.col
        ;; TODO: typehead
        [input {:type :text
                :class "form-control"
                :name (conj path :route-name)
                :placeholder "Nome do logradouro"}]]
       [:div.col
        [input {:type :text
                :class "form-control"
                :name (conj path :number)
                :placeholder "Número"}]]]]
     [:div.form-row
      [:div.col
       [form-group
        "Complemento"
        [input {:type :text
                :class "form-control"
                :name (conj path :complement)
                :placeholder "Apartamento, Quadra, Lote, etc."}]]]
      [:div.col
       [form-group
        "Ponto de referência"
        [input {:type :text
                :class "form-control"
                :name (conj path :reference)}]]]]
     ;; TODO: typehead
     [:div.form-row
      [:div.col
       [form-group
        "Município"
        [input {:type :text
                :class "form-control"
                :name (conj path :city)
                :value "Guarantã do Norte"}]]]
      [:div.col
       ;; TODO: typehead
       [form-group
        "Estado"
        [input {:type :text
                :class "form-control"
                :name (conj path :state)
                :value "Mato Grosso"}]]]]]))

(defn entity-form [path role]
  (r/with-let [docs-type ["CNH", "CNPJ" "CPF" "RG" "Passaporte"]
               fields (rf/subscribe [:rff/query path])]
    [:div
     [input {:type :hidden
             :class "form-control"
             :name (conj path :role)
             :value role}]
     [:div.form-row
      [:div.col
       [form-group
        "Nome"
        [input {:type :text
                :class "form-control"
                :name (conj path :name)}]]]
      [:div.col
       [form-group
        "Telefone"
         [input {:type :number
                 :class "form-control"
                 :name (conj path :phone)}]]]]
     [:fieldset
      [:legend "Endereço"]
      [address-form (conj path :address)]]
     [:fieldset
       [:legend "Documento de identidade"]
       [:div.form-row
        [:div.col
         [form-group
          "Tipo de documento"
          [select {:name (conj path :doc-type)
                   :class "form-control"}
           (for [doc docs-type]
             ^{:key doc}
             [:option {:value doc} doc])]]]
        [:div.col
         [form-group
          "Órgão emissor"
          [input {:type :text
                  :class "form-control"
                  :name (conj path :doc-issuer)}]]]
        [:div.col
         [form-group
          "Número do documento"
          [input {:type :text
                  :class "form-control"
                  :name (conj path :doc-number)}]]]]]
     ;; TODO: on-click expand
     [:fieldset
      [:legend "Filiação"]
      [:div.form-row
       [:div.col
        [form-group
         "Pai"
         [input {:type :text
                 :class "form-control"
                 :name (conj path :father)}]]]
       [:div.col
        [form-group
         "Mãe"
         [input {:type :text
                 :class "form-control"
                 :name (conj path :mother)}]]]]]]))

(def priority-weights
  {"apoio a policial" 4
   "ofensa à vida" 5
   "ofensa à integridade física" 2
   "ofensa à honra" 1
   "arma de fogo" 4
   "tráfico de drogas" 3
   "uso, porte de drogas" 2})

(defn priority-total [priorities]
  (-> priorities
      (filter (fn [[desc bool] bool]))))


(defn request-form []
  (r/with-let [path [:requests :new]
               fields (rf/subscribe [:rff/query path])
               priority-score (rf/subscribe [:requests/priority-score])]
    [card-nav
     [{:nav-title "Fato"
       :body ;; NOTE: use a select typehead? But where would I find all
             ;; the items?
             [:div
               [form-group
                "Natureza"
                 [input {:type :text
                         :class "form-control"
                         :name (conj path :???)}]]
               [form-group
                "Resumo da requisição"
                [textarea {:name (conj path :summary)
                           :class "form-control"}]]
               [:div.form-group
                 [:label "Prioridade"
                  ": " @priority-score]
                 (for [[desc weight] priority-weights]
                   ^{:key desc}
                   [checkbox-input {:name (conj path :priority desc)
                                    :label (string/capitalize desc)}])]
               [form-group
                "Data"
                [input {:type :date
                        :class "form-control"
                        :name (conj path :date)
                        :value (to-iso-date (js/Date.))}]]
               [form-group
                "Hora"
                [input {:type :time
                        :class "form-control"
                        :name (conj path :time)
                        :default-value (to-time-string (js/Date.))}]]]}
      {:nav-title "Endereço do fato"
       :body [address-form (conj path :address)]}
      ;; typehead
      {:nav-title "Solicitante(s)"
       ; TODO: (button to add another)]}]]
       :body [entity-form (conj path :requester) :requester]}
      ;; typehead
      {:nav-title "Suspeito(s)"
       ; TODO: (button to add another)]
       :body [entity-form (conj path :suspect) :suspect]}
      ;; typehead
      {:nav-title "Testemunha(s)"
       ; TODO: (button to add another)]
       :body [entity-form (conj path :witness) :witness]}
      ;; typehead
      ;; mirror solicitante
      {:nav-title "Vítima(s)"
       ; TODO: (button to add another)]
       :body [entity-form (conj path :victim) :victim]}
      {:nav-title "Providências"
       :body [:div
              [form-group
                "Providências"
                [textarea {:name (conj path :measures)
                           :class "form-control"}]
               [form-group
                 "Status"
                 [radio-input {:name (conj path :status)
                               :class "form-check-input"
                               :value :pending
                               :label "Em aberto"
                               :checked? (when (-> @fields :status nil?) true)}]
                 [radio-input {:name (conj path :status)
                               :class "form-check-input"
                               :value :dispatched
                               :label "Despachado"}]
                 [radio-input {:name (conj path :status)
                               :class "form-check-input"
                               :value :done
                               :label "Finalizado"}]]]]}]]))      

(defn create-request-page []
  (r/with-let [fields (rf/subscribe [:rff/query [:requests :new]])]
    [:section.section>div.container>div.content
     [card
      {:title [:h4 "Nova requisição"
               [:div.btn-group.float-right
                 [:button.btn.btn-success
                  {:on-click #(rf/dispatch [:requests/create @fields])}
                  "Criar"]
                 [:button.btn.btn-danger
                  {:on-click #(do (rf/dispatch [:navigate-by-name :requests])
                                  (rf/dispatch [:requests/clear-form]))}
                  "Cancelar"]]]
       :body [request-form]}]]))

(defn create-request-button []
  [:a.btn.btn-success
   {:href "#/requisicoes/criar"}
   "Nova requisição"])

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