(defproject pacman "1.0.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/clojurescript "0.0-1424"]
                 [ring "1.0.2"]
                 [compojure "1.0.1"]
                 [enlive "1.0.0"]]
  :plugins [[lein-cljsbuild "0.2.2"]]
  :source-path "src/clj"
  :extra-classpath-dirs ["src/cljs-macros"]
  :cljsbuild {:builds [{:source-path "src/cljs"
                        :compiler {:output-to "resources/public/js/pacman.js"}}]})
