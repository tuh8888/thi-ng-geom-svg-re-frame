(ns thi.ng.geom.svg.re-frame.events
  (:require
   [re-frame.core :refer [reg-event-db trim-v]]
   [thi.ng.geom.svg.re-frame.core :as svg-rf]))

(reg-event-db ::init-svg
              trim-v
              (fn
                [db
                 [id
                  {:keys [x y width height scale]
                   :or {x      0
                        y      0
                        width  600
                        height 500
                        scale  1}
                   {:keys [speed increment]
                    :or   {speed     1
                           increment 0.1}}
                   :zoom}]]
                (assoc-in db
                 [::svg-rf/id id :view-box]
                 {:x      x
                  :y      y
                  :width  width
                  :height height
                  :scale  scale
                  :zoom   {:speed     speed
                           :increment increment}})))

(defn apply-coord
  [f
   {x1 :x
    y1 :y} v2]
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
              (fn [db [id origin point]]
                (let [view-box (get-in db [::svg-rf/id id :view-box])]
                  (update-in db
                             [::svg-rf/id id :view-box]
                             merge
                             (->> origin
                                  (apply-coord - point)
                                  (apply-coord - view-box))))))

(reg-event-db
 ::zoom-view
 trim-v
 (fn [db [id x point]]
   (let [{:keys                     [scale]
          {:keys [speed increment]} :zoom
          :as                       view-box}
         (get-in db [::svg-rf/id id :view-box])
         new-scale (max 1E-10
                        (* scale ((if (pos? x) + -) 1 (* speed increment))))
         scale-frac (/ new-scale scale)
         t-point (apply-coord +
                              (apply-coord * point (- 1 scale-frac))
                              (apply-coord * view-box scale-frac))]
     (-> db
         (assoc-in [::svg-rf/id id :view-box :scale] new-scale)
         (update-in [::svg-rf/id id :view-box] merge t-point)))))
