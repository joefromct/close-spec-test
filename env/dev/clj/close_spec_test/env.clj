(ns close-spec-test.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [close-spec-test.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[close-spec-test started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[close-spec-test has shut down successfully]=-"))
   :middleware wrap-dev})
