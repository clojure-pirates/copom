(ns copom.db.queries.common
  (:require
    [copom.db.queries :as q]))

(defprotocol Query
  (all [m])
  (by-id [m])
  (create! [m] "Create a row for :table according to :params. Returns the id")
  (custom [m])
  (delete! [m])
  (get-by [m])
  (get-or-create! [m] "Query :table for :params and returns it, if there's a 
                      match. Otherwise, creates a new row and returns all fields.")
  (update! [m]))
  
(extend-protocol Query
  clojure.lang.PersistentArrayMap
  (all [m] 
   (q/query [(str "SELECT * FROM " (:table m))]
            (q/default-opts (:table m))))
  (by-id [{:keys [table] :as m}]
   (let [queryf #(q/query % (q/default-opts table))]
     (-> [(str "SELECT * FROM " table " WHERE id = ?")
          (or (:id m) (get m (q/->qualified-key table :id)))]
         queryf
         first)))
  (create! [m]
   ;; return the id (int)
   (-> (q/insert! (:table m) (:params m)) first first last))
  (custom [m]
   (q/query (:query m) (q/default-opts (:table m))))
  (delete! [m]
   (q/delete! (:table m) (:where m)))
  (get-by [m]
    (let [base (str "SELECT * FROM " (:table m) " WHERE ")
          [ks vs] (reduce (fn [[ks vs] [k v]]
                            [(conj ks (str (name k) " = ?"))
                             (conj vs v)])
                          [[] []] (-> m :params q/->underline-keys))
          full-query (into [(str base (clojure.string/join " AND " ks))]
                           vs)]
      (custom {:query full-query :table (:table m)})))
  (get-or-create! [{:keys [table] :as m}]
   (if-let [result (-> m get-by first)]
     result
     (let [id (create! m)]
       (by-id {:table table
               :id id}))))
  (update! [m]
   (q/update! (:table m) (:params m) (:where m))))  

(comment
  (all {:table "user"})
  (by-id {:table "user" :user/id 1})
  (create! {:table "user", :params {:user/first-name "efra"}})
  (update! {:table "user", :params {:last-name "admin"}, :where ["id = ?" 2]})
  (delete! {:table "user", :where ["first_name = ?" "efra"]})
  (get-by {:table "user" :params {:user/first-name "user"
                                  :user/last-name "test"}})
  (get-or-create! {:table "user" :params {:user/first-name "user"
                                          :user/last-name "test"}}))