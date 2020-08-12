(ns tilps.db
  (:require [datomic.api :as d]
            [java-time :as jt]
            [mount.core :as mount]
            [clojure.set :refer [subset?]]))

;; schemas
(defn schema
  "Build edn schema with less boilerplate"
  [ident value-type cardinality doc]
  {:db/ident ident
   :db/valueType (keyword "db.type" value-type)
   :db/cardinality (keyword "db.cardinality" cardinality)
   :db/doc doc})

(defn schemas
  "build multiple schemas"
  [in]
  (map #(apply schema %) in))

(def group-schema
  [[:group/title "string" "one" "Title of the group"]
   [:group/created "instant" "one" "Date the group was created"]])

(def person-schema
  [[:person/name "string" "one" "Name of the person"]
   [:person/group "ref" "many" "Group(s) a person belongs to"]])

(def transaction-schema
  [[:transaction/title "string" "one" "Title/ description of the transaction"]
   [:transaction/amount "float" "one" "Amount of the transaction"]
   [:transaction/sender "ref" "one" "Sender of the transaction"]
   [:transaction/reciever "ref" "one" "Reciever of the transaction"]])

(def expense-schema
  [[:expense/title "string" "one" "Title/ description of the expense"]
   [:expense/amount "float" "one" "Amount of the expense"]
   [:expense/payer "ref" "one" "Person who paid for the expense"]
   [:expanse/beneficiary "ref" "many" "Person(s) who benefit from the expense"]])

;; db connection
(defn create-db-conn []
  (let [db-uri "datomic:dev://localhost:4334/test"]
    (d/create-database db-uri)
    (d/connect db-uri)))

(mount/defstate ^{:on-reload :noop} ;; create mount state, prevent restarting state when ns is reloaded
          db-conn :start (create-db-conn))

;; basic db functions
(defn send! [data]
  (if (instance? datomic.peer.Connection db-conn)
    (d/transact db-conn data)
    (throw (Exception. "Datomic database not connected."))))

(defn get-db []
  (if (instance? datomic.peer.Connection db-conn)
    (d/db db-conn)
    (throw (Exception. "Datomic database not connected."))))

(defn query [q]
  (let [db (get-db)]
    (d/q q db)))


;; write functions
(defn add-user! [name group]
  (send! [{:person/name name
           :person/group group}]))

(defn add-group! [name]
  (send! [{:group/title name
          :group/created (java.util.Date.)}]))

(defn add-transaction! [title amount sender reciever]
  (send! [{:transaction/title title
           :transaction/amount amount
           :transaction/sender sender
           :transaction/reciever reciever}]))

(defn add-expense! [title amount payer beneficiary]
  (send! [{:expense/title title
           :expense/amount amount
           :expense/sender payer
           :expense/reciever beneficiary}]))

;; query functions
(defn get-users []
  (query '[:find (pull ?e [:db/id :person/name :person/group])
           :where [?e :person/name]
                  [?e :person/group]]))


(defn get-users-for-group
  [group]
  (d/q '[:find (pull ?e [:db/id :person/name :person/group])
         :in $ ?group
         :where [?e :person/group ?group]]
       (get-db)
       group))

;; test: create test group, add new users to group, query for members of group
(defn test-db []
  (add-group! "Test Group")
  (let [id (-> (query '[:find ?e :where [?e :group/title "Test Group"]])
               last first)]
    (add-user! "Paul" id)
    (add-user! "Max" id)
    (let [users-in-group (d/q '[:find ?name
                                :in $ ?id
                                :where [?e :person/group ?id]
                                [?e :person/name ?name]]
                              (get-db)
                              id)]
      (when (not (subset? #{["Paul"] ["Max"]} (set users-in-group)))
        (throw (Exception. "db test failed"))))))

;; INIT
;; - create client
;; - connect to client
;; - send schema
(defn init []
  (mount/start)
  (send! (mapcat schemas [group-schema person-schema transaction-schema expense-schema]))
  (test-db))



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
