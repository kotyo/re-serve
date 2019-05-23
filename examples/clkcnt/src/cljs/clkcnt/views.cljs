(ns clkcnt.views
  (:require
   [re-frame.core :as re-frame]
   [clkcnt.subs :as subs]
   [clkcnt.events :as evts]))

(defn main-panel []
  (let [cnt (re-frame/subscribe [::subs/cnt])]
    [:div
     [:h1 "Weclome!" ]
     [:h2 "Your click count: " @cnt]
     [:button {:on-click #(re-frame/dispatch [::evts/click])} "CLICK!"]]))
