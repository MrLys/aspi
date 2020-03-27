(ns aspi.core
  (:gen-class)
  (:require ;[org.httpkit.server :refer :all]
            [environ.core :refer [env]]
            ;[clojure.tools.logging :as log]
            [compojure.api.sweet :refer [api context GET POST PATCH]]
            [aspi.handler :refer [get-current]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def route [(GET (str "/" (:apikey env)) []
                 (get-current))
            (ANY "*" []
                 (route/not-found (slurp (io/resource "404.html"))))])
(def app
    (api
      {}
      (context "/api" [] route)))

(defn -main [& args]
  (run-jetty #'app {:port (Integer. (or port (env :port) 5000))}))

;;(defn -main
;;  [& args]
;;  (run-server app {:port 3000})) ;; http-kit complains that it cannot parse from string to Number?



