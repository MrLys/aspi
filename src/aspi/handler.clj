(ns aspi.handler
  (:require [ring.util.http-response :refer [ok not-found created unauthorized]]
             [clojure.core.memoize :as memo]
             [clojure.tools.logging :as log]
             [clj-http.client :as client]
             [clojure.data.json :as json]
             [clojure.walk :refer :all]
             [environ.core :refer [env]]))
             
(defn- log-data [data]
  (log/info (str "data: " data))
  data)

(def rands
  ["sing", "singing", "concert", "play", "guitar",
   "pretty", "2020", "2019", "2018", "2017", "instagram", "fest",
   "2016", "girl", "vg", "uka", "db", "vglista", "vg+lista",
   "Astrid+s", "I+do", "Favorite+part+of+me", "down+low", "oslo",
   "trondheim", "bergen", "stavanger", "musician", "norway","pizza",
   "spotify", "cute", "icons"])

(def sStrings ["s", "smeplass"])

(defn- get-current-num [i cnt]
  (log/info (str "checking for " i "\n"))
    (rand-int 5))

(defn get-current-rands [today cnt]
  (loop [x (rand-int 3) 
         s (nth sStrings (rand-int (count sStrings)))]
    (if (>= x 0)
      (recur (- x 1) (str s "+" (nth rands (rand-int (count rands) ))))
      s)))

(def get-rands-memo
  (memo/ttl get-current-rands :ttl/threshold 86400000))

(def get-current-num-memo
  (memo/ttl get-current-num :ttl/threshold 86400000)) ; less than 24 hours

(defn get-url [start-page n rs]
    (log/info (str "random words " rs))
    (log/info (str "n = " start-page))
    (str
      "https://www.googleapis.com/customsearch/v1?key="
      (env :gapikey)
      "&cx="
      (env :cx)
      "&fields=items(link,htmlTitle,image/thumbnailLink)"
      "&num=" n
      "&startPage=" start-page
      "&searchType=image"
      "&q=astrid+" rs))

(defn- search-google-for-image [start-page cnt rs]
  (client/get
    (log-data (get-url start-page cnt rs))))

(defn- get-data [cnt rs start-page]
  (search-google-for-image start-page cnt rs))
          

(defn fetcher [date cnt]
  (log/info (str "working in fetcher on " date "\n"))
  (let [rs (get-rands-memo date cnt)
        start-page (get-current-num-memo date cnt)]
    {:rwords rs
     :start-page start-page
     :data 
     (some-> (get-data cnt rs start-page)
             (:body)
             (json/read-str)
             (get "items"))}))

(def fetcher-memo  
  (memo/ttl fetcher :ttl/threshold 86400000))

(defn- build-item [item]
  {:link (get item "link") 
   :title (get item "htmlTitle")
   :thumbnailLink (get (get item "image")  "thumbnailLink")})

(defn- build-items [data]
  (if (and (not (nil? data)) (not (nil? (:data data))))
    (ok 
      (assoc data :data (map build-item (:data data))))
    (not-found {})))


(defn get-current []
  (log-data (build-items
   (fetcher-memo (java.time.LocalDate/now) 1))))

(defn get-daily-ten-links []
  (log/info "Start fetching ten daily links")
  (log-data (build-items (fetcher-memo (java.time.LocalDate/now) 10))))

;https://stackoverflow.com/questions/22116257/how-to-get-functions-name-as-string-in-clojure
(defn- func-name [f] 
  (clojure.string/replace (second (re-find #"^.+\$(.+)\@.+$" (str f))) #"\_QMARK\_" "?"))

(defn verify-params [handler id api-key]
  (log/info (str "user " id " requesting data through " (func-name handler)))
  (log/info (str api-key))
  (if (= api-key (:apikey env))
    (handler)
    (do 
      (log/info "invalid api-key")
      (not-found {:error "Not found"}))))

