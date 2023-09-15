(ns rwta.schema
  (:require [malli.core :as mc]))

;; Json input
;; {"title": "some title"}

(def Todo
  [:map {:closed true}
   [:title :string]])

(comment
  (mc/validate Todo {:title "hello"}) ; true
  (mc/validate Todo {:titl "hello"}) ; false
  
  
  )
