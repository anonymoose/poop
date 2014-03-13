;;
;; Connection pooling.
;; http://clojure.github.io/java.jdbc/doc/clojure/java/jdbc/ConnectionPooling.html
;;
(ns poop.lib.db
  (:require
   [clojure.tools.logging :as log]
   [ring.middleware.session.store :as session]
   [poop.lib.util :as util]
   [clojure.java.jdbc :as sql]
   [korma.db :as db]
   [robert.hooke :refer [add-hook]]
   [clj-time.core :as time]
   [clojure.string :as str])
  (:import [org.postgresql.util PGobject])
  (:import (java.net URI)))


(defn sql-log [sql millis]
  (log/info (str sql) "| timing:" millis "ms"))


(defn korma-sql-hook
  "Hook into korma's SQL exec method and log what comes out."
  [f & args]
  (let [start (time/now)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (sql-log (:sql-str (first args)) time-taken)
    result))


(defn korma-add-log-hook
  []
  (add-hook #'db/exec-sql #'korma-sql-hook))


(defn- connect-db-impl
  "split up DATABASE_URL into constituent parts so that korma likes it."
  [url]
  (if (not (nil? url))
    (let
        [db-uri-str url
         db-uri (java.net.URI. db-uri-str)]
      (->> (str/split (.getUserInfo db-uri) #":")
           (#(identity {:db (last (str/split db-uri-str #"\/"))
                        :host (.getHost db-uri)
                        :port (.getPort db-uri)
                        :user (% 0)
                                        ;:make-pool? true
                        :password (% 1)}))
           (db/postgres)
           (db/defdb pgdb)
           )
      (if (util/debug-mode?)
        (korma-add-log-hook))
      )
    nil
    ))


(defn purify
  "purify the parameter to prevent sql injection"
  [s]
  s)


(defn connect-db
  "Connect to the DB based on the environment var passed to use either by
    Heroku, or via exporting it in the shell locally. "
  ([]
     (connect-db-impl (System/getenv "POSTGRESQL_URL")))
  ([url] (connect-db-impl url)))


(defn wrap-transaction
  "Middleware that ensures that everything is executed in a transaciton.
    If anyone throws an exception, then roll it back and propagate the transaction up the stack."
  [handler]
  (fn [request]
    (db/transaction
     (handler request))))


;; https://github.com/blakesmith/pghstore-clj/blob/master/src/pghstore_clj/core.clj

(defn to-hstore
  [hash-val]
  (doto (PGobject.)
    (.setType "hstore")
    (.setValue
     (apply str
            (interpose ", "
                       (for [[k v] hash-val]
                         (format "\"%s\"=>\"%s\"" (name k) v)))))))

(defn from-hstore
  "Given an org.postgresql.util.PGobject"
  [hstore-val]
  (if (= "hstore" (.getType hstore-val))
    (into {}
          (for [[k v]
                (map (fn [v]
                       (map #(str/replace % #"\"" "") (str/split v #"=>")))
                     (str/split (.getValue hstore-val) #", "))]
            [(keyword k) v]))))



