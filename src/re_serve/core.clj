(ns re-serve.core
  (:require [re-serve.session :as session]
            [re-serve.cofx :as cofx]
            [re-serve.fx :as fx]
            [re-frame.core :as rcore]
            [re-frame.std-interceptors :refer [db-handler->interceptor
                                               fx-handler->interceptor
                                               ctx-handler->interceptor]]
            [re-frame.events :as revents]))


;; -- API ---------------------------------------------------------------------
;;
;; This namespace represents the re-serve API
;;
;; It is heavily copies re-frame's API and under the hood
;; most of it's code is reused here.
;;
;; We wanted to follow it's API definition scheme as well.
;;
;; This library has an identical API to the re-frame library
;; with some exceptions (e.g.: subscription handling)
;; and some additions (e.g.: session handling
;;

;; -- sessions ----------------------------------------------------------------
(defn start-session
  "Starts a new re-serve session. Returns with a `session-map` where new events
  can be dispatched. Every session has it's own internal separate state-machine
  where events are dispatched to.
  Under the hood it listens on multiple core.asycn channnels and handles incoming
  messages as events.
  Optionally accepts an `options` map where a `teardown-event` could be defined. This
  event will be the last event dispatched in a case of session stop.
  The session can be stopped by calling `stop-session` fn externally or by fireing an
  effect `:stop-session`."
  [& {:keys [teardown-event] :as options}]
  (session/start-session teardown-event))

(defn stop-session
  "Stops the session, all queued events will be handled, but newly
  dispatched events are dropped. After the last event processed,
  the `teardown-event` -sepcified optionally in `start-session`- is dispatched."
  [session-map]
  (session/stop-session session-map))

;; -- dispatch ----------------------------------------------------------------
(def dispatch       session/dispatch)
(def dispatch-sync  session/dispatch-sync)

;; -- effects -----------------------------------------------------------------
(def reg-fx         fx/reg-fx)
(def clear-fx       rcore/clear-fx)  ;; think unreg-fx
(def reg-fx-session fx/reg-fx-session)

;; -- coeffects ---------------------------------------------------------------
(def reg-cofx    rcore/reg-cofx)
(def inject-cofx rcore/inject-cofx)
(def clear-cofx  rcore/clear-cofx) ;; think unreg-cofx


;; -- Events ------------------------------------------------------------------

(defn reg-event-db
  "Register the given event `handler` (function) for the given `id`. Optionally, provide
   an `interceptors` chain.
  `id` is typically a namespaced keyword  (but can be anything)
  `handler` is a function: (db event) -> db
  `interceptors` is a collection of interceptors. Will be flattened and nils removed.
  `handler` is wrapped in its own interceptor and added to the end of the interceptor
   chain, so that, in the end, only a chain is registered.
   Special effects and coeffects interceptors are added to the front of this
   chain."
  ([id handler]
    (reg-event-db id nil handler))
  ([id interceptors handler]
   (revents/register id [cofx/base-interceptor cofx/inject-db fx/do-fx interceptors
                         (db-handler->interceptor handler)])))

(defn reg-event-fx
  "Register the given event `handler` (function) for the given `id`. Optionally, provide
  an `interceptors` chain.
  `id` is typically a namespaced keyword  (but can be anything)
  `handler` is a function: (coeffects-map event-vector) -> effects-map
  `interceptors` is a collection of interceptors. Will be flattened and nils removed.
  `handler` is wrapped in its own interceptor and added to the end of the interceptor
   chain, so that, in the end, only a chain is registered.
   Special effects and coeffects interceptors are added to the front of the
   interceptor chain.  These interceptors inject the value of app-db into coeffects,
   and, later, action effects."
  ([id fx-handler]
   (reg-event-fx id nil fx-handler))
  ([id interceptors fx-handler]
   (revents/register id [cofx/base-interceptor cofx/inject-db fx/do-fx interceptors
                         (fx-handler->interceptor fx-handler)])))

(defn reg-event-ctx
  "Register the given event `handler` (function) for the given `id`. Optionally, provide
  an `interceptors` chain.
  `id` is typically a namespaced keyword  (but can be anything)
  `handler` is a function: (context-map event-vector) -> context-map

  This form of registration is almost never used. "
  ([id handler]
   (reg-event-ctx id nil handler))
  ([id interceptors handler]
   (revents/register id [cofx/base-interceptor cofx/inject-db fx/do-fx interceptors
                         (ctx-handler->interceptor handler)])))

;; -- interceptors ------------------------------------------------------------

;; Standard interceptors.
;; Detailed docs on each in std-interceptors.cljs
(def debug       rcore/debug)
(def path        rcore/path)
(def enrich      rcore/enrich)
(def trim-v      rcore/trim-v)
(def after       rcore/after)
(def on-changes  rcore/on-changes)

;; Utility functions for creating your own interceptors
;;
;;  (def my-interceptor
;;     (->interceptor                ;; used to create an interceptor
;;       :id     :my-interceptor     ;; an id - decorative only
;;       :before (fn [context]                         ;; you normally want to change :coeffects
;;                  ... use get-coeffect  and assoc-coeffect
;;                       )
;;       :after  (fn [context]                         ;; you normally want to change :effects
;;                 (let [db (get-effect context :db)]  ;; (get-in context [:effects :db])
;;                   (assoc-effect context :http-ajax {...}])))))
;;
(def ->interceptor   rcore/->interceptor)
(def get-coeffect    rcore/get-coeffect)
(def assoc-coeffect  rcore/assoc-coeffect)
(def get-effect      rcore/get-effect)
(def assoc-effect    rcore/assoc-effect)
(def enqueue         rcore/enqueue)


;; -- unit testing ------------------------------------------------------------

;; TODO: implement
;;   * make-restore-fn
;;   * purge-event-queue

;; -- Event Processing Callbacks  ---------------------------------------------

;; TODO implement
;;   * add-post-event-callback
;;   * remove-post-event-callback



