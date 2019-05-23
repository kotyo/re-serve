(ns re-serve.events
  (:require [re-frame.events :refer [kind]]
            [re-frame.utils :refer [first-in-vector]]
            [re-frame.registrar :refer [get-handler]]
            [re-frame.loggers :refer [console]]
            [re-frame.interceptor :as interceptor]
            [re-frame.trace :as trace :include-macros true]))

(def ^:dynamic *handling* nil)    ;; remember what event we are currently handling

(defn handle
  "Given an event vector `event-v`, look up the associated interceptor chain, and execute it."
  [event-v]
  (let [event-id  (first-in-vector event-v)
        session (last event-v)]
    (if-let [interceptors  (get-handler kind event-id true)]
      (if *handling*
        (console :error "re-serve: while handling" *handling* ", dispatch-sync was called for" event-v ". You can't call dispatch-sync within an event handler.")
        (binding [*handling*  event-v]
          (trace/with-trace {:operation event-id
                             :op-type   kind
                             :tags      {:event event-v}}
            (trace/merge-trace! {:tags {:app-db-before (:db session)}})
            (let [result (interceptor/execute event-v interceptors)]
              (trace/merge-trace! {:tags {:app-db-after (:db result)}})
              result)))))))
