(ns webchange.editor-v2.graph-builder.graph.phrases-graph--slide-expected)

(def data {:start                  {:path        [:start]
                                    :entity      :trigger
                                    :children    []
                                    :connections #{{:previous :root
                                                    :name     "start"
                                                    :handler  :mari-voice-welcome}}}
           :mari-voice-welcome     {:path        [:mari-voice-welcome]
                                    :entity      :action
                                    :children    []
                                    :connections #{{:previous :start
                                                    :name     "next"
                                                    :sequence :start-scene
                                                    :handler  :riddle}}}
           :riddle                 {:path        [:riddle]
                                    :entity      :action
                                    :children    []
                                    :connections #{}}
           :box1                   {:path        [:box1]
                                    :entity      :object
                                    :children    []
                                    :connections #{{:previous :root
                                                    :name     "click"
                                                    :handler  :mari-voice-correct}
                                                   {:previous :root
                                                    :name     "click"
                                                    :handler  :mari-voice-wrong}}}
           :box2                   {:path        [:box2]
                                    :entity      :object
                                    :children    []
                                    :connections #{{:previous :root
                                                    :name     "click"
                                                    :handler  :mari-voice-correct}
                                                   {:previous :root
                                                    :name     "click"
                                                    :handler  :mari-voice-wrong}}}
           :box3                   {:path        [:box3]
                                    :entity      :object
                                    :children    []
                                    :connections #{{:previous :root
                                                    :name     "click"
                                                    :handler  :mari-voice-correct}
                                                   {:previous :root
                                                    :name     "click"
                                                    :handler  :mari-voice-wrong}}}

           :mari-voice-correct     {:path        [:mari-voice-correct]
                                    :entity      :action
                                    :children    []
                                    :connections #{{:previous :box2
                                                    :name     "next"
                                                    :sequence :pick-correct
                                                    :handler  :mari-voice-try-another}
                                                   {:previous :box1
                                                    :name     "next"
                                                    :sequence :pick-correct
                                                    :handler  :mari-voice-try-another}
                                                   {:previous :box3
                                                    :name     "next"
                                                    :sequence :pick-correct
                                                    :handler  :mari-voice-try-another}}}
           :mari-voice-try-another {:path        [:mari-voice-try-another]
                                    :entity      :action
                                    :children    []
                                    :connections #{{:previous :mari-voice-correct
                                                    :name     "next"
                                                    :sequence :pick-correct
                                                    :handler  :riddle}}}
           :mari-voice-wrong       {:path        [:mari-voice-wrong]
                                    :entity      :action
                                    :children    []
                                    :connections #{}}})