(ns tf2-server-cam.core
  (:require [clj-ssq.core :as ssq]
            [clj-ssq.codecs :refer [msq-regions]]
            [clojure.string :as str])
  (:import [java.net InetAddress])
  (:gen-class))

(def regions (disj (set (vals msq-regions)) :other))

(def master-ips
  (->> "hl2master.steampowered.com"
       (InetAddress/getAllByName)
       vec
       (map #(.getHostAddress %))))

(defn- list-tf2-servers [region]
  (let [servers
        (loop [ips (cycle master-ips)]
          (let [[cur-ip & rest-ips] ips
                servers
                @(ssq/master cur-ip 27011
                             region "\\appid\\440"
                             :timeout 20000 :socket-timeout 20000)]
            (if (:err servers)
              (do (Thread/sleep 30000)
                  (recur rest-ips))
              servers)))]
    (->> servers
         (map #(str/split % ":"))
         (map (fn [[ip port]]
                {:ip ip
                 :port port
                 :info (ssq/info ip port)
                 :rules (ssq/rules ip port)
                 :players (ssq/players ip port)})))))

(defn -main [& args]
  (let [region->servers
        (zipmap regions
                (->> regions
                     (map list-tf2-servers)
                     doall))]
    (println region->servers)
    (shutdown-agents)))
