(ns webchange.editor-v2.dialog.dialog-form.views-form-phrase
  (:require
    [re-frame.core :as re-frame]
    [cljs-react-material-ui.reagent :as ui]
    [webchange.editor-v2.translator.translator-form.state.actions :as translator-form.actions]
    [webchange.editor-v2.dialog.dialog-form.state.actions :as dialog-form.actions]
    [webchange.editor-v2.translator.translator-form.utils :refer [trim-text]]
    [webchange.editor-v2.translator.translator-form.common.views-text-field :refer [text-field]]))


(defn node-options
  []
  (let [text-input-params {:placeholder "Enter options"
                           :variant     "outlined"
                           :margin      "normal"
                           :multiline   true
                           :full-width  true}
        {:keys [path]} @(re-frame/subscribe [::translator-form.actions/current-phrase-action-info])
        paralel? (= 3 (count path))
        main-paralel? (= 0 (last path))
        current-phrase-action @(re-frame/subscribe [::translator-form.actions/current-phrase-action])
        volume-text (-> current-phrase-action (get-in [:data 1]) :volume str trim-text)
        offset-text (-> current-phrase-action (get-in [:data 0]) :duration str trim-text)
        handle-volume-change (fn [event] (let [new-value (.. event -target -value)]
                                           (re-frame/dispatch [::dialog-form.actions/set-phrase-action-volume
                                                               new-value])))
        handle-offset-change (fn [event]
                               (let [new-value (.. event -target -value)]
                                 (re-frame/dispatch [::dialog-form.actions/set-phrase-action-offset
                                                     new-value])))]
    [ui/grid {:container true
              :spacing   16
              :justify   "space-between"}
     [ui/grid {:item true :xs 6}
      [text-field (merge text-input-params
                         {:label           "Volume"
                          :value           (or volume-text "")
                          :on-change       handle-volume-change
                          :InputLabelProps {:shrink true}})]]
     (if (and paralel? (not main-paralel?))
       [ui/grid {:item true :xs 6}
        [text-field (merge text-input-params
                           {:label           "Delay"
                            :value           (or offset-text "")
                            :on-change       handle-offset-change
                            :InputLabelProps {:shrink  true
                                              :focused true}})]])]))
