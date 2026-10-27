# AlgoViz — Viva Preparation Notes

Short, code-referenced answers. File paths are under `src/main/java/com/algoviz/`.

### 1. Why JavaFX instead of Swing?
JavaFX has a scene graph with hardware-accelerated rendering, CSS styling, FXML for declarative layouts,
built-in animation (`Timeline`, `AnimationTimer`, transitions) and observable properties/bindings. Swing has none
of the CSS/FXML separation and needs manual repainting for animation. Our dark theme lives entirely in
`resources/com/algoviz/css/*.css`, layouts in `resources/com/algoviz/fxml/*.fxml`.

### 2. Why MVC?
To keep algorithm logic testable and independent from the screen. **Model**: `engine/**`, `model/**`
(no JavaFX imports). **View**: FXML + CSS + `ui/render` canvases. **Controller**: `controller/*Controller`.
Because the engine is UI-free, 1085 of the 1086 tests run without starting JavaFX.

### 3. What is the Strategy pattern here?
`engine/Algorithm.java` is the strategy interface (`run(I input) → ExecutionTrace<S>`). `BubbleSort`,
`QuickSort`, `BreadthFirstSearch`, `Dijkstra` … are concrete strategies. `SortingController` holds a
`SortingAlgorithm` chosen in a ComboBox and just calls `algorithm.run(in)` — it never checks *which* sort it is.
`service/AlgorithmCatalog` registers all strategies.

### 4. Where is polymorphism used?
* `selected().run(input)` in Sorting/Graph controllers dispatches to the subclass's `sort()` / `search()`.
* `PlaybackController<S extends ExecutionStep>` and `VisualizerController<S>` work with `SortStep`, `GraphStep`,
  `TreeStep`, `ListStep` through the `ExecutionStep` interface (`explanation()`, `pseudocodeLine()`, `metrics()`).
* `ResizableCanvas.redraw()` calls the overridden `draw()` of each canvas.
* `MainController` treats every screen as a `ModuleController` (`onShow`, `onHide`, `handleKey`).

### 5. How are execution events represented?
Each algorithm writes through a *recorder* (`engine/sorting/SortRecorder`, `engine/graph/GraphRecorder`, inner
`Recorder` classes in `BinarySearchTree` and `SinglyLinkedList`). Every operation (compare, swap, write, visit,
pointer change) performs the change, updates counters, and appends an **immutable step**: e.g. `SortStep` holds
the event type, involved indices, a copy of the array, sorted flags, markers (pivot/min/gap), merge buffer,
held key, a metrics snapshot, the pseudocode line and the explanation. The list of steps is an `ExecutionTrace`.

### 6. How does Step Backward work?
Each step is a complete snapshot, and `ExecutionHistory` is just a cursor (`index`) into the trace.
Step Backward = `cursor--` and render that snapshot — array, highlights **and counters** come back exactly,
because counters are stored per step (`ExecutionMetrics` is immutable). Nothing is "undone" or recomputed, so
forward/backward any number of times is consistent (`PlaybackControllerTest.repeatedForwardBackwardIsConsistent`).
Memory is bounded by input limits (≤150 elements, ≤665 grid cells).

### 7. How is thread safety maintained?
* All UI updates and all playback state changes happen on the JavaFX Application Thread: `FxPlaybackDriver`
  uses a `Timeline` whose `KeyFrame` calls `tick()` — no worker thread touches nodes.
* Expensive trace building (sorting, graph) runs in a `javafx.concurrent.Task` on a single daemon thread
  (`ui/animation/BackgroundRunner`). The task works on *copies* (`SortInput` clones the array,
  `GridGraph.copy()`), and its result is delivered on the FX thread only if its ticket is still the newest —
  results of superseded requests are dropped.
* `PlaybackController.generation()` increments on every load/clear; a tick arriving after pause/stop is ignored
  (`onTick` checks `isPlaying()`); switching modules calls `onHide()` → `driver.suspend()`.

### 8. How do the sorting algorithms differ?
* **Bubble**: adjacent compare/swap; largest bubbles to the end each pass; early exit if no swaps (best O(n)).
* **Selection**: scan unsorted part for the minimum, one swap per pass; always O(n²) comparisons; not stable.
* **Insertion**: lift key, shift larger elements right (writes, not swaps), insert; O(n) on sorted input.
* **Merge**: split, recurse, merge using an O(n) buffer; always O(n log n); stable (ties take left).
* **Quick (Lomuto)**: last element pivot, partition, recurse; O(n log n) average, O(n²) on sorted input; not stable.

### 9. Why can BFS find shortest paths in unweighted graphs?
BFS uses a FIFO queue, so nodes are dequeued in non-decreasing distance order: all nodes at distance d are
processed before any at d+1. The first time the target is dequeued, no shorter path can exist. Verified in
`GraphSearchTest.bfsVisitsInNonDecreasingDistanceOrder` and against a reference on 60 random grids.

### 10. Why does Dijkstra require non-negative edge weights?
Dijkstra settles the unsettled node with the smallest distance and never revisits it. That is only correct if no
later path can be cheaper — which holds when every edge adds ≥ 0. A negative edge could make an already-settled
node cheaper. `AdjacencyListGraph.addEdge` rejects negative weights; tests cover zero weights and rejection.

### 11. How does BST deletion work? (`engine/tree/BinarySearchTree.delete`)
1. Find the node (like search), remembering its parent.
2. **Leaf** → parent's link becomes null.
3. **One child** → parent links directly to that child.
4. **Two children** → find the inorder successor (go right once, then left until no left child), copy its value
   into the node, then unlink the successor (it has at most a right child).

### 12. Limitations of an ordinary BST
Its shape depends on insertion order. Inserting sorted values gives a chain of height n − 1, so search/insert/
delete degrade from O(log n) to O(n) (`BinarySearchTreeTest.heightAndDegenerateTree`). Self-balancing trees
(AVL, Red-Black) fix this with rotations; AlgoViz deliberately labels its tree as a standard BST.

### 13. What tests verify correctness?
1086 JUnit 5 tests (see README §13). Highlights: every sort on 50 inputs × 2 orders vs `Arrays.sort`, plus the
invariant "never marked sorted early"; BFS/Dijkstra optimality vs independent reference implementations on
random grids; 2000 random BST operations vs `java.util.TreeSet`; linked-list link integrity after every
operation; playback pause/resume/reset/step consistency with a fake clock; and a UI smoke test that drives the
real application.

### Bonus: how are metrics kept honest?
Counters are incremented only inside recorders at the moment the operation happens; each `MetricType` has a
written definition shown as a tooltip. Elapsed time uses `System.nanoTime()` (monotonic) and excludes paused
time (`engine/Stopwatch`). Complexity cards are titled "Theoretical complexity" and come from documentation,
not from measurements.
