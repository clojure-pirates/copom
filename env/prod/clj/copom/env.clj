(ns copom.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[copom started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[copom has shut down successfully]=-"))
   :middleware identity})
