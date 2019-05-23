(ns clkcnt.core
  (:require [figwheel-sidecar.repl-api :as fw]
            [clkcnt.server :as server]))

(defn figwheel-start []
  (fw/start-figwheel!)
  (fw/cljs-repl))

(defn server-restart []
  (server/restart))

(defn start-all []
  (server/start)
  (figwheel-start))
