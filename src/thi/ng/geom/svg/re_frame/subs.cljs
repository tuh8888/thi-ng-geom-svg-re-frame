(ns thi.ng.geom.svg.re-frame.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))


(reg-sub ::view-box (fn [db [_ id]] (get-in db [::svg-rf/id id :view-box])))

(reg-sub ::view-box-string
  (fn [[_ id] _] (subscribe [::view-box id]))
  (fn [{:keys [x y width height
              scale]} _]
    (apply str
      (interpose " "
        [x y (* scale width)
         (* scale height)]))))
