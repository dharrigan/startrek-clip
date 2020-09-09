(ns startrek.db
  (:require
   [clojure.tools.logging :as log]
   [honeysql.core :as sql]
   [honeysql.helpers :as helpers :refer [select from where]]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.result-set :refer [as-unqualified-lower-maps]])
  (:import
   [com.zaxxer.hikari HikariDataSource]))

(defn connection-pool-start
  [config]
  (connection/->pool HikariDataSource config))

(defn connection-pool-stop
  [datasource]
  (.close datasource))

(def find-starships-sql
  (-> (select :*)
      (from :starship)
      sql/format))

(defn find-starships
  [app-config]
  (let [{:keys [startrek-db]} app-config]
    (try
     (jdbc/execute! startrek-db find-starships-sql {:builder-fn as-unqualified-lower-maps})
     (catch Exception e (log/error e)))))

(defn find-starship-by-id-sql
  [id]
  (-> (select :*)
      (from :starship)
      (where [:= :id id])
      sql/format))

(defn find-starship-by-id
  [id app-config]
  (let [{:keys [startrek-db]} app-config]
    (try
     (jdbc/execute-one! startrek-db (find-starship-by-id-sql id) {:builder-fn as-unqualified-lower-maps})
     (catch Exception e (log/error e)))))

(comment

 (require '[startrek.db :as db])

 (db/find-starships app-config)

 #_+)
