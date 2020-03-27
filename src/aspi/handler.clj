(ns aspi.handler
  (:require [ring.util.http-response :refer [ok not-found created unauthorized]]
             [clojure.core.memoize :as memo]
             [clojure.java.io :as io]
             [clj-http.client :as client]
             [environ.core :refer [env]]))
             ;[clojure.tools.logging :as log]))


(defn- get-current-num [i]
  ;(log/info (str "checking for " i "\n"))
  (rand-int 98))


(def get-current-num-memo
  (memo/ttl get-current-num :ttl/threshold 86300000)) ; less than 24 hours

(def rands
  ["sing", "singing", "concert", "play",
   "pretty", "2020", "2019", "2018", "2017",
   "2016", "girl", "vg", "uka", "db", "vglista", "vg+lista",
   "Astrid+s", "I+do", "Favorite+part+of+me", "down+lo", "oslo",
   "trondheim", "bergen", "stavanger"])

(defn get-url [n]
  (str
      "https://www.googleapis.com/customsearch/v1?key="
      (env :gapikey)
      "&cx="
      (env :cx)
      "&fields=items"
      "&num=1"
      "&startPage=" n
      "&searchType=image"
      "&q=astrid+smeplass+s+" (nth rands (rand-int (- (count rands) 1)))))

(defn- search-google-for-image [n]
  (client/get
    (get-url n)))

(defn- get-data [index]
  (search-google-for-image (get-current-num-memo index)))

(defn fetcher [a]
  ;(log/info (str "working in fetcher on " a "\n"))
  (get-data a))

(defn image-fetcher [today]
  (let [data (fetcher today)]
    (if (not (nil? data))
      data
      nil)))

(def fetcher-memo
  (memo/ttl fetcher :ttl/threshold 86400000))

(defn get-current []
  (let [today (java.time.LocalDate/now)
        data (fetcher-memo today)]
    (println data)
    (if (not (nil? data))
    (ok(:body data))
    (not-found {}))))
