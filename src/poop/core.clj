(ns poop.core
  (:use [compojure.core]
        [ring.middleware.params         :only [wrap-params]]
        [ring.middleware.cookies        :only [wrap-cookies]]
        [ring.middleware.keyword-params :only [wrap-keyword-params]]
        [ring.middleware.session        :only [wrap-session]]
        [ring.middleware.reload         :only [wrap-reload]]
        [ring.middleware.logger         :only [wrap-with-logger]]
        [clojure.tools.logging          :only [info error]]
        [poop.controller.routes        :only [app-routes]]
        [poop.lib.web                  :only [wrap-mobile-detection wrap-production-detection]]
        [poop.lib.db                   :only [connect-db wrap-transaction]]
        [poop.lib.template             :only [init-templates]]
        [poop.lib.session              :only [db-session-store]]
        [sandbar.stateful-session]
        )
  (:require
   [compojure.handler :as handler]
   [ring.adapter.jetty :as jetty]
   [ring.util.response]
   [clojure.tools.logging :as log]))


(defn app []
  (-> app-routes
      (wrap-production-detection)
      (wrap-mobile-detection)
      (wrap-with-logger)
      (wrap-cookies)
      (wrap-stateful-session {:cookie-name "pcookie" :store (db-session-store) })
      (wrap-keyword-params)
      (wrap-params)
      ))


(def ring-handler
  (handler/site
   (do (connect-db)
       (init-templates)
       (app))))


(defn svr-start []
  (connect-db)
  (init-templates)
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (jetty/run-jetty (app) {:port port :join? false})))


(defn -main [] (svr-start))


(defn repl-start []
  (connect-db "postgresql://poop:poop@localhost:5432/poop")
  )





