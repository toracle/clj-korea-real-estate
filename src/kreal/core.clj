(ns kreal
  (:gen-class))

(require '[clojure.core.strint :refer [<<]])

(require '[kreal.code :as code])
(require '[kreal.parser :as parser])

(def start-year 2006)

(def recent-bound-year 2016)

(defn build-rent-building-list [dong-list year jongryu-code-list]
  (flatten
   (map (fn [jongryu-code]
          (parser/build-building-list dong-list year jongryu-code :전월세 :지번))
        jongryu-code-list)))

(defn save-list-to-file [lst filename]
  (let
      [file (java.io.File. filename)]
    (.mkdirs (.getParentFile file))
    (spit file (prn-str lst)))
  lst)

(defn save-gugun-list [sido-list]
  (let [gugun-list (parser/build-gugun-list sido-list)]
    (save-list-to-file gugun-list (<< "data/gugun-list.dat"))))

(defn save-dong-list [gugun-list]
  (let [dong-list (parser/build-dong-list gugun-list)]
    (save-list-to-file dong-list (<< "data/dong-list.dat"))))

(defn save-gugun-building-list [gugun gugun-dong-list deal-year jongryu-code gubun-code gubun2-code]
  (let [filename (<< "data/building-list/~{(:gugun-code gugun)}-~{(:sido-name gugun)}-~{(:gugun-name gugun)}-~{deal-year}-~{(name jongryu-code)}-~{(name gubun-code)}.dat")]
    (when-not
        (.exists (clojure.java.io/as-file filename))
      (-> (parser/build-building-list gugun-dong-list deal-year jongryu-code gubun-code gubun2-code)
          (save-list-to-file filename)))
    nil))

(defn save-gugun-all-building-list [gugun gugun-dong-list]
  (doseq [year (range start-year recent-bound-year)
          jongryu '(:아파트 :연립-다세대 :단독-다가구)
          gubun '(:전월세 :매매)]
    (save-gugun-building-list gugun gugun-dong-list year jongryu gubun :지번)))

(defn save-all-gugun-all-building-list []
  (doseq [gugun gugun-list]
    (let [gugun-dong-list (filter #(= (:gugun-code gugun)
                                      (:gugun-code %))
                                  gugun-list)]
      (save-gugun-all-building-list gugun gugun-dong-list))))


(def gugun-list (save-gugun-list code/sido-list))

(def dong-list (save-dong-list gugun-list))

(def gugun-list (read-string (slurp "data/gugun-list.dat")))

(def dong-list (read-string (slurp "data/dong-list.dat")))


(def kwanak-gu (first (filter #(= "관악구"
                                  (:gugun-name %))
                              gugun-list)))

(def kwanakgu-dong-list (filter #(= (:gugun-code kwanak-gu)
                                    (:gugun-code %))
                                dong-list))

(save-gugun-all-building-list kwanak-gu kwanakgu-dong-list)

(save-all-gugun-all-building-list)

(parser/build-deal-list '{:dong-name "남현동", :jongryu-code :아파트, :building-name "VIP(602-284)", :building-code "20034122", :sido-code "11", :dong-code "1162010300", :gugun-name "관악구", :sido-name "서울특별시", :gugun-code "11620"} 2012 :아파트 :전월세 :지번)

;; save-building-list를 year별로 할 수 있게 하고, merge-building-list를 두어서
;; 마지막 연도의 데이터만 웹에서 가져오고 이전 연도 데이터는 파일에 저장되어 있는 것을 활용하도록.
;; 가격 정보 데이터도 마찬가지.
