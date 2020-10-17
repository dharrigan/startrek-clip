(ns startrek.main
  (:require
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.tools.logging :as log]
   [juxt.clip.core :as clip])
  (:gen-class))

(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/errorf "Uncaught exception on [%s]." (.getName thread))
     (log/errorf ex))))

(defn ^:private load-config
  [opts]
  (-> (io/resource (:config opts))
      (read-config opts)))

(def ^:private cli-options
  [["-c" "--config FILE" "Config file, found on the classpath, to use."
    :default "config/config.edn"]
   ["-p" "--profile PROFILE" "Profile to use, i.e., local."
    :default :default
    :parse-fn #(keyword %)]])

(defn -main
  [& args]
  (let [{{:keys [config profile]} :options} (parse-opts args cli-options)
        system-config (load-config {:config config :profile profile})
        system (clip/start system-config)]
    (.addShutdownHook
     (Runtime/getRuntime)
     (new Thread #(clip/stop system-config system))))
  ;; do additional work here if required!
  @(promise))

(comment

 ;; paste into the repl

 (require
  '[startrek.main :as main]
  '[juxt.clip.repl :refer [start stop set-init! system]])
 (def profile :default)
 (def config (str "config/config" (when-not (= :default profile) (str "-" (name profile))) ".edn"))
 (def system-config (#'main/load-config {:config config :profile profile}))
 (set-init! (constantly system-config))
 (start)

 (def app-config (:app-config system))

 #_+)
