(ns rwta.system
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [clojure.java.io :as io]
            [rwta.model.todo :as model]
            [rwta.handler :as handler]))

(def config
  (ig/read-string (-> "config.edn" io/resource slurp)))

(comment
  config
; {:server/run-api-server {:handler {:key :server/create-api-server}},
;  :server/create-api-server
;  {:datasource {:key :database/connection},
;   :port 7575,
;   :join? false,
;   :server-type :immutant},
;  :database/connection
;  {:dbtype "postgres",
;   :dbname "rwca",
;   :username "rwca",
;   :password "rwca"}}
) ; nil

(defmethod ig/init-key :database/connection [_ opts]
  (let [ds (model/datasource opts)
        _ (model/init-table! ds)]
    ds))

(defmethod ig/halt-key! :database/connection [_ datasource]
  (.close datasource))

(defmethod ig/init-key :server/create-api-server
  [_ {:keys [datasource port join? server-type]}]
  (-> {::http/routes handler/routes
       ::http/port port 
       ::http/join? join? 
       ::http/type server-type}
      http/default-interceptors 
      (update ::http/interceptors concat 
              [(handler/inject-dependencies :datasource datasource)])
      (http/create-server)))

(defmethod ig/init-key :server/run-api-server [_ {:keys [handler]}]
  (http/start handler))

(defmethod ig/halt-key! :server/run-api-server [_ server]
  (http/stop server))

(comment
  (def db-system (ig/init config [:database/connection]))
  db-system
  (ig/halt! db-system [:database/connection]))

(comment 
  (def system (ig/init config))
  (ig/halt! system)
  (def db-system (ig/init config [:database/connection]))
  db-system
  )
