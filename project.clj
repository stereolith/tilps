(defproject tilps "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.764"]

                 ;; Server
                 [org.eclipse.jetty/jetty-server "9.4.27.v20200227"]
                 [ring/ring-jetty-adapter "1.8.1"  :exclusions [org.eclipse.jetty/jetty-server]]
                 [compojure "1.6.2"]
                 [com.datomic/datomic-pro "1.0.6202" :exclusions [[com.google.guava/guava][org.eclipse.jetty/jetty-server]]]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [clojure.java-time "0.3.2"]
                 [mount "0.1.16"]
                 [com.bhauman/figwheel-main "0.2.3" :exclusions [org.eclipse.jetty/jetty-server]]
                 ;; Client
                 ]

  :source-paths ["src" "src-cljs"]

  :figwheel {:ring-handler tilps.handler/app}

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]}

  :profiles
    {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                          [com.bhauman/rebel-readline-cljs "0.1.4"]]
           :source-paths ["dev/clj"]
           
           :resource-paths ["resources/public"]
           :target-paths ["resources/public/js"]
           }})
