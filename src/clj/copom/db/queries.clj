(ns copom.db.queries
  (:require
    [clojure.java.jdbc :as jdbc]
    [copom.db.core :as db :refer [*db*]]))

; A nicer query interface:
; Replaces underscores for dashes on the result.

(def ->dash #(-> % (.replace \_ \-) clojure.string/lower-case))

(defn ->underline* [k]
  (-> k name (.replace \- \_) keyword))

(defn ->underline [m]
  (reduce (fn [newm [k v]]
            (assoc newm (->underline* k) v))
          {} m))

(defn keyword->table-name [k]
  (-> k name (.replace \- \_)))

(defn query 
  "Takes a vector with a sql query and, optionally, an opts map and calls
  jdbc/query."
  ([q] (query q nil)) 
  ([q opts]
   (jdbc/query *db* 
               q
               (merge {:identifiers ->dash} 
                      opts))))
#_(query ["SELECT * FROM users"])

(defn insert! 
  "Takes a talbe name and a params map and makes a call to jdbc/insert!"
  [table params]
  (jdbc/insert! *db* (->underline* table) (->underline params)))

#_(insert! :users {:first-name "Efraim" :last-name "Augusto"})

(defn update!
  "Takes a table name and a params map and calls jdbc/update!"
  ([table params] 
   (update! table params nil))
  ([table params where] 
   (jdbc/update! *db* (->underline* table) (->underline params) where)))
#_(update! :users {:last-name "Augusto Gonçalves"} ["first_name = ?" "Efraim"])

(defn delete! 
  "Takes a table name and a where clause and calls jdbc/delete!"
  [table where]
  (jdbc/delete! *db* (->underline* table) where))
#_(delete! :users ["first_name = ?" "Efraim"])

(defn execute!
  "Takes a vector with an sql statement and, optionally, an opts map and calls
  jdbc/execute!"
  ([q] (execute! q nil))
  ([q opts]
   (jdbc/execute! *db* 
                  q 
                  (merge {:identifiers ->dash} 
                         opts))))