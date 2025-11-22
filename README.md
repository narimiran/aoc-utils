# AoC Utils: Helper functions for Advent of Code


These are some helper functions which I have collected
(and changed and, hopefully, improved) over the years of solving
Advent of Code in Clojure.

I usually kept them inside of each AoC repo, but now I have decided
to make a package out of it because of two reasons:
- now I don't have to copy-paste it every year, and due to some modifications
  end up with several slightly different versions of the same thing
- maybe there's somebody else who finds this useful and uses it for
  their AoC solving.
  
  
  
## Installing

Add the following to your `deps.edn` file:

``` clojure
{:deps
  ; other deps
  ,,,
  ; check the latest tag and sha in the releases/tags: 
  ; https://github.com/narimiran/aoc-utils/tags
  com.github.narimiran/aoc-utils {:git/tag "v0.8.2" :git/sha "e439cb6"}
}
```


  
## Usage

``` clojure
(ns day01
  (:require [aoc-utils.core :as aoc]))

(->> (aoc/parse-lines (aoc/read-input 1) row-parsing-function)
     (aoc/sum-by row-function)
```



## Documentation

The documentation is available at:
https://narimiran.github.io/aoc-utils




## FAQ

> Why is there no function to automatically retrieve input file for a given task?

Because I'm oldschool and I never used such functions/tools.
I like to visually inspect the input file before I manually copy it.

If I get the information that this repo has more users than just myself
(and they would like to have it), I'll consider adding it.


&nbsp;


> Why does the `read-input` function use (this) hard-coded location of inputs?

Because my AoC repos usually contain solutions in multiple languages and I keep
inputs external to all of them, like this:

``` shell
.
├── clojure
│   ├── deps.edn
│   └── src
│       ├── day01.clj
│       └── day02.clj
├── inputs
│   ├── 01.txt
│   └── 02.txt
├── python
│   ├── day01.py
│   └── day02.py
├── LICENSE.txt
└── README.md
```

If your repo has a different structure, it is probably the best to not use the
`read-input` function and to use `slurp` directly.
Or write your own version of the function.
