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

(defn map-values
  [m ks f]
  (reduce #(update-in %1 [%2] f) m ks))

(defn- list-tf2-servers [region]
  (let [servers
        (loop [ips (cycle master-ips)]
          (let [[cur-ip & rest-ips] ips
                servers
                @(ssq/master cur-ip 27011
                             region "\\appid\\440"
                             :timeout 15000 :socket-timeout 15000)]
            (if (:err servers)
              (do (Thread/sleep 20000)
                  (recur rest-ips))
              servers)))]
    (->> servers
         (map #(str/split % #":"))
         (map (fn [[ip port]]
                (let [port (Integer. port)]
                  (Thread/sleep 50)
                  {:ip ip
                   :port port
                   :info (ssq/info ip port)
                   :rules (ssq/rules ip port)
                   :players (ssq/players ip port)})))
         doall)))

(defn -main [& args]
  (let [region->servers
        (zipmap regions
                (->> regions
                     (map list-tf2-servers)
                     doall))

        region->servers
        (map-values
         region->servers
         (keys region->servers)
         (fn [servers-per-region]
           (map (fn [server-data]
                  (map-values server-data
                              [:info :rules :players]
                              (comp #(if (and (map? %1)
                                              (contains? %1 :exception))
                                       (dissoc %1 :exception)
                                       %1)
                                    deref)))
                servers-per-region)))]
    (prn region->servers)
    (shutdown-agents)))
