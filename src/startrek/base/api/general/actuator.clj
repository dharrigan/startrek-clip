(ns startrek.base.api.general.actuator
  {:author ["David Harrigan"]}
  (:require
   [startrek.base.api.general.metrics :as metrics]
   [startrek.components.database.interface :as db]))

(set! *warn-on-reflection* true)

(def ok 200)

(defn ^:private database-health-check
  [app-config]
  (if (db/health-check app-config)
    {:db {:status "UP"} :starships {:status "There's Klingons on the starboard bow, starboard bow Jim!"}}
    {:db {:status "DOWN"}}))

(defn ^:private health-check
  [app-config]
  (fn [_]
    {:status ok :body {:components (database-health-check app-config)}}))

(defn routes
  [app-config]
  ["/actuator"
   ["/health" {:get {:handler (health-check app-config)}}]
   ["/prometheus" {:get {:handler (metrics/metrics)}}]])
