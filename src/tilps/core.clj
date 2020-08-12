(ns tilps.core
  (:require [tilps.db :as db]))

(defn init []
  (db/init)
  (prn "Initializing tilps"))
