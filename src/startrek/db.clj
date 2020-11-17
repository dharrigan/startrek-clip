(ns startrek.db
  (:require
   [clojure.tools.logging :as log]
   [honeysql.core :as sql]
   [honeysql.helpers :as helpers :refer [select from where]]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection])
  (:import
   [com.zaxxer.hikari HikariDataSource]))

(defn connection-pool-start
  [config]
  (connection/->pool HikariDataSource config))

(defn connection-pool-stop
  [datasource]
  (.close datasource))

(defn ^:private remove-namespace-from-column
  [m]
  (into {} (map (fn [[k v]] [(keyword (name k)) v])) m))

(def ^:private find-starships-sql
  (-> (select :*)
      (from :starship)
      sql/format))

(defn find-starships
  [{:keys [startrek-db] :as app-config}]
  (try
   (->> (jdbc/execute! startrek-db find-starships-sql)
        ;; you could do whatever you want with the results here
        ;; but keeping them as namespaced keywords is a good
        ;; and recommended approach, avoids clashes (if you are
        ;; joining multiple tables etc.,). In this simple example
        ;; since I'm not doing anything special, I'll just
        ;; remove the namespace and return as-is.
        (map remove-namespace-from-column))
   (catch Exception e (log/error e))))

(defn ^:private find-starship-by-id-sql
  [id]
  (-> (select :*)
      (from :starship)
      (where [:= :id id])
      sql/format))

(defn find-starship-by-id
  [id {:keys [startrek-db] :as app-config}]
  (try
   (-> (jdbc/execute-one! startrek-db (find-starship-by-id-sql id))
       ;; see comment above for why I remove the namespace.
       (remove-namespace-from-column))
   (catch Exception e (log/error e))))

(comment

 (require '[startrek.db :as db])

 (db/find-starships app-config)

 ,)
