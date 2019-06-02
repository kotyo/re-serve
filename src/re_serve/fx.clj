(ns re-serve.fx
  (:require [re-frame.fx :as rfx]
            [re-frame.registrar :refer [get-handler clear-handlers]]
            [re-frame.loggers :refer [console]]
            [re-frame.interceptor  :refer [->interceptor]]
            [re-frame.trace :as trace :include-macros true]
            [re-frame.core :as rcore]
            [re-frame.events :as events]
            [clojure.core.async :as async])
  (:import [clojure.core.async.impl.channels ManyToManyChannel]))

(defn- do-fx-core
  [session [effect-key effect-value]]
  (if-let [effect-fn (get-handler rfx/kind effect-key false)]
    (effect-fn session effect-value)
    (do (console :error "re-frame: no handler registered for effect:"
                 effect-key ". Ignoring.")
        session)))

(def do-fx
  (->interceptor
   :id :re-serve-do-fx
   :after (fn re-serve-do-fx-after
            [context]
             (trace/with-trace
               {:op-type :event/do-fx}
               (assoc-in context [:coeffects :session]
                         (reduce do-fx-core
                                 (get-in context [:coeffects :session])
                                 (:effects context)))))))

;; Clearing previously registered effects
(rcore/clear-fx)

(def reg-fx-session rcore/reg-fx)

(defn reg-fx
  [id handler]
  (letfn [(fx-wrapper [session value]
            (handler value) session)]
    (reg-fx-session id fx-wrapper)))

;; Registering own effects
(reg-fx-session
 :db
 (fn [session value]
   (assoc session :db value)))

(reg-fx-session
 :dispatch-later
 (fn [session value]
   (reduce (fn [sess {:keys [ms dispatch] :as effect}]
             (if (or (empty? dispatch) (not (number? ms)))
               (do (console :error "re-serve: ignoring bad :dispatch-later value:" effect)
                   sess)
               (assoc-in sess [:event-sources (async/timeout ms)] dispatch)))
           session value)))

(reg-fx-session
 :dispatch
 (fn [{:keys [dispatch-chan] :as session}
      value]
   (if-not (vector? value)
     (console :error "re-serve: ignoring bad :dispatch value. Expected a vector, but got:"
              value)
     (async/put! dispatch-chan value))
   session))

(reg-fx-session
 :dispatch-n
 (fn [{:keys [dispatch-chan] :as session}
      value]
   (if-not (sequential? value)
     (console :error "re-serve: ignoring bad :dispatch-n value. Expected a collection, got:"
              value)
     (doseq [event (remove nil? value)]
       (async/put! dispatch-chan event)))
   session))

;; Unfortunately this fx-handler should be redefined here since we called clear-fx
;; to avoid re-registration errors

;; :deregister-event-handler
;;
;; removes a previously registered event handler. Expects either a single id (
;; typically a namespaced keyword), or a seq of ids.
;;
;; usage:
;;   {:deregister-event-handler :my-id)}
;; or:
;;   {:deregister-event-handler [:one-id :another-id]}
;;
(reg-fx
  :deregister-event-handler
  (fn [value]
    (let [clear-event (partial clear-handlers events/kind)]
      (if (sequential? value)
        (doseq [event value] (clear-event event))
        (clear-event value)))))

;; Additional built-in effect handlers

(reg-fx-session
 :attach
 (fn [session value-map]
   (reduce (fn [sess {:keys [chan dispatch] :as effect}]
             (if (or (empty? dispatch) (not (instance? ManyToManyChannel chan)))
               (do (console :error "re-serve: Ignoring bad attach value:" effect)
                   sess)
               (do (when (get-in sess [:event-sources chan])
                     (console :error "re-serve Reattaching event handler for channel:" effect))
                   (assoc-in sess [:event-sources chan] dispatch))))
           session value-map)))

(reg-fx-session
 :detach
 (fn [session value]
   (reduce (fn [sess chan]
             (update-in sess [:event-sources] dissoc chan))
           session value)))

(reg-fx-session
 :stop-session
 (fn [{:keys [dispatch-chan] :as session}
      vale]
   (async/close! dispatch-chan)
   session))
