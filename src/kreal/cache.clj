(ns kreal.cache)

(require '[clojure.core.strint :refer [<<]])

(require '[clj-time.core :as t])

(require '[clj-time.coerce :as c])

(def cache-expire-days 10)

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

(defn get-cache-filename [kwargs]
  (let [cache-key (build-param-cache-key kwargs)]
    (if (contains? kwargs :dealYear)
      (if (contains? kwargs :dongCode)
        (<< "cache/~{(:cmd kwargs)}/~{(:dealYear kwargs)}/~{(:dongCode kwargs)}/~{cache-key}.dat")
        (<< "cache/~{(:cmd kwargs)}/~{(:dealYear kwargs)}/~{cache-key}.dat"))
      (<< "cache/~{(:cmd kwargs)}/~{cache-key}.dat"))))

(defn cache-content [api-retriever kwargs]
  (let
      [cache-filename (get-cache-filename kwargs)
       file (java.io.File. cache-filename)
       content (api-retriever kwargs)]
    (.mkdirs (.getParentFile file))
    (spit file content)
    content))

(defn is-cache-fresh [kwargs]
  (let [last-modified-timestamp (-> kwargs
                                    get-cache-filename
                                    clojure.java.io/as-file
                                    .lastModified)
        last-modified (c/from-long last-modified-timestamp)
        now (t/today 0 0)
        current-year (t/year now)
        kwargs-year (:dealYear a)]
    (if (= 0 last-modified-timestamp)
      false
      (if-not kwargs-year
        false
        (or (< kwargs-year current-year)
            (and (= kwargs-year current-year)
                 (< cache-expire-days
                    (- now last-modified))))))))

(defn is-content-cached [kwargs]
  (let [file (-> kwargs
                 get-cache-filename
                 clojure.java.io/as-file)]
    (and (.exists file)
         (> (.length file) 0)
         (is-cache-fresh kwargs))))

(defn get-cached-content [kwargs]
  (-> kwargs
      get-cache-filename
      slurp))
