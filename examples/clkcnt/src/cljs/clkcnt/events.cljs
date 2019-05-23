(ns clkcnt.events
  (:require
   [re-frame.core :as re-frame]
   [clkcnt.db :as db]
   [clkcnt.effects :as fx]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::click
 (fn [{:keys [db] :as cofx} _]
   {::fx/send-click []}))

(re-frame/reg-event-db
 :update-click-count
 (fn [db [_ cnt]]
   {:cnt cnt}))