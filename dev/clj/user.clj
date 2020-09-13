(ns user
  (:require
   [ring.adapter.jetty :refer (run-jetty)]
   [tilps.handler :refer (app)]
   ))

(def server (atom nil))
(def server-opts
  {:port  3000
   :join? false})

(defn ring-start
  []
  (reset! server
          (run-jetty app server-opts)))

(defn ring-stop
  []
  (.stop @server))
