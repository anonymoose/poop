(ns poop.lib.redis
  (:require
   [taoensso.carmine :as red :refer (wcar)]))

(def local-conn {:pool {} :spec {}})

(defmacro R* [& body] `(red/wcar local-conn ~@body))

(defn redis-lrange [key beg end]
  (R* (red/lrange key beg end)))

(defn redis-keys [pattern]
  (R* (red/keys pattern)))

