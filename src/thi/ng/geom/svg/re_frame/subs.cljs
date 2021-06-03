(ns thi.ng.geom.svg.re-frame.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [thi.ng.geom.svg.re-frame.core :as svg-rf]))

(reg-sub ::view-box (fn [db [_ id]] (get-in db [::svg-rf/id id :view-box])))

(reg-sub ::view-box-string
  (fn [[_ id] _] (subscribe [::view-box id]))
  ;; Convert view-box info to string representation
  (fn [{:keys [x y width height scale]}]
    (when (and x y width height scale)
      (apply str (interpose " " [x y (* scale width) (* scale height)])))))
