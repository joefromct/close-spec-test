(ns close-spec-test.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[close-spec-test started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[close-spec-test has shut down successfully]=-"))
   :middleware identity})
