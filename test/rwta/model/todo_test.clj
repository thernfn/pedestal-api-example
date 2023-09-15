(ns rwta.model.todo-test
  (:require [clojure.test :as t]
            [rwta.model.todo :as todo])
  (:import (org.testcontainers.containers PostgreSQLContainer)))

(defn- create-database-container
  []
  (doto (PostgreSQLContainer. "postgres:15.4")
    (.withDatabaseName "todo-test-db")
    (.withUsername "test")
    (.withPassword "test")))

(defn- init-db
  [config]
  (let [ds (todo/datasource config)
        _ (todo/init-table! ds)]
    ds))

(t/deftest get-todo
  (let [database-container (create-database-container)]
    (try
      (.start database-container)
      (let [datasource (init-db {:jdbcUrl (.getJdbcUrl database-container)
                                 :username (.getUsername database-container)
                                 :password (.getPassword database-container)})]
        (t/testing "insert and get todo"
          (let [title "test todo"
                {:keys [todo-id]} (todo/save-todo! datasource title)
                get-todo-result (todo/get-todo datasource todo-id)
                select-nothing-result (todo/get-todo datasource (random-uuid))]
            (t/is (= title
                     (:title get-todo-result)))
            (t/is (nil? select-nothing-result)))))
      (finally
        (.stop database-container)))))

(t/deftest save-todo!
  (let [database-container (create-database-container)]
    (try
      (.start database-container)
      (let [datasource (init-db {:jdbcUrl (.getJdbcUrl database-container)
                                 :username (.getUsername database-container)
                                 :password (.getPassword database-container)})]
        (t/testing "insert todo"
          (let [title "test todo"
                save-todo!-result (todo/save-todo! datasource title)]
            (t/is (= title
                     (:title save-todo!-result))))))
      (finally
        (.stop database-container)))))
