(ns rwta.model.todo
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [honey.sql :as sql]
            [next.jdbc.result-set :as rs])
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.flywaydb.core Flyway)))

(defn datasource
  "https://cljdoc.org/d/com.github.seancorfield/next.jdbc/1.3.883/doc/getting-started"
  [config]
  (let [^HikariDataSource ds (connection/->pool HikariDataSource config)]
    ;; this code initializes the pool and performs a validation check:
    (.close (jdbc/get-connection ds))
    ;; otherwise that validation check is deferred until the first connection
    ;; is requested in a regular operation
    ds))

(defn init-table!
  [datasource]
  (.migrate
   (.. (Flyway/configure)
       (dataSource datasource)
; https://www.red-gate.com/blog/database-devops/flyway-naming-patterns-matter
       (locations (into-array String ["classpath:database/migrations"]))
       (table "schema_version")
       (load))))

(comment
  (let [ds (datasource {:dbtype "postgres"
                        :dbname "rwca"
                        :username "rwca"
                        :password "rwca"})]
    (init-table! ds)
    (jdbc/execute-one! ds
                       ["SHOW SERVER_VERSION"]))
  ; {:server_version "15.4 (Debian 15.4-1.pgdg120+1)"}
  )

(defn get-todo
  [datasource todo-id]
  (let [select-query (sql/format {:select :*
                                  :from :todo
                                  :where [:= :todo-id todo-id]})
        select-result (jdbc/execute-one!
                       datasource
                       select-query
                       {:builder-fn rs/as-unqualified-kebab-maps})]
    select-result))

(defn save-todo!
  [datasource title]
  (let [insert-query (-> {:insert-into :todo
                          :columns [:title]
                          :values [[title]]
                          :returning :*}
                         sql/format)
        insert-result (jdbc/execute-one!
                       datasource
                       insert-query
                       {:builder-fn rs/as-unqualified-kebab-maps})]
    insert-result))

(comment
  (let [ds (datasource {:dbtype "postgres"
                        :dbname "rwca"
                        :username "rwca"
                        :password "rwca"})]
    (save-todo! ds "second todo"))
; {:todo-id #uuid "20c8ea69-808e-42fb-a643-2a12c57d0586",
;  :created-at #inst "2023-09-15T12:17:18.882786000-00:00",
;  :title "second todo"}

  (parse-uuid "20c8ea69-808e-42fb-a643-2a12c57d0586")
; #uuid "20c8ea69-808e-42fb-a643-2a12c57d0586"
  (parse-uuid "Hello") ; nil
  (let [ds (datasource {:dbtype "postgres"
                        :dbname "rwca"
                        :username "rwca"
                        :password "rwca"})]
    (get-todo ds (parse-uuid "20c8ea69-808e-42fb-a643-2a12c57d0586")))
; {:todo-id #uuid "20c8ea69-808e-42fb-a643-2a12c57d0586",
;  :created-at #inst "2023-09-15T12:17:18.882786000-00:00",
;  :title "second todo"}
  (let [ds (datasource {:dbtype "postgres"
                        :dbname "rwca"
                        :username "rwca"
                        :password "rwca"})]
    (get-todo ds (parse-uuid "20c8ea69-808e-42fb-a643-2a12c57d0589"))) 
  ; nil
  (when true 1)
  )

