;;
;; Compojure Routing
;;

(ns poop.controller.routes
  (:use
   [compojure.core]
   [poop.lib.web]
   )
  (:require
   [compojure.route :as route]
   [clojure.tools.logging :as log]
   [poop.controller.sitec]
   [poop.controller.userc]
   ))


(defroutes app-routes

  (GET "/" [] (poop.controller.sitec/index))

  ;; (GET "/sign-out" []
  ;;      (poop.controller.userc/sign-out))

  (GET "/sign-up" []
       (poop.controller.userc/sign-up))

  (POST "/sign-up" [fname lname email password]
        (poop.controller.userc/sign-up {
                                         :fname fname
                                         :lname lname
                                         :email email
                                         :password password
                                         }))

  (POST "/add-kid" [name]
        (logged-in? #(poop.controller.userc/add-kid name)))

  (POST "/remove-kid" [kid_id]
       (logged-in? #(poop.controller.userc/remove-kid kid_id)))

  (GET "/dashboard" {query-params :query-params}
       (logged-in? #(poop.controller.userc/dashboard query-params)))

  (GET "/sign-in" [] (poop.controller.userc/sign-in false))
  (GET "/sign-in/deny" [] (poop.controller.userc/sign-in true))
  (POST "/sign-in" [email password] (poop.controller.userc/sign-in email password))

  (GET "/sign-out" [] (poop.controller.userc/sign-out))
  ;; (GET "/forgot-password" [] (poop.controller.userc/forgot-password))
  ;; (POST "/forgot-password" [email] (poop.controller.userc/forgot-password email))

  (GET "/account" [] (poop.controller.userc/account false))
  (GET "/account/deny" [] (poop.controller.userc/account true))
  (GET "/account/saved" [] (poop.controller.userc/account-saved))
  (POST "/account/:id" {{id :id} :params
                        form-params :form-params}
        (logged-in? id #(poop.controller.userc/account id form-params)))

  (POST "/change-password" {form-params :form-params}
        (logged-in? #(poop.controller.userc/change-password form-params)))

  (POST "/event-save" {form-params :form-params}
        (logged-in? #(poop.controller.userc/event-save
                      (form-params "users_id")
                      (form-params "kid_id")
                      form-params)))

  (GET "/event-report" []
       (logged-in? #(poop.controller.userc/event-report)))

  (GET "/event-view/:id" {{id :id} :params}
       (logged-in? #(poop.controller.userc/event-view id)))

  (GET "/:pg" [pg] (poop.controller.sitec/show-page pg))

  ; housekeeping routes
  (route/resources "/")
  ; (route/not-found (poop.controller.sitec/four-oh-four))
  )
