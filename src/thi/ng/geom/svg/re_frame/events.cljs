(ns thi.ng.geom.svg.re-frame.events
  (:require [re-frame.core :refer [reg-event-db trim-v]]))

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
        :as   db} [o point]]
    (update db
            :view-box
            merge
            (->> o
                 (apply-coord - point)
                 (apply-coord - view-box)))))

(reg-event-db ::zoom-view
  trim-v
  (fn [{:as db
        {:keys [orig-height orig-width scale zoom-speed]
         :as   view-box}
        :view-box} [x point]]
    (let [new-scale  (* scale (+ 1 (* (if (pos? x) 1 -1) zoom-speed 0.1)))
          scale-frac (/ new-scale scale)
          t-point    (apply-coord +
                                  (apply-coord * point (- 1 scale-frac))
                                  (apply-coord * view-box scale-frac))]
      (-> db
          (update :view-box assoc
                  :height   (* new-scale orig-height)
                  :width    (* new-scale orig-width)
                  :scale    new-scale)
          (update :view-box merge t-point)))))
