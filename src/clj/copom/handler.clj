(ns copom.handler
  (:require
    [copom.middleware :as middleware]
    [copom.layout :refer [error-page]]
    [copom.routes.home :refer [home-routes]]
    [reitit.coercion :as coercion]
    [reitit.ring.coercion :as rrc]
    reitit.coercion.spec
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [copom.env :refer [defaults]]
    [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app
  :start
  (middleware/wrap-base
    (ring/ring-handler
      (ring/router
        [(home-routes)]
        {:data {:middleware [rrc/coerce-exceptions-middleware
                             rrc/coerce-request-middleware
                             rrc/coerce-response-middleware]}})
      (ring/routes
        (ring/create-resource-handler
          {:path "/"})
        (wrap-content-type
          (wrap-webjars (constantly nil)))
        (ring/create-default-handler
          {:not-found
           (constantly (error-page {:status 404, :title "404 - Page not found"}))
           :method-not-allowed
           (constantly (error-page {:status 405, :title "405 - Not allowed"}))
           :not-acceptable
           (constantly (error-page {:status 406, :title "406 - Not acceptable"}))})))))
