(ns re-serve.cofx
  (:require [re-frame.cofx :refer [reg-cofx inject-cofx]]
            [re-frame.interceptor  :refer [->interceptor]]
            [re-frame.registrar :refer [clear-handlers]]))

(def base-interceptor
  (->interceptor
   :id :re-serve-base-interceptor
   :before (fn re-serve-base-before [{:keys [coeffects] :as context}]
             (let [event (:event coeffects)
                   session (last event)
                   new-event (subvec event 0 (dec (count event)))]
               (-> context
                   (assoc-in [:coeffects :session] session)
                   (assoc-in [:coeffects :event] new-event))))))

(clear-handlers :cofx :db)
(reg-cofx
 :db
 (fn db-coeffects-handler
   [{:keys [session] :as coeffects}]
   (assoc coeffects :db (:db session))))

(def inject-db (inject-cofx :db))
