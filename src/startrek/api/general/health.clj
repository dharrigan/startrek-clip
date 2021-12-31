(ns startrek.api.general.health
  {:author "David Harrigan"})

(set! *warn-on-reflection* true)

(def routes ["/ping" {:get {:handler (constantly {:status 200 :body "Pong!"})}}])
