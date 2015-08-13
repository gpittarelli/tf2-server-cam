(ns tf2-server-cam.core
  (:require [clj-ssq.core :as ssq]
            [clj-ssq.codecs :refer [msq-regions]])
  (:gen-class))

(def regions (vals msq-regions))

(defn- list-tf2-servers [region]
  (loop []
    (let [servers
          @(ssq/master "hl2master.steampowered.com" 27011
                       region "\\appid\\440"
                       :timeout 20000 :socket-timeout 20000)]
      (if (:err servers)
        (do (Thread/sleep 30000)
            (recur))
        servers))))

(defn -main [& args]
  (let [region->servers
        (zipmap regions
                (->> regions
                     (map list-tf2-servers)
                     doall))]
    (println region->servers)
    (shutdown-agents)))
