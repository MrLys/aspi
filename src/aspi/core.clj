(ns aspi.core
  (:gen-class)
  (:require ;[org.httpkit.server :refer :all]
            [environ.core :refer [env]]
            ;[clojure.tools.logging :as log]
            [compojure.api.sweet :refer [api context GET POST PATCH ANY]]
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
        :access-control-allow-origin #"https://grafana.tripletex.io"
        :access-control-allow-methods [:get]
        :access-control-allow-headers ["Origin" "X-Requested-With" "Content-Type" "Accept"]
        )))

(defn -main [& args]
  (run-jetty #'app {:port (Integer. (or (:port env) 5000))}))

;;(defn -main
;;  [& args]
;;  (run-server app {:port 3000})) ;; http-kit complains that it cannot parse from string to Number?



