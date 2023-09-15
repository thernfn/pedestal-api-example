(ns dev
  (:require [integrant.repl :as repl]
            [rwta.system :as system]))

(defn start []
  (set! *print-namespace-maps* false)
  (repl/set-prep! (constantly system/config))
  (repl/go))

(defn stop []
  (repl/halt))

(comment 
  (start)
  integrant.repl.state/system
  (stop)
  )
