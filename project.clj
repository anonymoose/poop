(defproject poop "0.1.0-SNAPSHOT"
  :description "Adoption website"
  :min-lein-version "2.0.0"
  :url ""
  :plugins [[lein-cloverage "1.0.2"]
            [lein-exec "0.3.1"]  ; http://charsequence.blogspot.in/2012/04/scripting-clojure-with-leiningen-2.html
            [lein-ring "0.8.6"]
            ]
  :ring {:handler poop.core/ring-handler
         :port 5000
         :auto-relaod? true
         :reload-paths ["src" "resources/templates"]}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.incubator "0.1.3"]
                 [clj-http-lite "0.1.0"]
                 [clj-time "0.4.5"]
                 [com.taoensso/carmine "2.4.0"]
                 [clojure-csv/clojure-csv "2.0.0-alpha2"]
                 [ring "1.2.0"]
                 [postgresql "9.1-901.jdbc4"]
                 [ring.middleware.logger "0.4.0"]  ; https://github.com/pjlegato/ring.middleware.logger
                 [korma "0.3.0-RC5"]
                 [compojure "1.1.5"]
                 [c3p0/c3p0 "0.9.1.2"]
                 [clj-http "0.7.5"]
                 [robert/hooke "1.3.0"]
                 [sandbar/sandbar "0.4.0-SNAPSHOT"]
                 [hiccup "1.0.2"]
                 [selmer "0.5.1"] ; https://github.com/yogthos/Selmer
                 [sendgrid "0.1.0"]
                 [abengoa/clj-stripe "1.0.3"]
                 [clj-time "0.6.0"]
                 [clj-json "0.5.3"]
                 [de.ubercode.clostache/clostache "1.3.1"]
                 [clj-tagsoup "0.3.0"] ;https://github.com/nathell/clj-tagsoup
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/core.memoize "0.5.6"]
                 [jackknife "0.1.6"]
                 [digest "1.4.3"]
                 ]
  :jvm-opts ["-Xmx700m"]
  :main poop.core
  )
