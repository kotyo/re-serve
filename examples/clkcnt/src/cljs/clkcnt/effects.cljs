(ns clkcnt.effects
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as a :refer [<! >! chan]]
            [haslett.client :as ws]
            [haslett.format :as fmt]
            [re-frame.core :as re-frame]))

(defonce in (chan))
(defonce out (chan))

(ws/connect "ws://127.0.0.1:8321" 
            {:source in 
             :sink out})

(go
  (loop []
    (when-let [mess-str (<! in)]
      (let [mess (cljs.reader/read-string mess-str)]
        (re-frame/dispatch [:update-click-count (:click-cnt mess)]))
      (recur))))

(defn- send-click []
  (go (>! out (pr-str {:operation :click}))))

(re-frame/reg-fx
 ::send-click
 (fn [p]
   (send-click)))