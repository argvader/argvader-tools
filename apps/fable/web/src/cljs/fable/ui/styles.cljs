(ns fable.ui.styles)

(def palette
  {:bg-deep    "#1a1a2e"
   :bg-card    "#16213e"
   :bg-surface "#0f3460"
   :accent     "#e91e8c"
   :accent-alt "#00bcd4"
   :text-light "#eeeeee"
   :text-muted "#aaaaaa"
   :success    "#4caf50"
   :warning    "#ff9800"
   :error      "#f44336"})

(def base-styles
  {:font-family "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
   :background  (:bg-deep palette)
   :color       (:text-light palette)
   :min-height  "100vh"})

(def card-style
  {:background    (:bg-card palette)
   :border-radius "12px"
   :padding       "24px"
   :box-shadow    "0 4px 24px rgba(0,0,0,0.4)"})

(def button-primary
  {:background    (:accent palette)
   :color         "#fff"
   :border        "none"
   :border-radius "8px"
   :padding       "12px 24px"
   :font-size     "16px"
   :font-weight   "600"
   :cursor        "pointer"
   :transition    "opacity 0.2s"})

(def button-secondary
  (merge button-primary
         {:background "transparent"
          :border     (str "2px solid " (:accent palette))
          :color      (:accent palette)}))

(def step-indicator-style
  {:display         "flex"
   :justify-content "center"
   :gap             "8px"
   :margin-bottom   "32px"})
