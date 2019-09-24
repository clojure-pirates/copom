(ns copom.db.core
  (:require
    [clojure.java.jdbc :as jdbc]
    [cheshire.core :refer [generate-string parse-string]]
    [conman.core :as conman]
    [java-time.pre-java8 :as jt]
    [mount.core :refer [defstate]]
    [copom.db.walkable :as w]
    [copom.config :refer [env]]
    [walkable.sql-query-builder :as sqb])
  (:import org.postgresql.util.PGobject
           java.sql.Array
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           [java.sql
            BatchUpdateException
            PreparedStatement]))



(defstate ^:dynamic *db*
          :start (conman/connect! {:jdbc-url (env :database-url)})
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

(comment
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
      (jt/sql-timestamp v))))

(extend-protocol jdbc/IResultSetReadColumn
    java.sql.Timestamp
  (result-set-read-column [v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (result-set-read-column [v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (result-set-read-column [v _2 _3]
    (.toLocalTime v))
  Array
  (result-set-read-column [v _ _] (vec (.getArray v)))
  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        "jsonb" (parse-string value true)
        "citext" (str value)
        value))))

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-type clojure.lang.IPersistentVector
  jdbc/ISQLParameter
  (set-parameter [v ^java.sql.PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_) (apply str (rest type-name)))]
        (.setObject stmt idx (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt idx (to-pg-json v))))))

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
    (jt/sql-timestamp v))
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))



; ------------------------------------------------------------------------------
; Walkable
; ------------------------------------------------------------------------------


(defn raw-parser 
  [query]
  (->
    (w/pathom-parser
      {::sqb/sql-db     *db*
       ::sqb/run-query  jdbc/query
       ::sqb/floor-plan w/compiled-schema}
      query)))

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
  (parser-print 
    [{:appusers/all 
      [:appuser/id]}])
  (jdbc/query *db* ["SELECT * FROM user"])
  (->
    (parser-print [{:appusers/all [:appusers/user-id]}])
    clojure.pprint/pprint))