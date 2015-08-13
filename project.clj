(defproject tf2-server-cam "0.1.0-SNAPSHOT"
  :description "TF2 mass server tracker"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-ssq "0.4.0"]]
  :main ^:skip-aot tf2-server-cam.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
