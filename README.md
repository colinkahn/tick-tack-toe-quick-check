# tick-tack-toe quick-check

A simple demo of using quick-check to shrink sequences of operations.

## What it does

It helps if you are familiar with model based testing using quick-check, but
I'll touch on a few details:

- We generate some operations our system can handle, in this case it's just
  playing a position on the board. 
- We define a function `apply-op` which takes our current ctx (or state) and
  applies an operation.
- We define a property using `for-all` and run quick-check on it.

Usually your property will be some failure case, but for demonstration purposes
I've made mine the success case that the game has won. This means that when
quick-check hits that condition and fails it will start to shrink the input
sequence of operations until it gets to the most minimal set.

For instance, if I ran it now I would get a failure for these operations:

```
[[{:player :y, :position [1 2]}
  {:player :y, :position [0 2]}
  {:player :y, :position [0 2]}
  {:player :y, :position [2 2]}
  {:player :x, :position [0 1]}
  {:player :x, :position [2 0]}
  {:player :y, :position [0 1]}
  {:player :y, :position [1 1]}
  {:player :y, :position [0 2]}
  {:player :x, :position [2 1]}
  {:player :x, :position [1 2]}
  {:player :y, :position [0 0]}
  {:player :x, :position [1 0]}
  {:player :y, :position [2 1]}
  {:player :x, :position [2 0]}
  {:player :y, :position [2 2]}
  {:player :x, :position [1 1]}
  {:player :y, :position [2 1]}
  {:player :x, :position [2 2]}
  {:player :y, :position [2 2]}
  {:player :x, :position [1 0]}
  {:player :x, :position [2 0]}
  {:player :x, :position [1 2]}
  {:player :x, :position [2 2]}
  {:player :x, :position [2 0]}
  {:player :y, :position [2 0]}
  {:player :x, :position [2 1]}
  {:player :y, :position [0 2]}
  {:player :y, :position [0 2]}
  {:player :x, :position [2 2]}]]
```

But through the magic of shrinking the smallest is:

```
[[{:player :x, :position [1 2]}
  {:player :y, :position [2 2]}
  {:player :x, :position [1 1]}
  {:player :y, :position [0 2]}
  {:player :x, :position [1 0]}]]
```

Nice!
