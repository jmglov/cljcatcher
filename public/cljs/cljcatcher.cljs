(ns cljcatcher
  (:require [promesa.core :as p]))

(def feed-url "https://feeds.simplecast.com/K0NWGFwM")

(defn fetch-feed [url]
  (p/-> (js/Request. url)
        js/fetch
        (.text)))

(defn parse-xml [xml]
  (.parseFromString (js/window.DOMParser.) xml "text/xml"))

(defn set-title! [feed]
  (let [title (-> feed
                  (.querySelector "channel > title")
                  (.-innerHTML))]
    (set! (.-title js/document) title))
  feed)

(defn set-description! [feed]
  (let [description
        (-> feed
            (.querySelector "channel > description")
            (.-innerHTML))
        el (js/document.querySelector "div#description")]
    (set! (.-innerHTML el) description))
  feed)

(comment

  (js/fetch (js/Request. feed-url))
  ;; => #object[Promise [object Promise]]
  ;; => #object[Request [object Request]]

  (p/-> (js/Request. feed-url)
        js/fetch
        (.text)
        js/console.log)
  ;; => #<Promise[~]>

  (p/-> (fetch-feed feed-url)
        parse-xml
        set-title!
        set-description!)

  (def feed (parse-xml xml))
  ;; => #'cljcatcher/feed
  ;; => #object[XMLDocument [object XMLDocument]]
  ;; => "#error {:message \"Failed to execute 'parseFromString' on 'DOMParser': 2 arguments required, but only 1 present.\", :data {:type :sci/error, :line 1, :column 1, :message \"Failed to execute 'parseFromString' on 'DOMParser': 2 arguments required, but only 1 present.\", :sci.impl/callstack #object[cljs.core.Volatile {:val ({:line 1, :column 1, :ns #object[To cljcatcher], :file nil})}], :file nil}, :cause #object[TypeError TypeError: Failed to execute 'parseFromString' on 'DOMParser': 2 arguments required, but only 1 present.]}"
  ;; => #object[DOMParser [object DOMParser]]

  (set-title! feed)

  (set-description! feed)

  (-> feed
      (.querySelector "channel > title")
      (.-innerHTML))
  ;; => "Science Rules! with Bill Nye"
  ;; => #object[Element [object Element]]

  (-> feed
      (.querySelector "channel > description")
      (.-innerHTML))
  ;; => "Bill Nye is doing some cool science stuff."

  (js/document.querySelector "div#description")

  )
