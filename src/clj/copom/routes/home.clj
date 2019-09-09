(ns copom.routes.home
  (:require
    [copom.layout :as layout]
    [copom.db.queries.common :as q]
    copom.routes.domain
    [copom.routes.entity :as ent]
    [copom.routes.requests :as requests]
    [copom.routes.superscription :as sup]
    [clojure.java.io :as io]
    [clojure.spec.alpha :as s]
    [copom.middleware :as middleware]
    [reitit.coercion.spec]
    [ring.util.http-response :as response]))

(defn get-delicts [req]
  (response/ok
    (q/all {:table "delict"})))

(defn home-page [request]
  (layout/render request "home.html"))

; -----------------------------------------------------------------------------
; Routes


(defn home-routes []
  [""
   {:middleware [;middleware/wrap-csrf
                 middleware/wrap-formats]
    :coercion reitit.coercion.spec/coercion}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/api"
    ["/delicts" {:get get-delicts}]
    ["/entities" {:post ent/create-entity}]
    ["/entities/names" {:get ent/get-names}]
    ["/entities/phones" {:get ent/get-phones}]
    ["/entities/{entity/id}/superscriptions"
     {:post ent/create-entity-superscription}]
    ["/entities/{entity/id}/superscriptions/{superscription/id}"
     {:post ent/create-entity-superscription
      :delete ent/delete-entity-superscription}]
    ["/neighborhoods" {:get sup/get-neighborhoods
                       :post sup/create-neighborhood}]
    ["/routes" {:get sup/get-routes
                :post sup/create-route}]
    ["/requests" {:get requests/get-requests
                  :post {;:parameters {:body :requests.new/request}
                         :handler requests/create-request}}]
    ["/requests/{request/id}" {:parameters {:path (s/keys :req [:request/id])}
                               :get requests/get-request
                               :put requests/update-request}]
    ["/requests/complaints/all" {:get requests/get-complaints}]
    ["/requests/{request/id}/superscriptions"
     {:post requests/create-request-superscription}]
    ["/requests/{request/id}/superscriptions/{superscription/id}"
     {:delete requests/delete-request-superscription}]
    ["/requests/{request/id}/entities"
     {:post requests/create-request-entity*}]
    ["/requests/{request/id}/entities/{entity/id}"
     {:post requests/create-request-entity
      :delete requests/delete-request-entity}]
    ["/requests/{request/id}/entities/{entity/id}/superscriptions/{superscription/id}"
     {:delete requests/delete-request-entity-superscription}]
    ["/requests/{request/id}/entities/{entity/id}/superscriptions"
     {:post requests/create-request-entity-superscription}]
    ["/superscriptions"
     {:post sup/create-superscription}]]])
    