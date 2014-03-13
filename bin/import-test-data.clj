(use '[poop.model.user :as user])
(use '[poop.lib.db                 :only [connect-db wrap-transaction]])
(use '[korma.db :as db])
(use '[clojure.tools.logging :only (info error)])

(defn genusr [fname lname user_type]
  (db/transaction
   (let [email (.toLowerCase (str fname "@" lname ".com"))
         basedata {
                   :fname                fname
                   :lname                lname
                   :email                email
                   :user_type            user_type
                   :password             "pass"
                   }
         usr (user/save basedata)]
     (info (str fname " " lname " " email))
     )))

(defn own [email-broker email-user]
  (let [u (user/find-by-email email-user)
        b (user/find-by-email email-broker)]
    (user/take-ownership (:id b) (:id u))))


(connect-db)

(genusr "Ken" "Bedwell" "USER")
(genusr "Amy" "Bedwell" "USER")
(genusr "Zach" "Bedwell" "USER")
(genusr "Aubrey" "Bedwell" "USER")

(genusr "Miles" "Davis" "BROKER")
(genusr "John" "Coletrane" "BROKER")
(genusr "Jerry" "Garcia" "BROKER")

(own "miles@davis.com" "ken@bedwell.com")
(own "miles@davis.com" "amy@bedwell.com")
(own "miles@davis.com" "zach@bedwell.com")