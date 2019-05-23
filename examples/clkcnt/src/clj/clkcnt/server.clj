(ns clkcnt.server
  (:require [org.httpkit.server :as server]
            [re-serve.core :as re-serve]))

(defonce instance (atom nil))

(re-serve/reg-event-fx
 :init-db
 (fn [_ [_ channel]] 
   {:db {:cnt 0 :channel channel}
    :send-cnt [channel 0]}))

(re-serve/reg-event-fx
 :increase-cnt
 (fn [{:keys [db] :as cofx} _]
   (let [cnt (-> db :cnt inc)]
     {:db (assoc db :cnt cnt)
      :send-cnt [(:channel db) cnt]})))

(re-serve/reg-fx
 :send-cnt
 (fn [[channel cnt]]
   (server/send! channel (pr-str {:click-cnt cnt}))))

(defn app [req]
  (server/with-channel req channel
    (let [session (re-serve/start-session)]
      (re-serve/dispatch-sync session [:init-db channel])
      (server/on-close 
       channel (fn [_] (re-serve/stop-session session)))
      (server/on-receive 
       channel #(re-serve/dispatch session [:increase-cnt %1]))))) 

; data is sent directly to the client
(defn start []
  (reset! instance (server/run-server app {:port 8321})))

(defn stop []
  (when-let [close-fn @instance]
    (close-fn :timeout 100)
    (reset! instance nil)))

(defn restart []
  (stop)
  (start))
