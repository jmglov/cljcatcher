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

(defn item->episode [i item]
  {:title (get-attr item "title")
   :description (get-attr item "summary")
   :audio-url (-> item
                  (.querySelector "enclosure")
                  (.getAttribute "url"))
   :number i})

(defn feed->podcast [feed]
  {:title (get-attr feed "channel > title")
   :description (get-attr feed "channel > description")
   :cover-art (get-attr feed "channel > image > url")
   :episodes (->> (.querySelectorAll feed "item")
                  reverse
                  (map-indexed item->episode))})

(defn set-title! [title]
  (set! (.-title js/document) title))

(defn set-description! [description]
  (let [el (js/document.querySelector "div#description")]
    (set! (.-innerHTML el) description)))

(defn set-cover-art! [image-url]
  (let [el (js/document.querySelector "img#cover-art")]
    (set! (.-src el) image-url)))

(defn load-episode! [{:keys [description audio-url] :as item}]
  (js/console.log item)
  (set-description! description)
  (set! (.-src (js/document.querySelector "audio"))
        audio-url))

(defn set-episodes! [episodes]
  (let [ul (js/document.querySelector "div#episode-list > ul")]
    (.replaceChildren ul)
    (doseq [{:keys [title description number]
             :as episode}
            episodes
            :let [li (js/document.createElement "li")]]
      (-> li (.-classList) (.add "clickable"))
      (set! (.-innerHTML li)
            (str "Episode " number " - " title))
      (.addEventListener li "click"
                         #(load-episode! episode))
      (.appendChild ul li))))

(defn display-podcast!
  [{:keys [title description cover-art episodes]
    :as podcast}]
  (js/console.log (clj->js podcast))
  (set-title! title)
  (set-description! description)
  (set-cover-art! cover-art)
  (set-episodes! episodes))

(comment

  (p/->> feed-url
         load-feed
         js/console.log)

  (p/->> feed-url
         load-feed
         feed->podcast
         display-podcast!)

  (p/->> feed-url
         load-feed
         feed->podcast
         :episodes
         first
         clj->js
         js/console.log
         #_display-podcast!)

  (p/let [feed (load-feed feed-url)
          podcast
          {:title (get-attr feed "channel > title")
           :description (get-attr feed "channel > description")
           :cover-art (get-attr feed "channel > image > url")}]
    (display-podcast! podcast))

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
