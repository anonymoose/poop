;;
;; sendgrid integration
;;
(ns poop.lib.mail
  (:require
   [poop.lib.util :as util]
   [clojure.tools.logging :as log]
   [sendgrid.core :as sendgrid]
   [clojure.string :as string]))


(defn get-sg-username []
  (get (System/getenv) "SENDGRID_USERNAME" "kbedwell"))


(defn get-sg-password [] 
  (get (System/getenv) "SENDGRID_PASSWORD" "Zachary234"))


(defn get-sg-auth []
  {:api_user (get-sg-username)
   :api_key (get-sg-password)})


(defn sendmail
  "{:to \"foo@bar.com\"
   :from \"whatever@poop.com\"
   :subject \"Mail\"
   :text \"<h1>Hello world</h1>\"}"
  [args]
  (let [res (sendgrid/send-email (get-sg-auth) args)
        msg (:message res)]
    (if (= "success" msg) true msg)))

(comment
  (mail/sendmail {:to "kenneth.bedwell@gmail.com"
                  :from "info@poop.io"
                  :subject "test"
                  :text "fiz buz"})
  )