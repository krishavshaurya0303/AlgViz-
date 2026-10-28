# AlgoViz — Viva Preparation Notes

Short, code-referenced answers. File paths are under `src/main/java/com/algoviz/`.

### 1. Why JavaFX instead of Swing?
JavaFX has a scene graph with hardware-accelerated rendering, CSS styling, FXML for declarative layouts,
built-in animation (`Timeline`, `AnimationTimer`, transitions) and observable properties/bindings. Swing has none
of the CSS/FXML separation and needs manual repainting for animation. Our dark theme lives entirely in
`resources/com/algoviz/css/*.css` (colours mirrored for canvas drawing in `ui/theme/Palette`), layouts in
`resources/com/algoviz/fxml/*.fxml`.

### 2. Why MVC?
To keep algorithm logic testable and independent from the screen. **Model**: `engine/**`, `model/**`
(no JavaFX imports). **View**: FXML + CSS + `ui/render` canvases. **Controller**: `controller/*Controller`.
Because the engine is UI-free, 1096 of the 1097 tests run without starting JavaFX (only `UiSmokeTest` needs it).

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
**Previous** = `cursor--` and render that snapshot — array, highlights **and counters** come back exactly,
because counters are stored per step (`ExecutionMetrics` is immutable). Nothing is "undone" or recomputed, so
forward/backward any number of times is consistent (`PlaybackControllerTest.repeatedForwardBackwardIsConsistent`).
Memory is bounded by input limits (≤150 elements, ≤665 grid cells).

### 7. How is thread safety maintained?
* All UI updates and all playback state changes happen on the JavaFX Application Thread: Previous/Next are
  button and key handlers, which JavaFX always runs on that thread, and they call `FxPlaybackDriver` directly —
  no worker thread touches nodes. (The driver also has a `Timeline` for timed playback, which runs on the same
  thread; the current UI does not use it.)
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
1097 JUnit 5 tests (see README §13). Highlights: every sort on 50 inputs × 2 orders vs `Arrays.sort`, plus the
invariant "never marked sorted early"; BFS/Dijkstra optimality vs independent reference implementations on
random grids; 2000 random BST operations vs `java.util.TreeSet`; linked-list link integrity after every
operation; playback step consistency with a fake clock; spring physics for draggable nodes; and a UI smoke test
that drives the real application with Previous/Next.

### 14. Why only Previous and Next, no Play button?
A learner (or an examiner) controls the pace: each step stays on screen with its explanation and pseudocode line
until they choose to move on, and nothing changes behind their back. The engine still supports timed playback
(`PlaybackController.play()`/`tick()`, tested in `PlaybackControllerTest`), so this is a UI choice, not a missing
feature.

### 15. How do the draggable nodes work without breaking the structure?
`ui/animation/SpringField` never moves a node's *layout* position — it only stores a displacement from it. Each
frame, three forces act on the displacement: a spring back home, springs along each link towards the neighbours'
displacement (so a dragged node pulls its neighbours like a rope), and a push away from nodes that come too close;
damping makes it settle. On release every displacement decays to 0, so the BST stays in sorted order and the list
in link order. The physics is plain arithmetic and is unit-tested without JavaFX (`SpringFieldTest`).

### 16. How does the memory view relate to dynamic memory allocation?
Every node is created at run time, so it lives on the **heap**; the operation's variables (`head`, `prev`,
`current`, `root` …) live on the **stack** and only hold addresses. `engine/memory/MemoryLayout` works this out
from each step's snapshot: a node that is new in this step was just allocated, a node that is gone was released.
* **C model**: `malloc(sizeof(struct node))` reserves 16 bytes (int data + padding + 8-byte pointer) for a list
  node, 24 for a tree node; deleting calls `free(p)`, which returns the block to the allocator. Forgetting `free`
  would be a memory leak; using `p` after `free` would be a dangling pointer.
* **Java model**: `new Node(v)` creates the object; a variable holds a *reference*. Java has no `free`: a node
  that no variable can reach any more is garbage, and the garbage collector reclaims it later. So leaks of this
  kind cannot happen, but you do not control *when* memory is released.
The addresses are illustrative (stable per node id), because Java never exposes real addresses.

### Bonus: how are metrics kept honest?
Counters are incremented only inside recorders at the moment the operation happens; each `MetricType` has a
written definition shown as a tooltip. No timing is shown on screen (the old "Elapsed" counter was removed with the
Play button, because manual stepping would make it meaningless); the step-recording time under the sorting chart is
labelled "not a performance benchmark". Complexity cards are titled "Theoretical complexity" and come from
documentation, not from measurements.
