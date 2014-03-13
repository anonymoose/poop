(ns poop.lib.util
  (:require
   [clojure.tools.logging :as log]
   [clj-time.predicates :as dtp]
   [clojure.string :as str]
   [clj-time.core :as dt]
   [clj-time.format :as dt-fmt]
   [clj-time.periodic :as dt-per]
   [clj-time.predicates :as dt-pred]
   )
  (:use [clojure.pprint])
  (:refer-clojure :exclude [extend second])
  (:import
   (java.io StringWriter)
   (java.sql Timestamp)
   (java.net URL)
   (java.lang StringBuilder)
   (java.io BufferedReader InputStreamReader)
   )
)



(defn current-date []
  (new java.util.Date))

(defn parse-sql-date
  ([dt]
     (parse-sql-date dt "yyyy-MM-dd"))
  ([dt fmt]
     (if-not (str/blank? dt)
       (new java.sql.Date (.getTime (.parse (new java.text.SimpleDateFormat fmt) dt))))))

(defn parse-date
  ([dt]
     (parse-date dt "yyyy-MM-dd"))
  ([dt fmt]
     (if-not (str/blank? dt)
       (.parse (new java.text.SimpleDateFormat fmt) dt))))

(defn words-date-days-ago
  ([days-ago]
     (let [cal (java.util.Calendar/getInstance)]
       (.setTime cal (new java.util.Date))
       (.add cal java.util.Calendar/DAY_OF_YEAR days-ago)
       (.format (new java.text.SimpleDateFormat "MMMMM dd, yyyy") (.getTime cal))
       )))

(defn dt-now []
  (dt/now))

(defn dt-yesterday []
  (dt/plus (dt/now) (dt/days -1)))

(defn dt-add-years [d ys]
  (dt/plus d (dt/years ys)))

(defn dt-add-months [d ms]
  (dt/plus d (dt/months ms)))

(defn dt-add-days [d ds]
  (dt/plus d (dt/days ds)))

(defn dt-parse
  ([dt]
     (dt-parse dt "yyyy-MM-dd"))
  ([dt fmt]
     (dt-fmt/parse (dt-fmt/formatter fmt) dt)))

(defn dt-weekend?
  [dt]
  (or (dt-pred/saturday? dt)
      (dt-pred/sunday? dt)))

(defn dt-before? [d1 d2]
  (dt/before? d1 d2))

(defn dt-after? [d1 d2]
  (dt/after? d1 d2))

(defn dt-format-to-str [d fmt]
  (dt-fmt/unparse (dt-fmt/formatter fmt) d))

(defn dt-days-seq [st]
  (dt-per/periodic-seq st (dt/days 1)))

(defn dt-date-list-from-past [days-back]
  (take days-back (dt-per/periodic-seq (dt/minus (dt/now) (dt/days days-back)) (dt/days 1))))

