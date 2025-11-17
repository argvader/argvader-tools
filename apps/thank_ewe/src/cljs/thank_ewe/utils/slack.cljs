(ns thank-ewe.utils.slack
  (:require [clojure.string :as str]))

(defn section
  "Wraps markdown text with a section block."
  [text]
  {:type "section"
   :text {:type "mrkdwn" :text text}})

(defn divider []
  {:type "divider"})

(defn context [elements]
  {:type "context"
   :elements (mapv (fn [text] {:type "mrkdwn" :text text}) elements)})

(defn- pad [value width]
  (let [value-str (str value)
        padding (max 0 (- width (count value-str)))]
    (str value-str (apply str (repeat padding " ")))))

(defn- column-widths [rows]
  (->> rows
       (apply map (fn [& values]
                    (apply max (map #(count (str %)) values))))))

(defn- join-row [row widths]
  (let [cells (map-indexed
                (fn [idx value]
                  (pad value (nth widths idx)))
                row)]
    (str "| " (str/join " | " cells) " |")))

(defn table-block
  "Produces a monospace table inside a section block."
  [headers rows]
  (let [table-rows (cons headers rows)
        widths (column-widths table-rows)
        header-line (join-row headers widths)
        separator-line (->> widths
                            (map #(apply str (repeat (+ % 2) "-")))
                            (str/join "+")
                            (#(str "+" % "+")))
        body-lines (map #(join-row % widths) rows)
        table-text (->> (concat [header-line separator-line] body-lines)
                        (str/join "\n"))]
    (section (str "```" table-text "```"))))

