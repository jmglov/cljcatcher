(ns cljcatcher
  (:require [promesa.core :as p]))

(def feed-url "https://feeds.simplecast.com/K0NWGFwM")

(defn fetch-feed [url]
  (p/-> (js/Request. url)
        js/fetch
        (.text)))

(defn parse-xml [xml]
  (.parseFromString (js/window.DOMParser.) xml "text/xml"))

(defn load-feed [url]
  (p/-> (fetch-feed url)
        parse-xml))

(defn get-attr [el attr-name]
  (-> el
      (.querySelector attr-name)
      (.-innerHTML)))

(defn set-title! [feed]
  (let [title (get-attr feed "channel > title")]
    (set! (.-title js/document) title))
  feed)

(defn set-description! [feed]
  (let [description
        (get-attr feed "channel > description")
        el (js/document.querySelector "div#description")]
    (set! (.-innerHTML el) description))
  feed)

(defn set-cover-art! [feed]
  (let [image-url
        (get-attr feed "channel > image > url")
        el (js/document.querySelector "img#cover-art")]
    (set! (.-src el) image-url))
  feed)

(defn display-podcast! [feed-url]
  (p/-> (load-feed feed-url)
        set-title!
        set-description!
        set-cover-art!))

(comment

  (display-podcast! feed-url)

  (p/let [items
          (p/-> (load-feed feed-url)
                set-title!
                set-description!
                set-cover-art!
                (.querySelectorAll "item"))
          episodes-list (js/document.querySelector "div#episode-list > ul")
          episodes
          (->> items
               (take 2)
               (map (fn [item]
                      (let [li (js/document.createElement "li")]
                        (-> li (.-classList) (.add "clickable"))
                        (set! (.-innerHTML li) (get-attr item "title"))
                        (.addEventListener
                         li "click"
                         #(set-description!
                           (set! (.-innerHTML (js/document.querySelector "div#description"))
                                 (get-attr item "summary"))))
                        li))))]
    (.replaceChildren episodes-list)
    (doseq [episode episodes]
      (.appendChild episodes-list episode)))

  )
