(ns kreal.parser)

(require '[clojure.core.strint :refer [<<]])
(require '[clojure.java.io :as io])

(require '[kreal.code :as code])
(require '[kreal.api :as api])

(defn amount-to-int [s]
  (-> s
      (clojure.string/replace "," "")
      read-string))

(defn parse-gugun-list [sido gugun]
  (assoc sido
         :gugun-code (get gugun "GUGUN_CODE")
         :gugun-name (get gugun "GUGUN_NAME")))

(defn parse-dong-list [gugun dong]
  (assoc gugun
         :dong-code (get dong "DONG_CODE")
         :dong-name (get dong "DONG_NAME")))

(defn parse-building-list [dong building jongryu-code]
  (assoc dong
         :building-code (get building "BLDG_CD")
         :building-name (get building "BLDG_NM")
         :jongryu-code jongryu-code))

(defn parse-building-info [building building-detail-json]
  (assoc building         
         :address-jibun (format "%s-%s"
                                (get building-detail-json "BOBN")
                                (get building-detail-json "BUBN"))
         :address (format "%s %s-%s"
                          (get building-detail-json "NM")
                          (get building-detail-json "BOBN")
                          (get building-detail-json "BUBN"))))

(defn parse-deal-list [building deal deal-year]
  (assoc building
         :deal-ym (format "%s-%s" deal-year (get deal "MM"))
         :deal-ymd (format "%s-%s-%s" deal-year (get deal "MM") (get deal "DD"))
         :right-amount (amount-to-int (get deal "RIGHT_AMT"))
         :rent-amount (amount-to-int (get deal "RENT_AMT"))
         :sum-amount (amount-to-int (get deal "SUM_AMT"))
         :area (amount-to-int (get deal "BLDG_AREA"))
         :floor (get deal "APTFNO")
         :right-gubun (get deal "RIGHT_GBN")
         :buiding-dong-name (get deal "DONG_NM")))

(defn build-gugun-list [sido-list]
  (mapcat (fn [sido]
            (mapcat (fn [gugun] (parse-gugun-list sido gugun))
                    (api/get-json-content {:cmd "getGugunListAjax" :sidoCode (:sido-code sido)})))
          sido-list))

(defn build-dong-list [gugun-list]
  (mapcat (fn [gugun]
            (mapcat (fn [dong] (parse-dong-list gugun dong))
                    (api/get-json-content {:cmd "getDongListAjax"
                                           :sidoCode (:sido-code gugun)
                                           :gugunCode (:gugun-code gugun)})))
          gugun-list))


(defn build-building-info [building deal-year jongryu-code gubun-code gubun2-code]
  (first
   (api/get-json-content {:cmd "getDanjiInfoAjax"
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
  (mapcat (fn [dong]
            (mapcat (fn [building-json]
                      (let [building (parse-building-list dong building-json jongryu-code)]
                        (->> (-> building
                                 (build-building-info deal-year jongryu-code gubun-code gubun2-code))
                             (parse-building-info building))))
                    (api/get-json-content {:cmd "getDanjiListAjax"
                                           :sidoCode (:sido-code dong)
                                           :gugunCode (:gugun-code dong)
                                           :dongCode (:dong-code dong)
                                           :viewType "LOCAL"
                                           :dealYear deal-year
                                           :jongryuCode jongryu-code
                                           :gubunCode gubun-code
                                           :gubunCode2 gubun2-code})))
          dong-list))

(defn build-deal-list [building deal-year jongryu-code gubun-code gubun2-code]
  (flatten (map (fn [deal] (parse-deal-list building deal deal-year))
                (api/get-json-content {:cmd "getDetailListAjax"
                                       :viewType "LOCAL"
                                       :jongryuCode jongryu-code
                                       :gubunCode gubun-code
                                       :gubunCode2 gubun2-code
                                       :dealYear deal-year
                                       :sidoCode (:sido-code building)
                                       :gugunCode (:gugun-code building)
                                       :dongCode (:dong-code building)
                                       :bldgCd (:building-code building)}))))
