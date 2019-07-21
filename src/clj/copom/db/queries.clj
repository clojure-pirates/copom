(ns copom.db.queries
  (:require
    [clojure.java.jdbc :as jdbc]
    [copom.db.core :as db :refer [*db*]]))

; -----------------------------------------------------------------------------
; Utils

(def ->dash #(-> % (.replace \_ \-) clojure.string/lower-case))

(defn ->underline-key [k]
  (-> k name (.replace \- \_) keyword))

(defn ->underline-keys [m]
  (reduce (fn [newm [k v]]
            (assoc newm (->underline-key k) v))
          {} m))

(defn ->qualified-key [ns k] (keyword (name ns) (name k)))

(defn ->qualified-keys [ns fields]
  (reduce (fn [new-fields [k v]]
            (assoc new-fields (keyword (name ns) (name k)) v))
          {} fields))

(defn rows->qualified-keys [ns rows]
  (map (partial ->qualified-keys ns) rows))

(defn ->simple-key [k] (keyword (name k)))

(defn ->simple-keys [m]
  (reduce (fn [new-m [k v]]
            (assoc new-m (->simple-key k) v))
          {} m))

(defn keyword->table-name [k]
  (-> k name (.replace \- \_)))

(defn default-opts [ns]
  {:identifiers (comp (partial ->qualified-key ns) ->dash)})

(defn revert-identifiers [m]
  (reduce (fn [new-m [k v]]
            (assoc new-m ((comp ->simple-key ->underline-key) k) v))
          {} m))

; -----------------------------------------------------------------------------
; Core

(defn query 
  "Takes a vector with a sql query and, optionally, an opts map and calls
  jdbc/query."
  ([q] (query q nil)) 
  ([q opts]
   (jdbc/query *db* 
               q
               (merge {:identifiers ->dash} 
                      opts))))
#_(query ["SELECT * FROM user"] (default-opts "user"))

(defn insert! 
  "Takes a talbe name and a params map and makes a call to jdbc/insert!"
  [table params]
  (jdbc/insert! *db* (->underline-key table) (->underline-keys params)))

#_(insert! :user {:user/first-name "Efraim" :user/last-name "Gonçalves"})

(defn update!
  "Takes a table name and a params map and calls jdbc/update!"
  ([table params] 
   (update! table params nil))
  ([table params where] 
   (jdbc/update! *db* (->underline-key table) (->underline-keys params) where)))
#_(update! :user {:last-name "Augusto Gonçalves"} ["first_name = ?" "Efraim"])

(defn delete! 
  "Takes a table name and a where clause and calls jdbc/delete!"
  [table where]
  (jdbc/delete! *db* (->underline-key table) where))
#_(delete! :user ["first_name = ?" "Efraim"])

(defn execute!
  "Takes a vector with an sql statement and, optionally, an opts map and calls
  jdbc/execute!"
  ([q] (execute! q nil))
  ([q opts]
   (jdbc/execute! *db* 
                  q 
                  (merge {:identifiers ->dash} 
                         opts))))

(comment 
  (in-ns 'copom.db.queries)
  
  (query ))