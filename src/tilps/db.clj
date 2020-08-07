(ns tilps.db
  (:require [datomic.client.api :as d]
            [java-time :as jt]))

(jt/instant)
;; config for peer server


;; INIT
;; - create client
;; - connect to client
(def db-conn (atom {}))
(defn init []
  (let [cfg {:server-type :peer-server
             :access-key "myaccesskey"
             :secret "mysecret"
             :endpoint "localhost:8998"
             :validate-hostnames false}
        client (d/client cfg)]
    (reset! db-conn (d/connect client {:db-name "tilps"}))))

;;(defn send! [data]
;;(if (nil? @db-conn)
;;    (throw (Exception. "Datomic database not connected."))
;;    (d/transact @db-conn {:tx-data data})))

(def group-schema [{:db/ident :group/title
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "Title of the group"}

                   {:db/ident :group/created
                    :db/valueType :db.type/instant
                    :db/cardinality :db.cardinality/one
                    :db/doc "Date the group was created"}])

(def person-schema [{:db/ident :person/name
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/doc "Name of a person"}])

;; tell db about new schema in a transaction with the :tx-data attribute
;;(send! (concat group-schema person-schema))


;;(defn add-user! [name]
;;  (send! {:person/name name} ))

;; define some data
(def first-movies [{:movie/title "The Goonies"
                           :movie/genre "action/adventure"
                           :movie/release-year 1985}
                          {:movie/title "Commando"
                           :movie/genre "action/adventure"
                           :movie/release-year 1985}
                          {:movie/title "Repo Man"
                           :movie/genre "punk dystopia"
                           :movie/release-year 1984}])

;; dispatch data to db in transcation witht he :tx-data attribute
;;(d/transact conn {:tx-data first-movies})


;; query data

;; retrieve current database value, which holds the current state of the database
;;(def db (d/db @db-conn))

;; define a datalog query:
;; :find specifies what you want returned, here: logic variable ?e, used in :where
;; :where (list of vectors): "bind the id of each entity that has an attribute called :movie/title to the logic variable named ?e"
(def all-movies-q '[:find ?e
                    :where [?e :movie/title]])

;; issue query
;;(d/q all-movies-q db)

(def all-titles-q '[:find ?movie-title
                    :where [_ :movie/title ?movie-title]])
;;(d/q all-titles-q db)

;; define query with jouned :where-clauses
(def titles-from-1985 '[:find ?title
                        :where [?e :movie/title ?title]
                               [?e :movie/release-year 1985]])

;;(d/q titles-from-1985 db)


(def all-data-from-1985 '[:find ?title ?year ?genre
                                 :where [?e :movie/title ?title]
                                        [?e :movie/release-year ?year]
                                        [?e :movie/genre ?genre]
                                        [?e :movie/release-year 1985]])
;;(d/q all-data-from-1985 db)
