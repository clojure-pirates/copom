(ns copom.utils
  (:require
    [clojure.string :as string]
    [clj-time.core :as t]
    [clj-time.format :as tf]
    [clj-time.coerce :as tc]))

(defn m->upper-case 
  ([m] (m->upper-case m (keys m)))
  ([m ks]
   (reduce (fn [acc k]
             (if-let [v (get acc k)]
               (assoc acc k (string/upper-case v))
               acc))
           m ks)))

#_(m->upper-case {:a "a" :ab "ab" :c nil})

(defn str->java-date [format d]
  (when d
    (->> d
      (tf/parse (tf/formatters format))
      tc/to-long
      java.util.Date.)))

#_
(parse-date :date-hour-minute 
            (str "2019-11-11" "T" "11:11"))