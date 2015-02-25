(ns lazybot.plugins.grimoire
  (:require [grimoire.util :as util]
            [grimoire.things :as t]
            [grimoire.either :as e]
            [grimoire.api :as api]
            [grimoire.api.web.read]
            [lazybot.registry :refer [defplugin send-message]]))

(def config
  {:datastore
   {:mode :web
    :host "http://conj.io"}})

(def nss
  (let [artifact (-> (t/->Group "org.clojure")
                     (t/->Artifact "clojure"))
        newest   (first (e/result (api/list-versions config artifact)))
        platform (t/->Platform newest "clj")]
    (->> platform
         (api/list-namespaces config)
         e/result
         (map t/thing->name)
         (into #{}))))

(defplugin
  (:cmd
   "Print the Grimoire URL for a symbol"
   #{"grim"}
   (fn [{:keys [args] :as com-m}]
     (let [sym      (first args)
           [_ ns s] (re-matches #"(.*?)/(.*)" sym)]
       (when (nss ns)
         (send-message
          com-m
          (format "http://conj.io/store/v0/org.clojure/clojure/latest/clj/%s/%s"
                  ns (util/munge s))))))))
