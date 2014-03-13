(ns poop.lib.web
  (:require
   [clojure.tools.logging :as log]
   [ring.util.response :as ring]    
   [sandbar.stateful-session :as session]
   [poop.lib.util :as util]
   [clj-json.core :as json]
   ))


(defn logged-in?
  "If there is a userid in the session, then execute the function.
   If an ID is provided, make sure it matches the one in session.
   Otherwise, kill the session and barf out somewhere."
  ([id func]
     (let [session-user-id (session/session-get :user-id)]
       (if (and (not (nil? session-user-id))
                (= session-user-id id))
         (func)
         (do
           (session/destroy-session!)
           (ring/redirect (str "/sign-in/deny"))))))
  ([func]
     (let [session-user-id (session/session-get :user-id)]
       (if (not (nil? session-user-id))
         (func)
         (do
           (session/destroy-session!)
           (ring/redirect (str "/sign-in/deny")))))))


(defn session-user-id
  "Convenience method for getting current userid from session"
  []
  (session/session-get :user-id))


(defn json-api-raw
  [data]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body data
   }
  )

(defn json-api
  [data]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)
   }
  )

(def ^:dynamic *production*)

(defn production? []
  *production*)

(defn wrap-production-detection
  "Middleware that binds a mobile variable if we have an environment variable \"PICKR_DEBUG\" == TRUE
  "
  [handler]
  (fn [request]
    (binding [*production* (not= "TRUE" (get (System/getenv) "PICKR_DEBUG" ""))]
      (handler request))))


(def ^:dynamic *mobile*)

(defn mobile? []
  *mobile*)

(defn wrap-mobile-detection
  "Middleware that binds a mobile variable if we are on a domain that starts with http://m.*
  We use /resources/public/js/site/mobile.js to determine if we should be here and this wrapper
  to rebind a var.
  "
  [handler]
  (fn [request]
    (binding [*mobile* (.startsWith (:server-name request) "m.")]
      (handler request))))