(defn dt-date-list-from-year [yr]
  (filter #(= yr (dt/year %)) (take 366 (dt-per/periodic-seq (dt/date-time yr 1 1) (dt/days 1)))))

(defn dt-date-list-from-month [yr mo]
  (filter #(and (= yr (dt/year %))
                (= mo (dt/month %)))
          (take 32 (dt-per/periodic-seq (dt/date-time yr mo 1) (dt/days 1)))))


;; (defn dt-weekend [d]
;;   (dtp/weekend? d))

(defn dt-today-as-int []
  (Integer/parseInt (dt-format-to-str (dt-now) "yyyyMMdd")))

(defn words-date-today []
  (words-date-days-ago 0))

(defn format-date
  ([dt]
     (format-date dt "yyyy-MM-dd"))
  ([dt fmt]
     (if-not (nil? dt)
       (.format (new java.text.SimpleDateFormat fmt) dt))))

(defn within-n-days?
  ([d1 d2 n fmt]
     (let [fmter (dt-fmt/formatter fmt)
           d1 (dt-fmt/parse fmter d1)
           d2 (dt-fmt/parse fmter d2)]
       (dt/within? (dt/interval d1 (dt/plus d1 (dt/days n))) d2)))
  ([d1 d2 n]
     (within-n-days? d1 d2 n "yyyy-MM-dd")))

(defn dt-within? [dt1 dtbetween dt2]
  (dt/within? (dt/interval dt1 dt2) dtbetween))

(defn current-date-str []
  (format-date (current-date)))

(defn current-year []
  (+ 1900 (.getYear (current-date))))

(defn sql-today []
  (new java.sql.Date (.getTime (parse-date (current-date-str)))))

(defn uuid []
  "Generate a base 36 string from a UUID
  90a58d7a-d274-4dc4-a80e-37daa76b7fb8 --> 2odnrp7yitnl05rg975gmyeas"
  (.toString (new BigInteger (.replace (str (java.util.UUID/randomUUID)) "-" "") 16) 36))

(defn ^Timestamp ts-now
  ([]
    (ts-now 0))
  ([delta]
    (Timestamp. (+ (System/currentTimeMillis) delta))))


(defn keyword-keyify
  "Turns
    {\"a\" \"aa\" \"b\" \"bb\"}
    into
    {:b \"bb\", :a \"aa\"}
   "
  [hash]
  (zipmap (map keyword (keys hash)) (vals hash)))

(defn round [places num]
  (read-string (format (str "%." places "f") num)))

(defn string-keyify
  "Turns
    {:b \"bb\", :a \"aa\"}
    into
    {\"a\" \"aa\" \"b\" \"bb\"}
   "
  [hash]
  (zipmap (map name (keys hash)) (vals hash)))


(defn dateify-params
  "if a key ends in _dt and its value is a string, convert that to a sql date."
  [params]
  (zipmap (keys params) 
          (for [kv (seq params)]
            (let [k (name (key kv))
                  v (val kv)]
                                        ; If it ends in _dt and is a string, then do the conversion of the value.
                                        ; Else just return it.
              (if (and (.endsWith k "_dt")
                       (= (type v) java.lang.String))
                (parse-sql-date v)
                v
                )))))

(defn undateify-params
  "if a value's type is a date-ish type, convert the date to a date string"
  [params]
  (zipmap (keys params) 
          (for [kv (seq params)]
            (let [k (name (key kv))
                  v (val kv)]
              (if (or (= (type v) java.sql.Timestamp)
                      (= (type v) java.sql.Date))
                (format-date v)
                v
                )))))


(defn debug-mode? []
  (= (get (System/getenv) "PICKR_DEBUG" "") "TRUE"))


(defn unix-timestamp-to-sql-timestamp [val]
  (if-not (nil? val)
    (java.sql.Timestamp. (* 1000 (Long. val)))))


(defn unix-timestamp-to-sql-date [val]
  (if-not (nil? val)
    (java.sql.Date. (* 1000 (Long. val)))))


(defn rename-key [kvmap old new]
  (assoc (dissoc kvmap old) new (kvmap old)))


(defn only-prefixed-with
  "(only-prefixed-with 'x_' {:x_a 'xa' :x_b 'xb' :a 'a' :b 'b' :c 'c'})
   -> {:x_b 'xb', :x_a 'xa'}
  "
  [prefix params]
  (into {} (remove nil? (map #(if (.startsWith (name (first %)) prefix) %) params))))


(defn not-prefixed-with
  "(not-prefixed-with 'x_' {:x_a 'xa' :x_b 'xb' :a 'a' :b 'b' :c 'c'})
   -> {:b 'b', :a 'a', :c 'c'}
  "
  [prefix params]
  (into {} (remove nil? (map #(if-not (.startsWith (name (first %)) prefix) %) params))))


(defn un-prefix
  "given a bunch of keys, some with prefixes, strip off the prefixes from the keys"
  [prefix params]
  (zipmap (map #(str/replace-first (name %) prefix "") (keys params)) (vals params))
  )

(def not-nil? (complement nil?))

(defn save-lists
  "
      (let [list-fields (select-keys params (keys (util/only-prefixed-with 'list_' params)))]
       (util/save-list list-fields post/save-list-item))
  "
  [list-fields fk-id func]
  (doseq [list-key (keys list-fields)]
                                        ; pull out the "[]" and "list_" from the list post parameter.
    (let [list-type (str/replace (str/replace list-key "[]" "") "list_" "")]
      (doseq [value (list-fields list-key)]
        (if (not (empty? value))
          (func fk-id list-type value))))))

(defn parse-float [f]
  (Float/parseFloat f))

(defn string-convert [v converter]
  (cond (and (= (type v) java.lang.String)
             (not-nil? v)
             (not (empty? v)))
        (converter v)
        (empty? v) nil
        :else v))


(defmacro time-only
  "Evaluates expr and returns the time it took"
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (/ (double (- (. System (nanoTime)) start#)) 1000000.0)))


(defmacro time-and-return
  "Evaluates expr and returns the time it took"
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     [(/ (double (- (. System (nanoTime)) start#)) 1000000.0)
      ret#]))

(defn between-inclusive [n st end]
  (and (>= n st) (<= n end)))

(defn pprint-to-str [obj]
  (let [w (StringWriter.)]
    (pprint obj w)
    (.toString w)))