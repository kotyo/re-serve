(ns re-serve.session-test
  (:require [clojure.test :refer :all]
            [re-serve.test-fixture :refer [sess make-fixture]]
            [re-serve.core :refer :all]))

(use-fixtures :each (make-fixture [::teardown :done-message]))

(deftest teardown-event-is-dispatched-on-external-stop
  (let [done-p (promise)]
    (reg-event-fx
     ::teardown
     (fn [_ [_ mess]] (deliver done-p mess) {}))
    (stop-session sess)
    (is (= :done-message (deref done-p 100 :timeout)))))

(deftest teardown-event-is-dispatched-on-internal-stop
  (let [done-p (promise)]
    (reg-event-fx
     ::teardown
     (fn [_ [_ mess]] (deliver done-p mess) {}))
    (reg-event-fx
     ::trigger-stop
     (fn [_ _] {:stop-session nil}))
    (dispatch sess [::trigger-stop])
    (is (= :done-message (deref done-p 100 :timeout)))))
