(ns copom.events.utils
  (:require
    [re-frame.core :as rf]))

(def base-interceptors
  [(when ^boolean js/goog.DEBUG rf/debug)
   rf/trim-v])

(def core-superscription-keys
  [:superscription/num :superscription/complement :superscription/reference
   :superscription/city :superscription/state])

(defn superscription-coercions [m]
  (merge (select-keys m core-superscription-keys)
         {:neighborhood/id (get-in m [:superscription/neighborhood :neighborhood/id])
          :route/id (get-in m [:superscription/route :route/id])}))
