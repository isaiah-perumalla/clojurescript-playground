(defproject nqueens "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2719"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [om "0.8.0-rc1"]]

  :node-dependencies [[source-map-support "0.2.8"]]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-npm "0.4.0"]]

  :source-paths ["src" "target/classes"]

  :clean-targets ["out/nqueens" "nqueens.js" "nqueens.min.js"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {
                :output-to "nqueens.js"
                :output-dir "out"
                :optimizations :simple
                :source-map "nqueens.js.map"}}

             {:id "release"
              :source-paths ["src"]
              :compiler {
                :output-to "nqueens.min.js"
                :pretty-print false              
                :optimizations :advanced
                :preamble ["react/react.min.js"]}}]})
