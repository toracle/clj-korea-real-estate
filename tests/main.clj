
(require '[kreal.api :as api])

(require '[kreal.parser :as parser])

(require '[kreal.cache :as cache])

(require '[kreal.code :as code])

(def test-param {:cmd "getGugunListAjax"
                 :sidoCode "11"})

(api/api-get-content test-param)

(api/content-provider test-param)

(parser/get-json-content test-param)

(cache/is-content-cached test-param)

(cache/get-cached-content test-param)

(cache/cache-content api/api-get-content test-param)

(parser/parse-gugun-list {:sido-code "11", :sido-name "서울특별시"}
                         {"GUGUN_CODE" "11680", "GUGUN_NAME" "강남구"})

(def test-param {:cmd "getDongListAjax"
                 :sidoCode "11"
                 :gugunCode "11680"})

(api/api-get-content test-param)

(api/content-provider test-param)

(parser/get-json-content test-param)

(parser/parse-dong-list {:sido-code "11", :sido-name "서울특별시",
                         :gugun-code "11680", :gugun-name "강남구"}
                        {"DONG_CODE" "1168010300", "DONG_NAME" "개포동"})



(first code/sido-list)
