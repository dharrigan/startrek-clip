(ns startrek.core
  (:require
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [juxt.clip.core :as clip])
  (:gen-class))

(def system nil)

(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/errorf "Uncaught exception on [%s]." (.getName thread))
     (log/errorf ex))))

(defn my-spy
  [message]
  (log/info message))

(add-tap my-spy)

(defn config
  [{:keys [filename] :as opts}]
  (-> (io/resource (or filename "config/config.edn"))
      (read-config opts)))

(defn -main
  [profile]
  (let [system-config (config {:profile (or (keyword profile) :local)})
        system (clip/start system-config)]
    (alter-var-root #'system (constantly system))
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. #(clip/stop system-config system))))
  @(promise))

(comment

 ;; paste into the repl
 (require
  '[startrek.core :as core]
  '[juxt.clip.repl :refer [start stop reset set-init! system]])

 ;; paste into the repl
 (set-init! #(core/config {:profile :local :filename "config/config.edn"}))

 ;; paste into the repl
 (start)

 #_+)
