(ns kreal.urls)

(require '[clojure.core.strint :refer [<<]])
(require '[clojure.data.json :as json])
(require '[org.httpkit.client :as http])
(require '[clojure.java.io :as io])

(require '[kreal.code :as code])

(def rtms-mobile-url "http://rtmobile.molit.go.kr/app/main.jsp")
(def rtms-mobile-base-url "http://rtmobile.molit.go.kr/mobile.do")
(def rtms-url "http://rt.molit.go.kr/rtApt.do?cmd=srhLocalView")

(defn build-url-param [kwargs]
  (clojure.string/join "&"
                       (map (fn [item]
                              (let
                                  [k (key item)
                                   v (val item)
                                   v2 (cond
                                        (= k :jongryuCode) (get code/jongryu-code v)
                                        (= k :gubunCode) (get code/gubun-code v)
                                        (= k :gubunCode2) (get code/gubun2-code v)
                                        :else v)]
                                (format "%s=%s" (name k) v2)))
                            kwargs)))

(defn get-list-url [kwargs]
  (let [param (build-url-param kwargs)]
    (<< "~{rtms-mobile-base-url}?~{param}")))

(defn get-json-list [kwargs]
  (let [url (get-list-url kwargs)
        response (http/get url)
        body (:body @response)
        json-body (json/read-str body)]
    (get json-body "jsonList")))

(defn parse-gugun-list [sido gugun]
  {:sido-code (:sido-code sido)
   :sido-name (:sido-name sido)
   :gugun-code (get gugun "GUGUN_CODE")
   :gugun-name (get gugun "GUGUN_NAME")})

(defn parse-dong-list [gugun dong]
  {:sido-code (:sido-code gugun)
   :sido-name (:sido-name gugun)
   :gugun-code (:gugun-code gugun)
   :gugun-name (:gugun-name gugun)
   :dong-code (get dong "DONG_CODE")
   :dong-name (get dong "DONG_NAME")})

(defn parse-building-list [dong building jongryu-code]
  {:sido-code (:sido-code dong)
   :sido-name (:sido-name dong)
   :gugun-code (:gugun-code dong)
   :gugun-name (:gugun-name dong)
   :dong-code (:dong-code dong)
   :dong-name (:dong-name dong)
   :building-code (get building "BLDG_CD")
   :building-name (get building "BLDG_NM")
   :jongryu-code jongryu-code})

(defn build-gugun-list [sido-list]
  (flatten
   (map (fn [sido]
          (flatten
           (map (fn [gugun] (parse-gugun-list sido gugun))
                (get-json-list {:cmd "getGugunListAjax" :sidoCode (:sido-code sido)}))))
        sido-list)))

(defn build-dong-list [gugun-list]
  (flatten
   (map (fn [gugun]
          (flatten
           (map (fn [dong] (parse-dong-list gugun dong))
                (get-json-list {:cmd "getDongListAjax"
                                :sidoCode (:sido-code gugun)
                                :gugunCode (:gugun-code gugun)}))))
        gugun-list)))

(defn build-building-list [dong-list deal-year jongryu-code gubun-code gubun2-code]
  "build a building list."
  (flatten
   (map (fn [dong]
          (flatten
           (map (fn [building] (parse-building-list dong building jongryu-code))
                (get-json-list {:cmd "getDanjiListAjax"
                                :sidoCode (:sido-code dong)
                                :gugunCode (:gugun-code dong)
                                :dongCode (:dong-code dong)
                                :viewType "LOCAL"
                                :dealYear deal-year
                                :jongryuCode jongryu-code
                                :gubunCode gubun-code
                                :gubunCode2 gubun2-code}))))
        dong-list)))
