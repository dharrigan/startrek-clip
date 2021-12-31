(ns startrek.main
  {:author "David Harrigan"}
  (:require
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [juxt.clip.core :as clip])
  (:gen-class))

(set! *warn-on-reflection* true)

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

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var :unused-binding]}
(defn ^:private process
  [arguments system-config {:keys [app-config] :as system}]
  ;; process arguments here...
  (clip/stop system-config system)
  (shutdown-agents))

(defn -main
  [& args]
  (let [{{:keys [config profile]} :options :keys [arguments]} (parse-opts args cli-options)
        system-config (load-config {:config config :profile profile})
        system (clip/start system-config)]
    (.addShutdownHook
     (Runtime/getRuntime)
     (new Thread #(clip/stop system-config system)))
    (if (seq arguments)
      (process arguments system-config system) ;; application run from the command line with arguments.
      @(promise)))) ;; application run from the command line, no arguments, keep webserver running.

(comment

 ;; paste (or eval) into the repl

 (do
  (require
   '[startrek.main :as main]
   '[juxt.clip.repl :refer [start stop set-init! system]])
  (def profile :local) ;; rename "config/config-example.edn" to "config/config-local.edn" and change values to suit your setup.
  (def config (str "config/config" (when-not (= :default profile) (str "-" (name profile))) ".edn"))
  (def system-config (#'main/load-config {:config config :profile profile}))
  (set-init! (constantly system-config))
  (start)
  (def app-config (:app-config system))
  (intern (find-ns 'startrek.main) 'app-config app-config)) ; shove the value of app-config into this namespace

 (do
  (stop)
  (start))

 ,)
