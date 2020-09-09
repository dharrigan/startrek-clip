(ns startrek.main
  (:require
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.tools.logging :as log]
   [juxt.clip.core :as clip])
  (:gen-class))

(def system nil)

(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/errorf "Uncaught exception on [%s]." (.getName thread))
     (log/errorf ex))))

(defn config
  [{:keys [filename] :as opts}]
  (-> (io/resource (or filename "config/config.edn"))
      (read-config opts)))

(def cli-options
  [["-p" "--profile PROFILE" "Profile to use (as a Clojure keyword)"
    :default :default
    :parse-fn #(keyword %)]])

(defn -main
  [& args]
  (let [{{:keys [profile]} :options} (parse-opts args cli-options)
        system-config (config {:profile profile})
        system (clip/start system-config)]
    (alter-var-root #'system (constantly system))
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
 (def filename (str "config/config" (when-not (= :default profile) (str "-" (name profile))) ".edn"))
 (def system-config (main/config {:profile profile :filename filename}))
 (set-init! #(main/config {:profile profile :filename filename}))

 ;; paste into the repl to get this show started!
 (start)

 (def app-config (:app-config system))

 #_+)
