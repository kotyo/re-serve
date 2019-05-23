(ns clkcnt.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::cnt
 (fn [db]
   (:cnt db)))
