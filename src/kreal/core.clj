(ns kreal
  (:gen-class))

(require '[clojure.core.strint :refer [<<]])

(require '[kreal.code :as code])
(require '[kreal.urls :as urls])

(def start-year 2006)
(def recent-bound-year 2016)

(defn build-rent-building-list [dong-list year jongryu-code-list]
  (flatten
   (map (fn [jongryu-code]
          (urls/build-building-list dong-list year jongryu-code :전월세 :지번))
        jongryu-code-list)))

(defn save-list-to-file [lst filename]
  (let
      [file (java.io.File. filename)]
    (.mkdirs (.getParentFile file))
    (spit file (prn-str lst)))
  lst)

(defn save-gugun-list [sido-list]
  (let [gugun-list (urls/build-gugun-list sido-list)]
    (save-list-to-file gugun-list (<< "data/gugun-list.dat"))))

(defn save-dong-list [gugun-list]
  (let [dong-list (urls/build-dong-list gugun-list)]
    (save-list-to-file dong-list (<< "data/dong-list.dat"))))

(defn save-building-list [dong-list deal-year jongryu-code gubun-code gubun2-code]
  (let [building-list (urls/build-building-list dong-list deal-year jongryu-code gubun-code gubun2-code)]
    (save-list-to-file building-list (<< "data/building-list-~{deal-year}-~{(name jongryu-code)}-~{(name gubun-code)}.dat"))))

(defn save-whole-rent-building-list [dong-list jongryu-code-list]
  (let [building-list (build-rent-building-list dong-list 2015 jongryu-code-list)]
    (save-list-to-file building-list (<< "data/whole-rent-building-list.dat"))))

(def gugun-list (save-gugun-list code/sido-list))

(def dong-list (save-dong-list gugun-list))

(def gugun-list (read-string (slurp "data/gugun-list.dat")))

(def dong-list (read-string (slurp "data/dong-list.dat")))

(def seoul-dong-list (distinct (filter (fn [item] (= (:gugun-code item) "11620")) dong-list)))

(def building-seoul-rent-list (save-whole-rent-building-list seoul-dong-list '(:아파트 :연립-다세대 :단독-다가구)))

;; save-building-list를 year별로 할 수 있게 하고, merge-building-list를 두어서
;; 마지막 연도의 데이터만 웹에서 가져오고 이전 연도 데이터는 파일에 저장되어 있는 것을 활용하도록.
;; 가격 정보 데이터도 마찬가지.
