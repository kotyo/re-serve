(defproject re-serve "0.1.0"
  :description "re-frame like server side state handnling."
  :url "https://github.com/kotyo/re-serve"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[re-frame "0.10.6" :exclusions [org.clojure/clojure org.clojure/clojurescript reagent]]
                 [org.clojure/core.async "0.4.490"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]]}}
  :repl-options {:init-ns re-serve.core})
