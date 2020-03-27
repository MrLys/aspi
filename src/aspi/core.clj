(ns aspi.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [compojure.api.sweet :refer [api GET ANY]]
            [ring.middleware.cors :refer [wrap-cors]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [aspi.handler :refer [get-current]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def routes
  [(GET (str "/api/" (:apikey env)) []
         (get-current))
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
