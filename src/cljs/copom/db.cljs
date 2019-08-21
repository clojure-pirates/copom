(ns copom.db
  (:require
    [reagent.core :as r]))

(defonce app-db (r/atom {}))