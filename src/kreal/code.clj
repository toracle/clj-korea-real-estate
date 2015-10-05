(ns kreal.code)

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
