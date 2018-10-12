(ns startingclojure.app
  (:use (compojure handler
                   (core :only (GET POST defroutes))))
  (:require compojure.route
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [net.cgrand.enlive-html :as enlive]))

(defonce counter (atom 10000))

(defonce urls (atom {}))

(defn shorten
  [url]
  (let [id (swap! counter inc)
        id (Long/toString id 36)]
    (swap! urls assoc id url)
    id))

(enlive/deftemplate homepage
  (enlive/xml-resource "homepage.html")
  [request]
  [:#listing :li] (enlive/clone-for [[id url] @urls]
                    [:a] (comp
                           (enlive/content (format "%s : %s" id url))
                           (enlive/set-attr :href (str \/ id)))))

(defn redirect
  [id]
  (response/redirect (@urls id)))

(defroutes app*
  (GET "/" request (homepage request))
  (GET "/:id" [id] (redirect id))
  (POST "/shorten" request
    (let [id (shorten (-> request :params :url))]
      (response/redirect "/")))
  (compojure.route/resources "/"))


(def app (compojure.handler/site app*))

(def server (jetty/run-jetty #'app {:port 8000 :join? false}))