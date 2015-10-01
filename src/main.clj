(ns clj-korea-real-estate
  (:gen-class))

(require '[org.httpkit.client :as http])
(require '[clojure.java.io :as io])
(require '[clojure.core.strint :refer [<<]])
(require '[clojure.data.json :as json])

(def rtms-mobile-url "http://rtmobile.molit.go.kr/app/main.jsp")
(def rtms-url "http://rt.molit.go.kr/rtApt.do?cmd=srhLocalView")

(def jongryu-code
  {
   :아파트, 1
   :연립/다세대, 2
   :단독/다가구, 3
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

(def gugun-list
  [
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11680", :gugun-name, "강남구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11740", :gugun-name, "강동구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11305", :gugun-name, "강북구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11500", :gugun-name, "강서구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11620", :gugun-name, "관악구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11215", :gugun-name, "광진구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11530", :gugun-name, "구로구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11545", :gugun-name, "금천구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11350", :gugun-name, "노원구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11320", :gugun-name, "도봉구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11230", :gugun-name, "동대문구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11590", :gugun-name, "동작구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11440", :gugun-name, "마포구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11410", :gugun-name, "서대문구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11650", :gugun-name, "서초구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11200", :gugun-name, "성동구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11290", :gugun-name, "성북구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11710", :gugun-name, "송파구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11470", :gugun-name, "양천구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11560", :gugun-name, "영등포구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11170", :gugun-name, "용산구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11380", :gugun-name, "은평구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11110", :gugun-name, "종로구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11140", :gugun-name, "중구"}
   {:sido-code "11", :sido-name, "서울특별시" :gugun-code, "11260", :gugun-name, "중랑구"}
   ])

(defn get-dong-list-url [gugun deal-year jongryu-code gubun-code gubun2-code]
  (let
      [cmd "getDongListAjax"
       sido-code (:sido-code gugun)
       gugun-code (:gugun-code gugun)]
    (<< "http://rtmobile.molit.go.kr/mobile.do?cmd=~{cmd}&sidoCode=~{sido-code}&gugunCode=~{gugun-code}&dealYear=~{deal-year}&jongryuCode=~{jongryu-code}&gubunCode=~{gubun-code}&gubunCode2=~{gubun2-code}")))

(defn get-danji-list-url [dong deal-year jongryu-code gubun-code gubun2-code]
  (let
      [cmd "getDanjiListAjax"
       sido-code (:sido-code dong)
       gugun-code (:gugun-code dong)
       dong-code (:dong-code dong)
       view-type "LOCAL"]
    (<< "http://rtmobile.molit.go.kr/mobile.do?cmd=~{cmd}&viewType=~{view-type}&sidoCode=~{sido-code}&gugunCode=~{gugun-code}&dealYear=~{deal-year}&jongryuCode=~{jongryu-code}&gubunCode=~{gubun-code}&gubunCode2=~{gubun2-code}&dongCode=~{dong-code}")))

(defn build-dong-list [gugun-list deal-year jongryu-code gubun-code gubun2-code]
  (flatten (map (fn [gugun]
         (let [url (get-dong-list-url gugun deal-year jongryu-code gubun-code gubun2-code)
               response (http/get url)
               body (:body @response)
               json-body (json/read-str body)]
           (flatten (map (fn [dong]
                  {:sido-code (:sido-code gugun)
                   :sido-name (:sido-name gugun)
                   :gugun-code (:gugun-code gugun)
                   :gugun-name (:gugun-name gugun)
                   :dong-code (get dong "DONG_CODE")
                   :dong-name (get dong "DONG_NAME")})
                (get json-body "jsonList")))))
       gugun-list)))

(defn build-danji-list [dong-list deal-year jongryu-code gubun-code gubun2-code]
  (flatten (map (fn [dong]
         (let [url (get-danji-list-url dong deal-year jongryu-code gubun-code gubun2-code)
               response (http/get url)
               body (:body @response)
               json-body (json/read-str body)]
           (flatten (map (fn [danji]
                  {:sido-code (:sido-code dong)
                   :sido-name (:sido-name dong)
                   :gugun-code (:gugun-code dong)
                   :gugun-name (:gugun-name dong)
                   :dong-code (:dong-code dong)
                   :dong-name (:dong-name dong)
                   :danji-code (get danji "BLDG_CD")
                   :danji-name (get danji "BLDG_NM")})
                (get json-body "jsonList")))))
       dong-list)))

(defn save-list [lst filename]
  (let
      [file (java.io.File. filename)]
    (.mkdirs (.getParentFile file))
    (spit file (prn-str lst))))

(defn save-dong-list [deal-year jongryu-code gubun-code gubun2-code]
  (let [dong-list (build-dong-list gugun-list deal-year jongryu-code gubun-code gubun2-code)]
    (save-list dong-list (<< "data/dong-list-~{deal-year}-~{jongryu-code}-~{gubun-code}-~{gubun2-code}.dat"))))

(defn save-danji-list [deal-year jongryu-code gubun-code gubun2-code]
  (let [danji-list (build-danji-list dong-list gugun-list deal-year jongryu-code gubun-code gubun2-code)]
    (save-list dong-list (<< "data/danji-list-~{deal-year}-~{jongryu-code}-~{gubun-code}-~{gubun2-code}.dat"))))

(save-dong-list 2015 (:아파트 jongryu-code) (:전월세 gubun-code) (:지번 gubun2-code))
(save-danji-list 2015 (:아파트 jongryu-code) (:전월세 gubun-code) (:지번 gubun2-code))

