(ns copom.db.queries.common
  (:require
    [copom.db.queries :as q]))

(defprotocol Query
  (all [m])
  (by-id [m])
  (create! [m])
  (update! [m])
  (delete! [m]))
  
(extend-protocol Query
  clojure.lang.PersistentArrayMap
  (all [m] 
   (q/query [(str "SELECT * FROM " (:table m))]))
  (by-id [m]
   (-> [(str "SELECT * FROM " (:table m) " WHERE id = ?") (:id m)] 
       q/query
       first))
  (create! [m]
   (q/insert! (:table m) (:params m)))
  (update! [m]
   (q/update! (:table m) (:params m) (:where m)))
  (delete! [m]
   (q/delete! (:table m) (:where m))))  

(comment
  (all {:table "users"})
  (by-id {:table "users", :id 3})
  (create! {:table "users", :params {:first-name "admin"}})
  (update! {:table "users", :params {:last-name "admin"}, :where ["id = ?" 3]})
  (delete! {:table "users", :where ["id = ?" 3]}))