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

(defn build-param-cache-key [kwargs]
  (clojure.string/join "-"
                       (map (fn [item]
                              (let [k (key item)
                                    v (val item)
                                    v2 (cond
                                         (= k :jongryuCode) (name v)
                                         (= k :gubunCode) (name v)
                                         (= k :gubunCode2) (name v)
                                         :else v)]
                                v2))
                            kwargs)))

(defn get-list-url [kwargs]
  (let [param (build-url-param kwargs)]
    (<< "~{rtms-mobile-base-url}?~{param}")))

(defn get-cache-filename [kwargs]
  (let [cache-key (build-param-cache-key kwargs)]
    (if (contains? kwargs :dealYear)
      (if (contains? kwargs :dongCode)
        (<< "cache/~{(:cmd kwargs)}/~{(:dealYear kwargs)}/~{(:dongCode kwargs)}/~{cache-key}.dat")
        (<< "cache/~{(:cmd kwargs)}/~{(:dealYear kwargs)}/~{cache-key}.dat"))
      (<< "cache/~{(:cmd kwargs)}/~{cache-key}.dat"))))

(defn cache-content [cache-filename content]
  (let
      [file (java.io.File. cache-filename)]
    (.mkdirs (.getParentFile file))
    (spit file content)
    content))

(defn get-url [kwargs]
  (let [url (get-list-url kwargs)
        response (http/get url)
        body (:body @response)]
    body))

(defn content-provider [kwargs]
  (let [cache-filename (get-cache-filename kwargs)]
    (if (.exists (clojure.java.io/as-file cache-filename))
      (slurp cache-filename)
      (cache-content cache-filename (get-url kwargs)))))

(defn get-json-list [kwargs]
  (let [content (content-provider kwargs)
        json-body (json/read-str content)]
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

(defn parse-building-info [building building-detail-json]
  {:sido-code (:sido-code building)
   :sido-name (:sido-name building)
   :gugun-code (:gugun-code building)
   :gugun-name (:gugun-name building)
   :dong-code (:dong-code building)
   :dong-name (:dong-name building)
   :building-code (:building-code building)
   :building-name (:building-name building)
   :jongryu-code (:jongryu-code building)
   :address-jibun (format "%s-%s" (get building-detail-json "BOBN") (get building-detail-json "BUBN"))
   :address (format "%s %s-%s" (get building-detail-json "NM") (get building-detail-json "BOBN") (get building-detail-json "BUBN"))})

(defn amount-to-int [s]
  (-> s
      (clojure.string/replace "," "")
      read-string))

(defn parse-deal-list [building deal deal-year]
  {:sido-code (:sido-code building)
   :sido-name (:sido-name building)
   :gugun-code (:gugun-code building)
   :gugun-name (:gugun-name building)
   :dong-code (:dong-code building)
   :dong-name (:dong-name building)
   :building-code (:building-code building)
   :building-name (:building-name building)
   :jongryu-code (:jongryu-code building)
   :deal-ym (format "%s-%s" deal-year (get deal "MM"))
   :deal-ymd (format "%s-%s-%s" deal-year (get deal "MM") (get deal "DD"))
   :right-amount (amount-to-int (get deal "RIGHT_AMT"))
   :rent-amount (amount-to-int (get deal "RENT_AMT"))
   :sum-amount (amount-to-int (get deal "SUM_AMT"))
   :area (amount-to-int (get deal "BLDG_AREA"))
   :floor (get deal "APTFNO")
   :right-gubun (get deal "RIGHT_GBN")
   :buiding-dong-name (get deal "DONG_NM")})


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


(defn build-building-info [building deal-year jongryu-code gubun-code gubun2-code]
  (first
   (get-json-list {:cmd "getDanjiInfoAjax"
                   :jongryuCode jongryu-code
                   :gubunCode gubun-code
                   :gubunCode2 gubun2-code
                   :dealYear deal-year
                   :sidoCode (:sido-code building)
                   :gugunCode (:gugun-code building)
                   :dongCode (:dong-code building)
                   :bldgCd (:building-code building)})))

(defn build-building-list [dong-list deal-year jongryu-code gubun-code gubun2-code]
  "build a building list."
  (flatten
   (map (fn [dong]
          (flatten
           (map (fn [building-json]
                  (let [building (parse-building-list dong building-json jongryu-code)]
                    (->> (-> building
                             (build-building-info deal-year jongryu-code gubun-code gubun2-code))
                         (parse-building-info building))))
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

(defn build-deal-list [building deal-year jongryu-code gubun-code gubun2-code]
  (flatten (map (fn [deal] (parse-deal-list building deal deal-year))
                (get-json-list {:cmd "getDetailListAjax"
                                :viewType "LOCAL"
                                :jongryuCode jongryu-code
                                :gubunCode gubun-code
                                :gubunCode2 gubun2-code
                                :dealYear deal-year
                                :sidoCode (:sido-code building)
                                :gugunCode (:gugun-code building)
                                :dongCode (:dong-code building)
                                :bldgCd (:building-code building)}))))
