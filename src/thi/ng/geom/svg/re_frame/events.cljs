(ns thi.ng.geom.svg.re-frame.events
  (:require [re-frame.core :refer [reg-event-db trim-v]
             thi.ng.geom.svg.re-frame.core :as svg-rf]))

(reg-event-db ::init-svg
  trim-v
  (fn [db [id view-box]] (assoc-in db [::svg-rf/id id] view-box)))

(defn apply-coord
  [f {x1 :x
      y1 :y}
    v2]
  (let [{x2 :x
         y2 :y}
        (if (coll? v2)
          v2
          {:x v2
           :y v2})]
    {:x (f x1 x2)
     :y (f y1 y2)}))

(reg-event-db ::translate-view
  trim-v
  (fn [{:keys [view-box]
        :as   db} [id origin point]]
    (update-in db
               [::svg-rf/id id :view-box]
               merge
               (->> origin
                    (apply-coord - point)
                    (apply-coord - view-box)))))

(reg-event-db ::zoom-view
  trim-v
  (fn [{:as db
        {:keys [scale zoom-speed]
         :as   view-box}
        :view-box} [id x point]]
    (let [new-scale  (* scale (+ 1 (* (if (pos? x) 1 -1) zoom-speed 0.1)))
          scale-frac (/ new-scale scale)
          t-point    (apply-coord +
                                  (apply-coord * point (- 1 scale-frac))
                                  (apply-coord * view-box scale-frac))]
      (-> db
          (assoc-in [::svg-rf/id id :view-box :scale] new-scale)
          (update-in [::svg-rf/id id :view-box] merge t-point)))))
