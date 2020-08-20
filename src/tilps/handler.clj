(ns tilps.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [ring.util.response :refer [response]]
            [clojure.walk :refer [keywordize-keys]]
            [tilps.db :as db]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (context "/api" []
           (GET "/user" []
                (response (db/get-user)))
           (GET "/user/:id" [id]
                (response (db/get-user (Long. id))))
           (POST "/user" {:keys [json-params]}
                (let [{:keys [name group]} (keywordize-keys json-params)]
                   (response (db/add-user! name group))))
           (GET "/group" []
                (response (db/get-group)))
           (POST "/group" {:keys [json-params]}
                 (let [{:keys [name]} (keywordize-keys json-params)]
                   (db/add-group! name)))
           (context "/group/:id" [id]
                    (GET "/" []
                         (response (db/get-group (Long. id))))
                    (GET "/users" []
                         (response (db/get-users-for-group (Long. id))))
                    (GET "/entries" []
                         (response {:transactions (db/get-transactions-for-group (Long. id))
                                    :expenses (db/get-expenses-for-group (Long. id))}))
                    (POST "/transaction" {:keys [json-params]}
                          (let [{:keys [title amount sender reciever]} (keywordize-keys json-params)]
                            (response (db/add-transaction! title amount sender reciever (Long. id)))))
                    (POST "/expense" {:keys [json-params]}
                          (let [{:keys [title amount payer beneficiary]} (keywordize-keys json-params)]
                            (response (db/add-expense! title amount payer beneficiary (Long. id)))))))
  (route/not-found "Not Found"))

(def app
  (->
   app-routes
   wrap-json-response
   wrap-json-params
   (wrap-defaults api-defaults)))
