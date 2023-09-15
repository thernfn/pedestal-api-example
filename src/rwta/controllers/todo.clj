(ns rwta.controllers.todo
  (:require [next.jdbc :as jdbc]
            [ring.util.http-response :as http-response]
            [malli.core :as mc]
            [rwta.schema :as s]
            [rwta.model.todo :as db]))

(def ping 
  {:name ::ping 
   :enter 
   (fn [context]
     (let [response (http-response/ok "pong!")]
       (assoc context :response response)))})

(def db-info 
  {:name ::db-info
   :enter 
   (fn [{:keys [dependencies] :as context}]
     (let [{:keys [datasource]} dependencies
           db-response (jdbc/execute-one! 
                         datasource
                         ["SHOW SERVER_VERSION"])]
       (assoc context :response 
              (http-response/ok (str "Database server version: "
                                     (:server_version db-response))))))})

(def get-todo 
  {:name ::get-todo 
   :enter 
   (fn [{:keys [dependencies request] :as context}]
     (let [{:keys [datasource]} dependencies 
           todo-id (-> request 
                       :path-params 
                       :todo-id 
                       (parse-uuid))
           response (when todo-id (db/get-todo datasource todo-id))]
       (if response 
         (assoc context :response 
                (http-response/ok response))
         (assoc context :response 
                (http-response/not-found)))))})

(def save-todo! 
  {:name ::save-todo 
   :enter 
   (fn [{:keys [dependencies request] :as context}]
     (let [{:keys [datasource]} dependencies
           {:keys [json-params]} request]
       (if (mc/validate s/Todo json-params)
          (let [response (db/save-todo! datasource (:title json-params))]
            (assoc context :response 
                   (http-response/ok response)))
          (assoc context :response 
                 (http-response/bad-request)))))})

