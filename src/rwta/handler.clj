(ns rwta.handler
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.http.body-params :as body-params]
            [muuntaja.interceptor :as muun-intc]
            [rwta.controllers.todo :as todo-ctl]))

(defn inject-dependencies
  [dependency-key dependency-value]
  (interceptor/interceptor
   {:name ::inject-dependencies
    :enter (fn [context]
             (assoc-in context [:dependencies dependency-key]
                       dependency-value))}))

(def routes
  (route/expand-routes
   [[:todo-api
     ["/ping" {:get [`todo-ctl/ping]}]
     ["/db-info" {:get [`todo-ctl/db-info]}]
     ["/todo"
      ^:interceptors [(body-params/body-params) (muun-intc/format-interceptor)]
      {:post [`todo-ctl/save-todo!]}
      ["/:todo-id"
       {:get [`todo-ctl/get-todo]}]]]]))

(comment 
  routes
  )


