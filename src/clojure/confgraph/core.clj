(ns confgraph.core
  (:gen-class)
  (:import ExtendedConstructor
           java.io.File
           org.jgrapht.EdgeFactory
           org.jgrapht.graph.DefaultDirectedGraph
           org.jgrapht.graph.DefaultEdge
           org.yaml.snakeyaml.Yaml))

(def defs       (filter #(.isFile %) (.listFiles (File. "defs/runs"))))
(def vertices   (map #(.getName %) defs))
(def yaml       (Yaml. (ExtendedConstructor.)))
(def extends    (fn [x] (.get (.load yaml (slurp (.getPath x))) "ddts_extends")))
(def prototypes (map extends defs))
(def edges      (into {} (filter val (zipmap vertices prototypes))))

(def rootpath
  (memoize #(let [x (edges %)] (if x (conj {% x} (rootpath x)) {}))))

(defn graph [prefix]
  (let [g (DefaultDirectedGraph. DefaultEdge)
        e (filter #(re-matches (re-pattern (str prefix ".*")) (first %)) edges)]
    (doseq [v vertices] (. g (addVertex v)))
    (doseq [[src dst] (into {} (for [[src dst] e] (rootpath src)))] (. g (addEdge src dst)))
    g))

(defn -main [& args]
  (if (> (count args) 1)
    (println "Supply at most a single filtering prefix.")
    (println (.toString (graph (first args))))))
