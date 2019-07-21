(ns copom.db.core
  (:require
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]
    [java-time.pre-java8 :as jt]
    [mount.core :refer [defstate]]
    [copom.db.walkable :as w]
    [copom.config :refer [env]]
    [walkable.sql-query-builder :as sqb]))


(defstate ^:dynamic *db*
          :start (conman/connect! {:jdbc-url (env :database-url)})
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")


(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Timestamp
  (result-set-read-column [v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (result-set-read-column [v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (result-set-read-column [v _2 _3]
    (.toLocalTime v)))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [v]
    (java.sql.Timestamp. (.getTime v)))
  java.time.LocalTime
  (sql-value [v]
    (jt/sql-time v))
  java.time.LocalDate
  (sql-value [v]
    (jt/sql-date v))
  java.time.LocalDateTime
  (sql-value [v]
    (jt/sql-timestamp v))
  java.time.ZonedDateTime
  (sql-value [v]
    (jt/sql-timestamp v)))

; ------------------------------------------------------------------------------
; Walkable
; ------------------------------------------------------------------------------


(defn raw-parser 
  [query]
  (w/pathom-parser
    {::sqb/sql-db     *db*
     ::sqb/run-query  jdbc/query
     ::sqb/floor-plan w/compiled-schema}
    query))

; NOTE: works only for 1 query
(defn parser [query]
  (let [result
        (-> (w/pathom-parser
              {::sqb/sql-db     *db*
               ::sqb/run-query  jdbc/query
               ::sqb/floor-plan w/compiled-schema}
              query))]
    (if (= result :com.wsscode.pathom.core/not-found)
      result
      (let [[ident v] (first result)]
        v))))

(defn parser-print [query]
  (let [result
        (-> (w/pathom-parser
              {::sqb/sql-db     *db*
               ::sqb/run-query  (fn [db & more] 
                                  (apply clojure.pprint/pprint more) (flush) 
                                  (apply jdbc/query db more))
               ::sqb/floor-plan w/compiled-schema}
              query))]
    (if (= result :com.wsscode.pathom.core/not-found)
      result
      (let [[ident v] (first result)]
        v))))

(comment
  (parser [{[:user/by-id 1] [:user/id :user/first-name]}])
  (->
    (parser-print [{:users/all [:users/user-id]}])
    clojure.pprint/pprint))