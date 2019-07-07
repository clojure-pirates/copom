(ns copom.views.requests
  (:require
    [clojure.string :as string]
    [copom.views.components :as comps :refer 
     [card card-nav checkbox-input form-group radio-input]]
    [copom.router :as router]
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
      ^{:key (:request/id r)}
      [:a {:href (str "#/requisicao/" (:request/id r))}
        [:li.list-group-item
          (:request/priority r) " | "
          (:request/complaint r) " | "
          (:request/created-at r)]])])

;; -------------------------
;; Create Request Page

;; TODO: route-types sub
;; TODO: typeheads for neighborhood, route-name, city, state
(defn address-form [path]
  (r/with-let [route-types (atom #{"Rua", "Avenida", "Rodovia", "Linha", "Travessa", "Praça"})
               fields (rf/subscribe [:rff/query path])]
    [:div
     
     [form-group
      [:span "Logradouro"
       [:span.text-danger " *obrigatório"]]
      [:div.form-row
       [:div.col
        ;; TODO: typehead
        [input {:type :text
                :class "form-control"
                :name (conj path :neighborhood/name)
                :placeholder "Bairro"}]]
       [:div.col-2
        [select {:name (conj path :route/type)
                 :class "form-control"
                 :default-value "Rua"}
         (for [r @route-types]
           ^{:key r}
           [:option {:value r} r])]]
       [:div.col
        ;; TODO: typehead
        [input {:type :text
                :class "form-control"
                :name (conj path :route/name)
                :placeholder "Nome do logradouro"}]]
       [:div.col
        [input {:type :text
                :class "form-control"
                :name (conj path :superscription/num)
                :placeholder "Número"}]]]]
     [:div.form-row
      [:div.col
       [form-group
        "Complemento"
        [input {:type :text
                :class "form-control"
                :name (conj path :superscription/complement)
                :placeholder "Apartamento, Quadra, Lote, etc."}]]]
      [:div.col
       [form-group
        "Ponto de referência"
        [input {:type :text
                :class "form-control"
                :name (conj path :superscription/reference)}]]]]
     ;; TODO: typehead
     [:div.form-row
      [:div.col
       [form-group
        "Município"
        [input {:type :text
                :class "form-control"
                :name (conj path :superscription/city)
                :default-value "Guarantã do Norte"}]]]
      [:div.col
       ;; TODO: typehead
       [form-group
        "Estado"
        [input {:type :text
                :class "form-control"
                :name (conj path :superscription/state)
                :default-value "Mato Grosso"}]]]]]))

(defn entity-form [path role]
  (r/with-let [docs-type ["CNH", "CNPJ" "CPF" "RG" "Passaporte"]
               fields (rf/subscribe [:rff/query path])]
    [:div
     [input {:type :hidden
             :class "form-control"
             :name (conj path :entity/role)
             :default-value (name role)}]
     [:div.form-row
      [:div.col
       [form-group
        [:span "Nome"
         [:span.text-danger " *obrigatório"]]
        [input {:type :text
                :class "form-control"
                :name (conj path :entity/name)}]]]
      [:div.col
       [form-group
        [:span "Telefone"
         [:span.text-danger " *obrigatório"]]
        [input {:type :number
                :class "form-control"
                :name (conj path :entity/phone)}]]]]
     [:fieldset
      [:legend "Endereço"]
      [address-form (conj path :entity/superscription)]]
     [:fieldset
       [:legend "Documento de identidade"]
       [:div.form-row
        [:div.col
         [form-group
          "Tipo de documento"
          [select {:name (conj path :entity/doc-type)
                   :class "form-control"
                   :default-value "CPF"}
           (for [doc docs-type]
             ^{:key doc}
             [:option {:value doc} doc])]]]
        [:div.col
         [form-group
          "Órgão emissor"
          [input {:type :text
                  :class "form-control"
                  :name (conj path :entity/doc-issuer)}]]]
        [:div.col
         [form-group
          "Número do documento"
          [input {:type :text
                  :class "form-control"
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
                 :name (conj path :entity/father)}]]]
       [:div.col
        [form-group
         "Mãe"
         [input {:type :text
                 :class "form-control"
                 :name (conj path :entity/mother)}]]]]]]))

(defn request-form []
  (r/with-let [path [:requests :new]
               fields (rf/subscribe [:rff/query path])
               delicts (rf/subscribe [:delicts/all])
               priority-score (rf/subscribe [:requests/priority-score])]
    [card-nav
     [{:nav-title "Fato"
       :body ;; NOTE: use a select typehead? But where would I find all
             ;; the items?
             [:div
               ;[comps/pretty-display @fields]
               [form-group
                [:span "Natureza"
                 [:span.text-danger " *obrigatório"]]
                [input {:type :text
                        :class "form-control"
                        :name (conj path :request/complaint)}]]
               [form-group
                [:span "Resumo da requisição"
                 [:span.text-danger " *obrigatório"]]
                [textarea {:name (conj path :request/summary)
                           :class "form-control"}]]
               [:div.form-group
                 [:label "Prioridade"
                  ": " @priority-score]
                 (for [{:delict/keys [id name weight]} @delicts]
                   ^{:key id}
                   [checkbox-input {:name (conj path :request/delicts id)
                                    :label (string/capitalize name)}])]
               [form-group
                "Data"
                [input {:type :date
                        :class "form-control"
                        :name (conj path :request/date)
                        :default-value (to-iso-date (js/Date.))}]]
               [form-group
                "Hora"
                [input {:type :time
                        :class "form-control"
                        :name (conj path :request/time)
                        :default-value (to-time-string (js/Date.))}]]]}
      {:nav-title "Endereço do fato"
       :body [address-form (conj path :request/superscription)]}
      ;; typehead
      {:nav-title "Solicitante(s)"
       ; TODO: (button to add another)]}]]
       :body [entity-form (conj path :request/requester) :requester]}
      ;; typehead
      {:nav-title "Suspeito(s)"
       ; TODO: (button to add another)]
       :body [entity-form (conj path :request/suspect) :suspect]}
      ;; typehead
      {:nav-title "Testemunha(s)"
       ; TODO: (button to add another)]
       :body [entity-form (conj path :request/witness) :witness]}
      ;; typehead
      ;; mirror solicitante
      {:nav-title "Vítima(s)"
       ; TODO: (button to add another)]
       :body [entity-form (conj path :request/victim) :victim]}
      {:nav-title "Providências"
       :body [:div
              [form-group
                "Providências"
                [textarea {:name (conj path :request/measures)
                           :class "form-control"}]
               [form-group
                 [:span "Status"
                  [:span.text-danger " *obrigatório"]]
                 [radio-input {:name (conj path :request/status)
                               :class "form-check-input"
                               :value "pending"
                               :label "Em aberto"
                               :checked? (when (-> @fields :request/status nil?) true)}]
                 [radio-input {:name (conj path :request/status)
                               :class "form-check-input"
                               :value "dispatched"
                               :label "Despachado"}]
                 [radio-input {:name (conj path :request/status)
                               :class "form-check-input"
                               :value "done"
                               :label "Finalizado"}]]]]}]]))      

(defn create-request-page []
  (r/with-let [fields (rf/subscribe [:rff/query [:requests :new]])
               errors (rf/subscribe [:rff/query [:requests :new :request/errors]])]
    [:section.section>div.container>div.content
     [card
      {:title [:h4 "Nova requisição"
               (when @errors [:span.alert.alert-danger @errors])
               [:div.btn-group.float-right
                 [:button.btn.btn-success
                  {:on-click #(rf/dispatch [:requests/create @fields])}
                  "Criar"]
                 [:a.btn.btn-danger
                  {:href (router/href :requests)
                   :on-click #(rf/dispatch [:requests/clear-form])} 
                                  
                  "Cancelar"]]]
       :body [request-form]}]]))

(defn create-request-button []
  [:a.btn.btn-success
   {:href "/#/requisicoes/criar"}
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