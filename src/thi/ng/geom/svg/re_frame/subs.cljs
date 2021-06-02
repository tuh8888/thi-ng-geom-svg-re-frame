(ns thi.ng.geom.svg.re-frame.subs
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub ::view-box (fn [db _] (get db :view-box)))

(reg-sub ::scale (fn [db _] (get-in db [:view-box :scale])))
