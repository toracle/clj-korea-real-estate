(ns kreal.api)

(require '[clojure.core.strint :refer [<<]])
(require '[clojure.data.json :as json])
(require '[org.httpkit.client :as http])
(require '[kreal.code :as code])
(require '[kreal.cache :as cache])

(def rtms-mobile-url "http://rtmobile.molit.go.kr/app/main.jsp")

(def rtms-mobile-base-url "http://rtmobile.molit.go.kr/mobile.do")

(def rtms-url "http://rt.molit.go.kr/rtApt.do?cmd=srhLocalView")

(def api-cache-enabled true)

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

(defn api-url [kwargs]
  (let [param (build-url-param kwargs)]
    (<< "~{rtms-mobile-base-url}?~{param}")))

(defn api-get-content [kwargs]
  (let [url (api-url kwargs)
        response (http/get url)
        body (:body @response)]
    body))

(defn content-provider [kwargs]
  (if api-cache-enabled
    (if (cache/is-content-cached kwargs)
      (cache/get-cached-content kwargs)
      (cache/cache-content api-get-content kwargs))
    (api-get-content kwargs)))

(defn get-json-content [kwargs]
  (let [content (content-provider kwargs)
        json-body (json/read-str content)]
    (get json-body "jsonList")))
