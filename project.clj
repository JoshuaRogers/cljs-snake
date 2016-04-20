(defproject cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
	               [org.clojure/clojurescript "1.8.40"]
                 [cljsjs/three "0.0.72-0"]]
	:plugins [[lein-cljsbuild "1.1.3"]]
	:cljsbuild {
		:builds [{
			:source-paths ["src"]
			:compiler {
        :output-to "resources/public/cube.js"
        :output-dir "resources/public"
        :optimizations :whitespace
        :pretty-print true
        :source-map "resources/public/cube.js.map"
			}}]})
