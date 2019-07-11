(ns copom.routes.home
  (:require
    [copom.layout :as layout]
    [copom.db.queries.common :as q]
    copom.routes.domain
    [copom.routes.requests :as requests]
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
    ["/requests" {:get requests/get-requests
                  :post {;:parameters {:body :requests.new/request}
                         :handler requests/create-request}}]
    ["/requests/{request/id}" {:get {:parameters {:path (s/keys :req [:request/id])}}
                               :handler requests/get-request}]]])