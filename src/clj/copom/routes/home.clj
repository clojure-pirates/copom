(ns copom.routes.home
  (:require
    [copom.layout :as layout]
    [copom.db.queries.common :as q]
    copom.routes.domain
    [copom.routes.requests :as requests]
    [clojure.java.io :as io]
    [copom.middleware :as middleware]
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
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/api"
    ["/delicts" {:get get-delicts}]
    ["/requests" {:get requests/get-requests
                  :post {:parameters {:body :requests.new/request}
                         :handler requests/create-request}}]]])