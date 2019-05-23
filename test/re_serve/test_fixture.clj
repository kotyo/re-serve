(ns re-serve.test-fixture
  (:require [re-serve.core :refer [start-session stop-session]]))

(def ^:dynamic sess nil)

(defn make-fixture
  ([] (make-fixture nil))
  ([teardown-event]
   (fn [f]
     (let [session (if teardown-event
                     (start-session :teardown-event teardown-event)
                     (start-session))]
       (try
         (with-bindings {#'sess session}
           (f))
         (finally
           (stop-session session)))))))
