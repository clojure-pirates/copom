(ns copom.events.utils
  (:require
    [re-frame.core :as rf]))

(def base-interceptors
  [(when ^boolean js/goog.DEBUG rf/debug)
   rf/trim-v])
