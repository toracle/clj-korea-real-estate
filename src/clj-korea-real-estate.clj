(ns clj-korea-real-estate
  (:gen-class))

(require '[org.httpkit.client :as http])
(require '[clojure.java.io :as io])
(require '[clojure.core.strint :refer [<<]])
(require '[clojure.data.json :as json])

(def rtms-mobile-url "http://rtmobile.molit.go.kr/app/main.jsp")
(def rtms-url "http://rt.molit.go.kr/rtApt.do?cmd=srhLocalView")
(def recent-bound-year 2016)

(def rtms-mobile-base-url "http://rtmobile.molit.go.kr/mobile.do")

(def jongryu-code
  {
   :아파트, 1
   :연립-다세대, 2
   :단독-다가구, 3
   :오피스텔, 5
   :분양/입주권, 6
   })

(def gubun-code
  {
   :매매, 1
   :전월세, 2
   })

(def gubun2-code
  {
   :지번, "LAND"
   :도로명, "ROAD"
   })

(def sido-list
  [
   {:sido-code "11", :sido-name "서울특별시"}
   {:sido-code "26", :sido-name "부산광역시"}
   {:sido-code "27", :sido-name "대구광역시"}
   {:sido-code "28", :sido-name "인천광역시"}
   {:sido-code "30", :sido-name "대전광역시"}
   {:sido-code "29", :sido-name "광주광역시"}
   {:sido-code "31", :sido-name "울산광역시"}
   {:sido-code "36", :sido-name "세종특별자치시"}
   {:sido-code "41", :sido-name "경기도"}
   {:sido-code "42", :sido-name "강원도"}
   {:sido-code "43", :sido-name "충청북도"}
   {:sido-code "44", :sido-name "충청남도"}
   {:sido-code "45", :sido-name "전라북도"}
   {:sido-code "46", :sido-name "전라남도"}
   {:sido-code "47", :sido-name "경상북도"}
   {:sido-code "48", :sido-name "경상남도"}
   {:sido-code "50", :sido-name "제주특별자치도"}
   ])

(defn build-url-param [kwargs]
  (clojure.string/join "&"
                       (map (fn [item]
                              (let
                                  [k (key item)
                                   v (val item)
                                   v2 (cond
                                        (= k :jongryuCode) (get jongryu-code v)
                                        (= k :gubunCode) (get gubun-code v)
                                        (= k :gubunCode2) (get gubun2-code v)
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

(defn build-whole-rent-building-list [dong-list jongryu-code-list]
  (distinct
   (flatten
    (map (fn [year]
           (flatten
            (map #(build-building-list seoul-dong-list year % :전월세 :지번)
                 jongryu-code-list)))
         (range 2006 recent-year)))))

(defn save-list-to-file [lst filename]
  (let
      [file (java.io.File. filename)]
    (.mkdirs (.getParentFile file))
    (spit file (prn-str lst)))
  lst)

(defn save-gugun-list [sido-list]
  (let [gugun-list (build-gugun-list sido-list)]
    (save-list-to-file gugun-list (<< "data/gugun-list.dat"))))

(defn save-dong-list [gugun-list]
  (let [dong-list (build-dong-list gugun-list)]
    (save-list-to-file dong-list (<< "data/dong-list.dat"))))

(defn save-building-list [dong-list deal-year jongryu-code gubun-code gubun2-code]
  (let [building-list (build-building-list dong-list deal-year jongryu-code gubun-code gubun2-code)]
    (save-list-to-file building-list (<< "data/building-list-~{deal-year}-~{(name jongryu-code)}-~{(name gubun-code)}.dat"))))

(defn save-whole-rent-building-list [dong-list jongryu-code-list]
  (let [building-list (build-whole-rent-building-list dong-list jongryu-code-list)]
    (save-list-to-file building-list (<< "data/whole-rent-building-list.dat"))))


(def gugun-list (save-gugun-list sido-list))
(def dong-list (save-dong-list gugun-list))
(def gugun-list (read-string (slurp "data/gugun-list.dat")))
(def dong-list (read-string (slurp "data/dong-list.dat")))
(def seoul-dong-list (filter (fn [item] (= (:gugun-code item) "11620")) dong-list))

(def building-seoul-rent-list (save-whole-rent-building-list seoul-dong-list '(:아파트 :연립-다세대 :단독-다가구)))

