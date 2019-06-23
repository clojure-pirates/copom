(ns copom.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [copom.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[copom started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[copom has shut down successfully]=-"))
   :middleware wrap-dev})
