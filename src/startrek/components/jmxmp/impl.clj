(ns startrek.components.jmxmp.impl
  {:author ["David Harrigan"]}
  (:import
   [java.lang.management ManagementFactory]
   [javax.management.remote JMXConnectorServer JMXConnectorServerFactory JMXServiceURL]))

(set! *warn-on-reflection* true)

;; CLIP Lifecycle Functions

(defn start
  "Start a JMX server on the specified `port`."
  [port]
  (doto
    (JMXConnectorServerFactory/newJMXConnectorServer
     (JMXServiceURL. "jmxmp" "0.0.0.0" port) nil (ManagementFactory/getPlatformMBeanServer))
    (.start)))

(defn stop
  "Stop the running JMX `server`."
  [^JMXConnectorServer server]
  (.stop server))
