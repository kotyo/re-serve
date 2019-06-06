(ns re-serve.session
  (:require [re-serve.events :as events]
            [re-frame.interceptor  :refer [->interceptor]]
            [re-frame.loggers :refer [console]]
            [clojure.core.async :as async]))

;; TODO handle exceptions

(defn- handle-event-source
  [{:keys [:event-sources] :as session} val port]
  (let [vector-prefix (get-in session [:event-sources port])]
    (-> (events/handle (into vector-prefix [val session]))
        (get-in [:coeffects :session]))))

(defn- handle-dispatch-source
  [{:keys [:event-sources] :as session} val]
  (-> (events/handle (conj val session))
      (get-in [:coeffects :session])))

(defn start-session
  [teardown-event]
  (let [dispatch-chan (async/chan)]
    (async/go-loop [session {:db {}
                             :dispatch-chan dispatch-chan
                             :event-sources {}}]
      (let [[val port] (async/alts! (conj (keys (:event-sources session)) dispatch-chan)
                                    :priority true)]
        (cond
          (not= port dispatch-chan) (recur (cond-> (handle-event-source session val port)
                                             (nil? val)
                                             (update-in [:event-sources] dissoc port)))
          (some? val) (recur (handle-dispatch-source session val))
          teardown-event (handle-dispatch-source session teardown-event))))
    {:dispatch-chan dispatch-chan}))

(defn stop-session
  [{:keys [dispatch-chan] :as session}]
  (async/close! dispatch-chan))

(defn dispatch
  [{:keys [dispatch-chan] :as session} event]
  (if (vector? event)
    (async/put! dispatch-chan event)
    (console :error "re-serve: Dispatch event should be a vector:" event)))

(defn dispatch-sync
  [{:keys [dispatch-chan] :as session} event]
  (if (vector? event)
    (async/>!! dispatch-chan event)
    (console :error "re-serve: Dispatch event should be a vector:" event)))

