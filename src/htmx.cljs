; htmx interface for comments backend
(ns htmx
  (:require [html-serializer :as html]
            [hiccup]
            [repo]
            [promesa.core :as p]))

(defn author-input
  [{:keys [author-input-id]} swap-oob]
  (let [html-attrs {:type "text" :name "author" :id author-input-id}
        html-attrs (if swap-oob (assoc html-attrs :hx-swap-oob true) html-attrs)]
    [:input html-attrs]))

(defn message-input
  [{:keys [message-input-id]} swap-oob]
  (let [html-attrs {:name "message" :required true :rows 5 :id message-input-id}
        html-attrs (if swap-oob (assoc html-attrs :hx-swap-oob true) html-attrs)]
    [:textarea html-attrs]))

(defn get-comments
  "Retrieves a list of comments as HTML."
  [config post-id]
  (p/let [cmts (repo/get-comments config post-id)
          html-comments (html/serialize-comment-list cmts)]
    (hiccup/html html-comments)))

(defn post-comment
  "Adds a comment. Returns HTML representing
  the added comment and also the new, blank input form
  to render in place of the old one."
  [config cmt]
  (p/let [comment-time (.toISOString (js/Date.))
          cmt-w-time (assoc cmt :time comment-time)
          comment-result (repo/save-comment config cmt-w-time)
          serialized-result (html/serialize-comment comment-result)]
    (str
      (hiccup/html (list
                     (author-input config true)
                     (message-input config true)))
      "\n"
      (hiccup/html serialized-result))))

(defn get-submit-comment-button
  [recaptcha-sitekey]
  (if recaptcha-sitekey
    [:button {:type "submit"
              :class "g-recaptcha"
              :data-sitekey recaptcha-sitekey
              :data-callback "announce"} "Submit"]
    [:button {:type "submit"} "Submit"]))

(defn get-recaptcha-callback-js
  [{:keys [comment-form-id recaptcha-callback-event]}]
  (str
    "function announce(token) {\n"
    "  const event = new Event('" recaptcha-callback-event "');\n"
    "  const elem = document.querySelector('#" comment-form-id "');\n"
    "  elem.dispatchEvent(event);\n"
    "}"))

(defn gen-comments-form
  [{:keys [comment-form-id
           comment-list-div-id
           post-comment-url
           recaptcha-callback-event
           recaptcha-sitekey]
    :as config}
   post-id]
  [:form
   {:id comment-form-id
    :hx-post post-comment-url
    :hx-swap "afterbegin"
    :hx-target (str "#" comment-list-div-id)
    :hx-trigger (if recaptcha-sitekey (str recaptcha-callback-event ",submit") "submit")
    :hx-swap-oob "true"}
   [:input {:type "hidden" :name "post-id" :value post-id}]
   [:label {:for "author"} "Name (optional)"]
   (author-input config false)
   [:label {:for "message"} "Comment"]
   (message-input config false)
   [:script (get-recaptcha-callback-js config)]
   [:script {:src "https://www.google.com/recaptcha/api.js"}]
   (get-submit-comment-button recaptcha-sitekey)])

(def get-comments-form-html (comp #(hiccup/html % true) gen-comments-form))

(defn make-htmx-config
  [user-config]
  (let [defaults {:comment-form-id "comment-form"
                  :comment-list-div-id "comments-list"
                  :post-comment-url "http://localhost:3000/comments"
                  :recaptcha-callback-event "recaptcha-verified"
                  :author-input-id "author-input"
                  :message-input-id "message-input"}]
    (merge defaults user-config)))

