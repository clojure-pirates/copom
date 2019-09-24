(ns copom.views.request
  (:require
    [clojure.string :as string]
    [copom.db :refer [app-db]]
    [copom.forms :as rff :refer [input select textarea]]
    [copom.views.components :as comps :refer 
     [card card-nav checkbox-input form-group radio-input thead]]
    [copom.views.entity :refer [entity-form-wrapper]]
    [copom.views.superscription :as sup :refer 
     [address-form delete-superscription-button]]
    [copom.router :as router]
    [copom.utils :refer [to-iso-date to-time-string request-status]]
    [reagent.core :as r]
    [re-frame.core :as rf]))
    

(defn display-datetime [s]
  (when s
    (string/join " "
      (string/split s #"T"))))

(defn list-requests [requests]
  (if-not (seq @requests)
    [:h6 "Não há requisições."]
    [:table.table.table-hover.table-striped.text-center
     [thead ["ID" "NATUREZA" "DATA/HORA OCORRÊNCIA" "STATUS" "PRIORIDADE"]]
     [:tbody
       (for [r @requests]
         ^{:key (:request/id r)}
         [:tr {:style {"cursor" "pointer"}
               :on-click #(rf/dispatch 
                            [:navigate/by-path 
                              (str "#/requisicoes/" (:request/id r) "/editar")])}
          [:td (:request/id r)]
          [:td (:request/complaint r)]
          [:td (or (display-datetime (:request/event-timestamp r)) 
                   "-")]
          [:td (request-status r)]
          [:td (:request/priority r)]])]]))
          
;; -------------------------
;; Create Request Page

(defn request-form [doc path]
  (let [delicts (rf/subscribe [:delicts/all])
        priority-score (rf/subscribe [:requests/priority-score path])
        superscription-path [:request/superscription]]
    (fn []
      (let [sid (get-in @doc (conj superscription-path :superscription/id))]
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
                            :default-value 
                            (when-not (:request/id @doc)
                              (to-iso-date (js/Date.)))}]]
                   [form-group
                    "Hora"
                    [input {:type :time
                            :class "form-control"
                            :doc doc
                            :name :request/time
                            :default-value 
                            (when-not (:request/id @doc)
                              (to-time-string (js/Date.)))}]]]}
          {:nav-title "Endereço do fato"
           :body [:fieldset
                  [:legend "Endereço" " "
                   (when-not sid
                     [:span
                       [sup/create-superscription-button
                        {:doc doc
                         :path superscription-path
                         :request/id (:request/id @doc)
                         :handler (fn [params]
                                    (rf/dispatch
                                      [:request.create-superscription/handler
                                       params]))}]])  
                   (when sid
                     [:span
                       [sup/edit-superscription-button
                        {:doc doc
                         :path superscription-path
                         :request/id (:request/id @doc)
                         :superscription/id sid
                         :handler (fn [params]
                                    (rf/dispatch
                                      [:request.edit-superscription/handler
                                       params]))}] 
                       [delete-superscription-button
                        {:handler #(rf/dispatch                         
                                     [:request.delete-superscription/handler
                                      {:doc doc
                                       :path superscription-path
                                       :request/id (:request/id @doc)
                                       :superscription/id sid}])}]])]
                  (when sid
                    [address-form {:doc doc :path [:request/superscription]}])]}
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
                                   :value "PENDING"
                                   :label "Em aberto"
                                   :checked? (when-not (:request/status @doc) true)}]
                     [radio-input {:name :request/status
                                   :class "form-check-input"
                                   :doc doc
                                   :value "DISPATCHED"
                                   :label "Despachado"}]
                     [radio-input {:name :request/status
                                   :class "form-check-input"
                                   :doc doc
                                   :value "DONE"
                                   :label "Finalizado"}]]]]}]]))))      

(defn create-request-page []
  (r/with-let [doc (r/cursor app-db [:request])
               errors (rf/subscribe [:rff/query [:requests :new :request/errors]])]
    [:section.section>div.container>div.content
     [card
      {:title [:h4 "Nova requisição"
               (when @errors [:span.alert.alert-danger @errors])
               [:div.btn-group.float-right
                 [:button.btn.btn-success
                  {:on-click #(rf/dispatch [:requests/create doc])}
                  "Criar"]
                 [:a.btn.btn-danger
                  {:href (router/href :requests)}
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
         [:a {:href (str "#/requisicoes/" (:request/id @doc) "/editar")} 
          "Request"]
         [card
          {:title [:h4 "Requisição #" (:request/id @doc)
                   (when @errors [:span.alert.alert-danger @errors])
                   " "
                   [:div.btn-group
                     [:button.btn.btn-success
                      {:on-click #(rf/dispatch [:requests/update doc])}
                      "Salvar"]
                     [:a.btn.btn-danger
                      {:href (router/href :requests)}
                      "Cancelar"]]]
           :body [request-form doc [:requests/edit]]}]]))))

; TODO: pagination (show only n requests per page)
(defn requests-page []
  (r/with-let [requests (rf/subscribe [:requests/all])
               pending-reqs (rf/subscribe [:requests/pending])
               dispatched-reqs (rf/subscribe [:requests/dispatched])
               done-reqs (rf/subscribe [:requests/done])] 
    [:section.section>div.container>div.content
     [card
      {:title [:h4 "Requisições pendentes"
               [:span.float-right 
                [create-request-button]]]
       :body [list-requests pending-reqs]}]
     [card
      {:title [:h4 "Requisições despachadas"
               [:span.float-right 
                [create-request-button]]]
       :body [list-requests dispatched-reqs]}]
     [card
      {:title [:h4 "Requisições finalizadas"
               [:span.float-right 
                [create-request-button]]]
       :body [list-requests done-reqs]}]
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

(defn not-done-requests []
  (r/with-let [requests (rf/subscribe [:requests/not-done])]
    [card
     {:title [:h4 "Requisições em aberto"]
      :body (if (seq @requests)
              [list-requests requests]
              [:h6 "Não há requisições em aberto."])}]))

(defn manage-requests-button []
  [:a.btn.btn-primary
   {:href "#/requisicoes"}
   "Gerenciar requisições"])

(defn buttons []
  [:div
   [manage-requests-button] " "
   [create-request-button]])

(defn dashboard []
  [:section.section>div.container>div.content
   [:div.row>div.col-md-12
    [buttons]]
   [:div.row>div.col-md-12
     [latest-requests]]
   [:div.row>div.col-md-12
     [not-done-requests]]
   [:div.row>div.col-md-12
    [buttons]]])
