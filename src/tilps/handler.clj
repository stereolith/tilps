(ns tilps.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]
            [tilps.db :as db]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (context "/api" []
           (GET "/user" []
                (response (db/get-user)))
           (GET "/user/:id" [id]
                (response (db/get-user (Long. id))))
           (GET "/group" []
                (response (db/get-group)))
           (GET "/group/:id" [id]
                (response (db/get-group (Long. id))))
  (route/not-found "Not Found")))

(def app
  (->
   app-routes
   wrap-json-response
   (wrap-defaults site-defaults)))
