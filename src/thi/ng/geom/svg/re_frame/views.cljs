(ns thi.ng.geom.svg.re-frame.views
  (:require [thi.ng.geom.svg.re-frame.events :as evts]
            [thi.ng.geom.svg.re-frame.subs :as subs]
            [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.svg.adapter :as adapt]
            [clojure.set :as set]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]))

(defn dragged
  [origin point e]
  (when-let [o @origin]
    (.preventDefault e)
    (rf/dispatch-sync [::evts/translate-view o point])))

(defn end-drag [origin _ _] (reset! origin nil))

(defn start-drag [origin point _] (reset! origin point))

(defn view-box-string
  [{:keys [x y width height]}]
  (apply str (interpose " " [x y width height])))

(defn zoom
  [_ point e]
  (.preventDefault e)
  (rf/dispatch-sync [::evts/zoom-view (.-deltaY e) point]))

(defn event-point
  [svg e]
  (let [point (.createSVGPoint ^js svg)]
    (set! (.-x point) (.-clientX e))
    (set! (.-y point) (.-clientY e))
    (let [t-point (.matrixTransform point (.. ^js svg getScreenCTM inverse))]
      {:x (.-x t-point)
       :y (.-y t-point)})))

(defn svg-component
  [svg-attribs scene]
  (let [origin (atom nil)]
    (letfn [(add-interaction
             [this]
             (let [dnode (rdom/dom-node this)]
               (doseq [[k f] {:mousedown  start-drag
                              :mousemove  dragged
                              :mouseup    end-drag
                              :mouseleave end-drag
                              :wheel      zoom}]
                 (.addEventListener dnode
                                    (name k)
                                    #(f origin (event-point dnode %) %)))))]
      (r/create-class
       {:component-did-mount  add-interaction
        :component-did-update add-interaction
        :reagent-render       (fn [svg-attribs scene]
                                (let [svg-attribs (assoc
                                                   svg-attribs
                                                   :view-box
                                                   (view-box-string
                                                    @(rf/subscribe
                                                      [::subs/view-box])))]
                                  (-> scene
                                      (->> (svg/svg svg-attribs)
                                           adapt/all-as-svg
                                           (adapt/inject-element-attribs
                                            adapt/key-attrib-injector))
                                      (update 1
                                              set/rename-keys
                                              {"xmlns:xlink"
                                               "xmlnsXlink"}))))}))))

(defn tspan [& args] (assoc (apply svg/text args) 0 :tspan))

(defn multi-line-text
  [pos text-attribs lines offset]
  (->> lines
       (map-indexed (fn [i line]
                      (let [pos (map #(+ %1 (* %2 i)) pos offset)]
                        (tspan pos line text-attribs))))
       (into (svg/text pos "" text-attribs))))
