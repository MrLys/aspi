(ns aspi.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [compojure.api.sweet :refer [api GET ANY]]
            [ring.middleware.cors :refer [wrap-cors]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [aspi.handler :refer [get-current verify-params get-daily-ten-links]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def routes
  [(GET "/api/single" []
        :query-params [user :- String]
        :header-params [api-key :- String]
        (verify-params get-current user api-key))
   (GET "/api" []
             :header-params [api-key :- String]
             :query-params [user :- String]
             (verify-params get-daily-ten-links user api-key))
   (ANY "*" []
        (route/not-found (slurp (io/resource "404.html"))))])

(def app
    (->
      (api
        {}
        routes)
      (wrap-cors
        :access-control-allow-origin (re-pattern (:origin env))
        :access-control-allow-methods [:get]
        :access-control-allow-headers ["Origin" "X-Requested-With" "Content-Type" "Accept"]
        )))

(defn -main [& args]
  (run-jetty #'app {:port (Integer. (or (:port env) 5000))}))
