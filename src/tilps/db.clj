(ns tilps.db
  (:require [datomic.client.api :as d]
            [java-time :as jt]
            [tilps.db :as db]))

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

;; basic db functions
(def *db-conn* (atom {}))
(defn send! [data]
  (if (nil? @*db-conn*)
    (throw (Exception. "Datomic database not connected."))
    (d/transact @*db-conn* {:tx-data data})))

(defn get-db []
  (if (nil? @*db-conn*)
    (throw (Exception. "Datomic database not connected."))
    (d/db @*db-conn*)))

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

(prn (get-users))

(defn get-users-for-group
  [group]
  (d/q '[:find (pull ?e [:db/id :person/name :person/group])
         :in $ ?group
         :where [?e :person/group ?group]]
       (get-db)
       group))

(get-users-for-group 17592186045520)

;; test: create test group, add new users to group, query for members of group
(defn test-db []
  (add-group! "Testgruppe 2")
  (let [id (-> (query '[:find ?e :where [?e :group/title "Testgruppe"]])
               last first)]
    (add-user! "Paul" id)
    (add-user! "Max" id)
    (prn id)
    (d/q '[:find ?name
           :in $ ?id
           :where [?e :person/group ?id]
                  [?e :person/name ?name]]
         (get-db)
         id)))
(test-db)
;; INIT
;; - create client
;; - connect to client
;; - send schema
(defn init []
  (let [cfg {:server-type :peer-server
             :access-key "myaccesskey"
             :secret "mysecret"
             :endpoint "localhost:8998"
             :validate-hostnames false}
        client (d/client cfg)]
    (reset! *db-conn* (d/connect client {:db-name "tilps"}))
    (send! (mapcat schemas [group-schema person-schema transaction-schema expense-schema]))
    (test-db)))





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
