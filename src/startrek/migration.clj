(ns startrek.migration
  (:import
   [org.flywaydb.core Flyway]
   [org.flywaydb.core.api.configuration FluentConfiguration]))

(set! *warn-on-reflection* true)

(defn flyway
  [datasource migration-locations]
  (Flyway. (doto
             (FluentConfiguration.)
             (.dataSource datasource)
             (.locations (into-array migration-locations)))))

(defn migrate
  [datasource migration-locations]
  (.migrate (flyway datasource migration-locations)))
