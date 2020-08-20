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
   [:group/created "instant" "one" "Date the group was created"]
   [:group/transaction "ref" "many" "Transactions the group"]])

(def person-schema
  [[:person/name "string" "one" "Name of the person"]
   [:person/group "ref" "many" "Group(s) a person belongs to"]])

(def transaction-schema
  [[:transaction/title "string" "one" "Title/ description of the transaction"]
   [:transaction/amount "float" "one" "Amount of the transaction"]
   [:transaction/sender "ref" "one" "Sender of the transaction"]
   [:transaction/reciever "ref" "one" "Reciever of the transaction"]
   [:transaction/group "ref" "one" "Group of the transaction"]])

(def expense-schema
  [[:expense/title "string" "one" "Title/ description of the expense"]
   [:expense/amount "float" "one" "Amount of the expense"]
   [:expense/payer "ref" "one" "Person who paid for the expense"]
   [:expense/beneficiary "ref" "many" "Person(s) who benefit from the expense"]
   [:expense/group "ref" "one" "Group of the expense"]])

;; db connection
(defn create-db-conn []
  (let [db-uri "datomic:dev://localhost:4334/test"]
    (d/delete-database db-uri)
    (d/create-database db-uri)
    (d/connect db-uri)))

(mount/defstate ^{:on-reload :noop} ;; create mount state, prevent restarting state when ns is reloaded
          db-conn :start (create-db-conn))

;; basic db functions
(defn send! [data]
  (if (instance? datomic.peer.Connection db-conn)
    @(d/transact db-conn data)
    (throw (Exception. "Datomic database not connected."))))

(defn get-db []
  (if (instance? datomic.peer.Connection db-conn)
    (d/db db-conn)
    (throw (Exception. "Datomic database not connected."))))

(defn query
  ([q] (map first (d/q q (get-db))))
  ([q input] (map first (d/q q (get-db) input))))


;; write functions (return written entities)
(defn add-user! [name group]
  (let [{:keys [db-after tempids]} (send! [{:person/name name :person/group group}])]
    (d/pull db-after '[:db/id :person/name :person/group] (val (first tempids)))))

(defn add-group! [name]
  (send! [{:group/title name
          :group/created (java.util.Date.)}]))

(defn add-transaction! [title amount sender reciever group]
  (send! [{:transaction/title title
           :transaction/amount amount
           :transaction/sender sender
           :transaction/reciever reciever
           :transaction/group group}]))

(defn add-expense! [title amount payer beneficiary group]
  (send! [{:expense/title title
           :expense/amount amount
           :expense/payer payer
           :expense/beneficiary beneficiary
           :expense/group group}]))

;; query functions
(defn get-group
  ([] (query '[:find (pull ?e [:db/id :group/title :group/created])
           :where [?e :group/title]]
             ))
  ([id]
   (d/pull (get-db) '[:db/id :group/title :group/created] id)))

(defn get-user
  ([]
   (query '[:find (pull ?e [:db/id :person/name :person/group])
            :where [?e :person/name]]))
  ([id]
   (d/pull (get-db) '[:db/id :person/name :person/group] id)))


(defn get-expenses []
  (query '[:find (pull ?e [:db/id :expense/title :expense/amount :expense/sender :expense/reciever])
         :where [?e :expense/amount]]))

(defn get-users-for-group
  [group]
  (query '[:find (pull ?e [:db/id :person/name :person/group])
         :in $ ?group
         :where [?e :person/group ?group]]
       group))

(defn get-transactions-for-group
  [group]
  (query '[:find (pull ?e [:db/id
                           :transaction/title
                           :transaction/amount
                           {:transaction/sender [:db/id :person/name]}
                           {:transaction/reciever [:db/id :person/name]}])
         :in $ ?group
         :where [?e :transaction/group ?group]]
       group))

(defn get-expenses-for-group
  [group]
  (query '[:find (pull ?e [:db/id
                           :expense/title
                           :expense/amount
                           {:expense/payer [:db/id :person/name]}
                           {:expense/beneficiary [:db/id :person/name]}])
         :in $ ?group
         :where [?e :expense/group ?group]]
       group))

;; tests
(defn test-transactions [gid]
  (let [users-in-group (get-users-for-group gid)]
    (add-transaction!
     "Test-Transaction"
     12.49
     (-> users-in-group first :db/id)
     (-> users-in-group second :db/id)
     gid)
    (def transactions (get-transactions-for-group gid))
    (prn transactions)))

(defn test-expenses [gid]
  (let [users-in-group (get-users-for-group gid)]
    (add-expense!
     "Test-Expense"
     9.99
     (-> users-in-group first :db/id)
     (list (-> users-in-group second :db/id) (-> users-in-group (nth 3) :db/id))
     gid)
    (def expenses (get-expenses-for-group gid))
    (prn expenses)))

(defn test-db []
  (add-group! "Test Group")
  (let [gid (-> (query '[:find ?e :where [?e :group/title "Test Group"]])
               first)]
    (add-user! "Paul" gid)
    (add-user! "Max" gid)
    (let [users-names-in-group (query '[:find ?name
                                :in $ ?id
                                :where [?e :person/group ?id]
                                [?e :person/name ?name]]
                              gid)]
      (prn users-names-in-group)
      (when (not (subset? #{"Paul" "Max"} (set users-names-in-group)))
        (throw (Exception. "db test failed"))))

    (add-user! "Ron" gid)
    (add-user! "Liz" gid)
    (test-transactions gid)
    (test-expenses gid)))

;; INIT
;; - create client
;; - connect to client
;; - send schema

(defn send-schema []
  (send! (mapcat schemas [group-schema person-schema transaction-schema expense-schema])))

(defn init []
  (mount/start)
  (send-schema)
  (test-db))

