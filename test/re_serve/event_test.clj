(ns re-serve.event-test
  (:require  [clojure.test :refer :all]
             [re-serve.test-fixture :refer [sess make-fixture]]
             [re-serve.core :refer :all]))

(use-fixtures :each (make-fixture))

(deftest dispatch-single-event
  (let [p (promise)]
    (reg-event-fx ::foo (fn [_ [_ mess]] (deliver p mess) {}))
    (dispatch sess [::foo ::done])
    (is (= ::done (deref p 100 ::timeout)))))

(deftest dispatch-multiple-events
  (reg-event-db ::inc-0 (fn [db _] (update db :cnt (fnil inc 0))))
  (dispatch sess [::inc-0])
  (dispatch sess [::inc-0])
  (let [p (promise)]
    (reg-event-db ::read-0 (fn [db _] (deliver p (:cnt db)) db))
    (dispatch sess [::read-0])
    (is (= 2 (deref p 100 ::timeout)))))

(deftest doesnt-dispatch-on-stopped-session
  (let [p (promise)]
    (reg-event-fx ::foo (fn [_ [_ mess]] (deliver p mess) {}))
    (stop-session sess)
    (dispatch sess [::foo ::done])
    (is (= ::timeout (deref p 100 ::timeout)))))

(deftest dispatch-doesnt-interfere-with-other-sessions
  (let [p (promise)
        sess-2 (start-session)]
    (try
      (reg-event-db ::inc-1 (fn [db [_ mess]] (update db :cnt (fnil inc 0))))
      (reg-event-db ::read-1 (fn [db _] (deliver p (:cnt db)) db))
      (dispatch sess [::inc-1])
      (dispatch sess [::inc-1])
      (dispatch sess-2 [::inc-1])
      (dispatch sess-2 [::read-1])
      (is (= 1 (deref p 100 ::timeout)))
      (finally
        (stop-session sess-2)))))

