;;
;; Template Rendering (https://github.com/yogthos/Selmer)
;;
(ns poop.lib.template
  (:require
   [clojure.tools.logging :as log]
   [selmer.parser :as parser]
   [selmer.filters :as filter]
   [clojure.string :as str]
   [poop.lib.util :as util]

   [clojure.java.io]))


(defn- common-vars [vars]
  (merge vars {:today (util/current-year)
               :today-words (util/words-date-today)
               }))


(def TEMPLATE-ROOT "resources/templates/")
(def TEMPLATE-EXT ".html")


(defn- tpl-contents [f]
  (slurp (java.io.File. (str TEMPLATE-ROOT f TEMPLATE-EXT)))
  ;(str TEMPLATE-ROOT f TEMPLATE-EXT)
  )

(defn- format-ghost-string [id val klass deflt]
  (str
   "<input type=\"text\" id=\"" id "\" name=\"" id "\" class=\"" (if (str/blank? val) "ghost") " " klass "\" value=" (if (str/blank? val) deflt (str "\"" val "\"")) ">" 
   )
  )


(defn init-templates
  "Run this at app initialization to get our selmer custom tags in there."
  []
  ; let selmer know where things are.  should be called once in 
  ;(selmer.parser/set-resource-path! (str (get (System/getenv) "PWD") "/" TEMPLATE-ROOT))

  ; render some ghosted output, suitable for working with jquery's ghost
  (parser/add-tag! :ghost
                   (fn [args context-map]
                     (let [id (nth args 0)
                           obj (context-map (keyword (nth args 1)))
                           deflt (nth args 2)
                           klass (nth args 3 "input-small-kl")
                           val (obj (keyword id))]
                       (format-ghost-string id val klass deflt))))
  (parser/add-tag! :ghost-val
                   (fn [args context-map]
                     (let [id (nth args 0)
                           val (context-map (keyword (nth args 1)))
                           deflt (nth args 2)
                           klass (nth args 3 "input-small-kl")]
                       (format-ghost-string id val klass deflt))))

  (filter/add-filter! :empty? empty?)
  (filter/add-filter! :truncate (fn [s l]
                                  (let [maxlen (Integer/parseInt l)
                                        strlen (count s)]
                                    (if (> strlen maxlen)
                                     (subs s 0 maxlen)
                                     s
                                     ))))
  (filter/add-filter! :round (fn [s]
                               (if (not (nil? s))
                                 (format "%.2f" (.doubleValue (new Double s)))
                                 "")))

  (filter/add-filter! :fixup (fn [s]
                               (->
                                s
                                (str/replace #"&#39;" "'")
                                )
                               ))
  
  (filter/add-filter! :replace (fn [s replace-this with-this]
                                  (str/replace s (re-pattern replace-this) with-this)))  
  )

(defn add-template-tag [key fn]
  (parser/add-tag! key fn)  )


(defn render-tpl
  ([tpl vars]
     "Render the specified template to a string and return it."
     (parser/render (tpl-contents tpl) (common-vars vars)))
  ([header page footer vars]
     "Render everything with header, footer and common variables.
      this is for 'outside' pages (hence the -out suffix)"
     (let [v (common-vars vars)]
       (str (parser/render (tpl-contents header) v)
            (parser/render (tpl-contents page) v)
            (parser/render (tpl-contents footer) v)))))


(defn page-out
  "(page-out 'views/footer {:abc 123})"
  [page vars]
  (render-tpl "header" page "footer" vars))


(defn page-in
  [page vars]
  (page-out page vars))


(defn render-str [tpl args]
)


