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

(def datasource (atom nil))

(defn connection-pool-start
  [config]
  (let [ds (connection/->pool HikariDataSource config)]
    (reset! datasource ds)
    @datasource))

(defn find-starships
  []
  (try
   (jdbc/execute! @datasource (-> (select :*)
                                  (from :starship)
                                  sql/format)
                  {:builder-fn as-unqualified-lower-maps})
   (catch Exception e (log/error e))))

(defn find-starship-by-id
  [id]
  (try
   (jdbc/execute-one! @datasource (-> (select :*)
                                      (from :starship)
                                      (where [:= :id id])
                                      sql/format)
                      {:builder-fn as-unqualified-lower-maps})
   (catch Exception e (log/error e))))

(comment

 (require '[startrek.db :as db])
 (db/find-starships)

 #_+)
