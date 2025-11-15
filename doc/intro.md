# Intro

`aoc-utils` is a collection of various helper functions I usually
use for solving Advent of Code (AoC) tasks.


## Highlights

- Read the contents of an input file with `aoc/read-input`.
  This is usually just `(aoc/read-input 1)`, and if I need to
  read the file containing test input, e.g. `01_test.txt`
  then I use `(aoc/read-input "01_test")`
- The input file usually contains multiple lines of data,
  which are parsed with the `parse-lines` function.
  For the details on how to parse different datatypes, see below.
- A very frequent task type is the one where we have a 2D-grid.
  For converting the input to a datatype that can be easily
  used as a grid, see `grid->point-map` and `grid->point-set` functions.
  There are also some helpers to manipulate 2D points,
  `pt+`, `pt-`, `pt*`, and their 3D equivalents (`pt-3d+`, etc.).
- Some tasks are graph traversal problems, and for those
  there are four options: `dfs`, `bfs`, `a-star`, `dijkstra`.
  These try to be as general as possible to fit different tasks,
  but they are not as performant as a specialized function for
  a given task.




## Reading input files

The [`read-input`](aoc-utils.core.html#var-read-input)
function has the folowing assumptions:

  - inputs are in the sibling `../inputs` directory
  - inputs have `.txt` extension
  - if passing a single digit as a parameter, it zero-pads it

E.g. `(read-input 1)` slurps `../inputs/01.txt` file.







## Input parsing

The [`aoc/parse-input`](aoc-utils.core.html#var-parse-input)
function is doing some heavy lifting.\
The `parse-fn` parameter there is the key to the versatility:
it makes possible to parse all AoC inputs, either by the typical
functions (see the list below) or by passing it a custom function,
specially crafted for the task at hand.

Possible "built-ins" to pass to the `parse-fn` parameter:

- `:int` - parse a single integer
- `:ints`- get all integers
- `:digits` - extract all single digits
- `:chars` - make a list of chars
- `:words` - make a list of words

This function is usually used on single-line inputs.


----


Parsing a whole multi-line input file is just a matter of applying
that function to each line of the input, and there is
[`aoc/parse-lines`](aoc-utils.core.html#var-parse-lines) that does exactly that.

On some rare occasions, the input is split into paragraphs by having an
empty line between different parts of input.
We have a function for that too: 
[`aoc/parse-paragraphs`](aoc-utils.core.html#var-parse-paragraphs).







## Grids

AoC wouldn't be AoC if there aren't many tasks where you're given a 2D
grid (sometimes even 3D).

It is important to have a usable representation of a grid.\
Sometimes we need to know a character at each coordinate
and we can use the
([`grid->point-map`](aoc-utils.core.html#var-grid-.3Epoint-map)) function
to represent the grid,
the other times only the coordinates are important and we can use the
([`grid->point-set`](aoc-utils.core.html#var-grid-.3Epoint-set)) function.\
For both functions, we can only keep the coordinates that satisfy the `pred` function.


Sometimes we need to inspect a grid.
With the [`points->lines`](aoc-utils.core.html#var-points-.3Elines) function,
we create a printable string of all points in the grid.


----

### 2D grids

Here are some functions that help us with points in 2D grids:

- [`manhattan`](aoc-utils.core.html#var-manhattan)
- [`pt+`](aoc-utils.core.html#var-pt.2B)
- [`pt-`](aoc-utils.core.html#var-pt-)
- [`pt*`](aoc-utils.core.html#var-pt*)
- [`left-turn`](aoc-utils.core.html#var-left-turn)
- [`right-turn`](aoc-utils.core.html#var-right-turn)
- [`inside?`](aoc-utils.core.html#var-inside.3F)
- [`neighbours-4`](aoc-utils.core.html#var-neighbours-4)
- [`neighbours-8`](aoc-utils.core.html#var-neighbours-8)



### 3D grids

For 3D grids, we have variants of the functions above:

- [`manhattan-3d`](aoc-utils.core.html#var-manhattan-3d)
- [`pt-3d+`](aoc-utils.core.html#var-pt-3d.2B)
- [`pt-3d-`](aoc-utils.core.html#var-pt-3d-)
- [`pt-3d*`](aoc-utils.core.html#var-pt-3d*)
- [`inside-3d?`](aoc-utils.core.html#var-inside-3d.3F)
- [`neighbours-3d`](aoc-utils.core.html#var-neighbours-3d)




## Graph traversal

Graph traversal problems are relatively frequent in AoC.

I've written a huge general function for traversing grids, but
it remains to be seen how useful it'll be for specific tasks with
their specific needs: I feel like in the recent years the graph traversal
tasks always had some gotcha which made it harder to use a general algorithm,
rather than a specific one written for the task at hand.

If this gets used, the implementation details will probably change,
depending on the specific tasks.
(As if the (private) `traverse` function is not already way too long
and complicated.

All four algorithms (`DFS`, `BFS`, `Dijkstra`, `A*`) share the same logic,
the difference is in a `queue` type, they are available as functions:

- [`dfs`](aoc-utils.core.html#var-dfs)
- [`bfs`](aoc-utils.core.html#var-bfs)
- [`dijkstra`](aoc-utils.core.html#var-dijkstra)
- [`a-star`](aoc-utils.core.html#var-a-star)


The list of possible keys passed in the options map is quite large:
```
[start end walls size size-x size-y
 nb-func nb-num nb-cond end-cond
 cost-fn heuristic steps-limit
 allow-revisits? side-effect]
```

It's best to read the function's source code to understand what each does.






## Utilities

Functions for some common AoC stuff.

- [`transpose`](aoc-utils.core.html#var-transpose):
  We often need to iterate through columns, instead of rows.
  This transposes the matrix.
- [`count-if`](aoc-utils.core.html#var-count-if):
  In many tasks we need to apply some condition and then count
  the number of elements which satisfy it. This should be slightly faster
  (to type, at least) than `(count (filter ...))`.
- [`sum-map`](aoc-utils.core.html#var-sum-map): 
  Similarly, in some tasks we need to map a function to
  each element, and then take a sum of the results.
  There are also `sum-map-indexed`, `sum-pmap`, `prod-map`,
  `max-map` and `max-pmap` variants.
- [`find-first`](aoc-utils.core.html#var-find-first):
  Returns the first element which satisfies a predicate.
  It should be slightly faster than using `(some ...)`.
- [`gcd`](aoc-utils.core.html#var-gcd): Greatest common divisor.
- [`lcm`](aoc-utils.core.html#var-lcm): Least common multiple.
- [`sign`](aoc-utils.core.html#var-sign): Sign of a number.
- [`none?`](aoc-utils.core.html#var-none.3F): A faster version of `not-any?`.
