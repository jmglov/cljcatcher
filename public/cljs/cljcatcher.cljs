(ns cljcatcher)

(def title "cljcatcher - An Awesome Podcast")

(set! (.-title js/document) title)

(comment

  (+ 1 2)
  ;; => 3

  (js/alert "This is much cooler!")

  )
