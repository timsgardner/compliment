(ns compliment.t-core
  (:require [midje.sweet :refer :all]
            [compliment.core :as core]
            [compliment.context :as ctx]))

;; This namespace contains only sanity checks for the public API. For
;; in-depth source testing see their respective test files.

(facts "about completion"
  (fact "`completions` takes a prefix, optional namespace and a context.
  If context is missing, `nil` should be passed"
    (core/completions "redu" nil)
    => (contains ["reduce" "reduce-kv" "reductions"] :gaps-ok)

    (core/completions "fac" (find-ns 'midje.sweet) nil)
    => (just #{"fact-group" "fact" "facts"})

    (core/completions "fac" (find-ns 'clojure.core) nil)
    => ()

    (core/completions "compliment.core/co" *ns* nil)
    => (just ["compliment.core/completions"])

    (core/completions "core/doc" *ns* nil)
    => (just ["core/documentation"]))

  (fact "in case of non-existing namespace doesn't fail"
    (core/completions "redu" nil nil) => anything
    (core/completions "n-m" 'foo.bar.baz nil) => anything)

  (fact "many sources allow some sort of fuzziness in prefixes"
    (core/completions "re-me" nil)
    => (just #{"remove-method" "reset-meta!" "remove-all-methods"})

    (core/completions "remme" nil)
    => (just [#"remove-method" "remove-all-methods"])

    (core/completions "cl.co." nil)
    => (contains #{"clojure.core.protocols" "clojure.core.unify"} :gaps-ok)

    (core/completions "clcop" nil)
    => ["clojure.core.protocols"]

    (core/completions ".gSV" nil)
    => (just #{".getSpecificationVersion" ".getSpecificationVendor"}))

  (fact "candidates are sorted by their length first, and then alphabetically"
    (core/completions "map" nil)
    => (contains ["map" "map?" "mapv" "mapcat" "map-indexed"])

    (core/completions "al-" nil)
    => ["all-ns" "alter-meta!" "alter-var-root"])

  (fact "context can help some sources to give better candidates list"
    (def a-str "a string")
    (core/completions ".sub" "(__prefix__ a-str)")
    => (just #{".subSequence" ".substring"})

    (def a-big-int 42M)
    (core/completions ".sub" "(__prefix__ a-big-int)")
    => [".subtract"]))

(facts "about documentation"
  (fact "`documentation` takes a symbol string which presumably can be
  resolved to something that has a docstring.")
  (core/documentation "reduce")         => (every-pred string? not-empty)
  (core/documentation ".suspend")       => (every-pred string? not-empty)
  (core/documentation "jus.t g:arbage") => "")
