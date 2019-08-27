(ns copom.utils
  (:require
    [clojure.string :as string]))

(defn m->upper-case 
  ([m] (m->upper-case m (keys m)))
  ([m ks]
   (reduce (fn [acc k]
             (if-let [v (get acc k)]
               (assoc acc k (string/upper-case v))
               acc))
           m ks)))

#_(m->upper-case {:a "a" :ab "ab" :c nil})