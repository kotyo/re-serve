(ns re-serve.fx-test
  (:require [re-serve.core :refer :all]
            [clojure.test :refer :all]
            [clojure.core.async :as async]
            [re-serve.test-fixture :refer [sess make-fixture]]))

(use-fixtures :each (make-fixture))

(deftest attach-test
  (let [c (async/chan)
        res (atom [])
        finished (promise)]
    (reg-event-fx :init (fn [_ _] {:attach [{:chan c :dispatch [:mess]}]}))
    (reg-event-fx :mess (fn [_ [_ m]]
                          (swap! res conj m)
                          (when (nil? m) (deliver finished true))
                          {}))
    (dispatch-sync sess [:init])
    (async/>!! c 13)
    (async/>!! c 14)
    (async/close! c)
    (is (deref finished 250 false))
    (is (= [13 14 nil] @res))))
