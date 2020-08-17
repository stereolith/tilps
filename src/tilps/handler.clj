(ns tilps.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]
            [tilps.db :as db]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/users" []
       (response (map first (db/get-users))))
  (route/not-found "Not Found"))

(def app
  (->
   app-routes
   wrap-json-response
   (wrap-defaults site-defaults)))
