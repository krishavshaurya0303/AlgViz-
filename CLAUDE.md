# CLAUDE.md — AlgoViz project memory

Claude Code loads this file automatically at the start of every session in this folder.
It records the full history and every decision from the original cloud session that built the project.

## Owner and working style
- Owner: Krishav (GitHub `krishavshaurya0303`, repo `krishavshaurya0303/AlgViz-`, branch `main`).
- Purpose: college DSA + Java OOP project, to be demoed to a Head of Department and defended in a viva.
- Wants replies that are strict, blunt, concise and complete.
- When teaching: one small idea at a time, with ASCII traces before code, one question at a time.
- Machine now: **Windows laptop, Java 27 installed**. The working copy is `Downloads\AlgoViz\AlgoViz` (an older unzipped copy sits in `Documents\AlgoViz`; don't edit it). The app has been confirmed to open via `run-windows.bat`.
- The original build happened in a Linux cloud container (OpenJDK 21, Maven 3.9.11, Xvfb).

## What AlgoViz is
A JavaFX desktop app (Java 17+ target, JavaFX 21.0.5, Maven, JUnit 5) that visualizes DSA by running **real
instrumented implementations** that record every operation as an immutable snapshot ("step"). The UI plays
the steps back with play/pause/step back/step forward/reset/seek/speed, highlights the matching pseudocode line,
explains each step in plain English, and shows counters.

Modules: Dashboard, Sorting Studio (Bubble/Selection/Insertion/Merge/Quick), Graph Explorer (BFS/DFS/Dijkstra
on a 19×35 grid with walls and weighted cells of cost 5, 4-directional movement), Tree Visualizer (standard
unbalanced BST: insert/search/delete in all 3 cases plus inorder/preorder/postorder/level-order), Linked List
Lab (singly linked list with a head pointer only: insert/delete at head/tail/index, search, traverse, reverse,
clear), Algorithm Library, Settings.

## Commands
```
run-windows.bat                  # double-click launcher (uses the Maven Wrapper; no Maven install needed)
mvnw.cmd javafx:run              # same thing from a terminal
mvnw.cmd test                    # run all tests (the UI smoke test is skipped without a DISPLAY env var)
mvnw.cmd package                 # also builds target/algoviz-1.0.0-app.jar (platform-specific fat jar)
```
On Linux a headless UI test run is `xvfb-run -a mvn test`.
Last verified result: **1086 tests, 0 failures**. Confirmed on Linux/JDK 21 and on Windows/Java 27 (2026-10-27: 1086 run, 0 failures, 1 skipped = `UiSmokeTest`, no DISPLAY). The stack trace printed during `SettingsServiceTest` is the intentional "Could not read settings" path, not a failure.

## Architecture (where things are)
`src/main/java/com/algoviz/`
- `engine/`: UI-free core.
  - `Algorithm<I,S>` (Strategy).
  - `ExecutionStep` (immutable snapshot interface) and `ExecutionTrace<S>`.
  - `ExecutionHistory<S>` (cursor), `PlaybackController<S>` (state machine EMPTY/READY/PLAYING/PAUSED/FINISHED, `tick()`, generation counter, listeners).
  - `Stopwatch` (nanoTime, excludes paused time, injectable clock).
- `engine/sorting/`:
  - `SortingAlgorithm` (Template Method: `run()` → `sort(SortRecorder)`).
  - `SortRecorder`: an instrumented array. All compare/swap/write calls go through it, which counts and records them.
  - `SortStep`, `SortOrder`.
  - The 5 sorts. Quick Sort is Lomuto with the last element as pivot. Insertion Sort uses shifts plus a held key. Merge Sort is top-down with a visible buffer.
- `engine/graph/`:
  - `Graph` interface with two implementations: `GridGraph` (editable) and `AdjacencyListGraph` (rejects negative weights).
  - `GraphSearchAlgorithm` (template; rebuilds the path from parent pointers; handles the no-path case).
  - `BreadthFirstSearch`, `DepthFirstSearch` (iterative; marks visited on pop), `Dijkstra` (PriorityQueue with lazy deletion and seq tie-breaks).
- `engine/tree/`: `BinarySearchTree` (traced ops mutate the tree and return a trace), `TreeSnapshot` (nodes have stable ids), `TreeOperation` (includes VIEW).
- `engine/linkedlist/`:
  - `SinglyLinkedList`: each pointer assignment is a separate step.
  - `ListSnapshot` keeps the layout order stable during an operation, so the arrows visibly flip during reverse.
- `model/`:
  - `ExecutionMetrics` (immutable EnumMap snapshot), `MetricType` (each has a precise definition, used as the tooltip).
  - `AlgorithmMetadata`, `ModuleId`, `UserSettings`.
- `service/`: `AlgorithmCatalog` (strategy registry plus docs), `SettingsService` (`~/.algoviz/settings.properties`, atomic save, tolerant of bad files).
- `controller/`:
  - `MainController`: shell, lazy FXML loading, keyboard shortcuts, help dialog.
  - `AppContext`: manual DI through the FXML controller factory. There are no singletons.
  - Base classes: `VisualizerController<S>` (wires the playback bar, explanation, pseudocode and metrics) and `StaticPageController`.
  - One controller per screen.
- `ui/animation/`:
  - `FxPlaybackDriver`: a Timeline calls `tick()` and the speed sets the Timeline rate.
  - `Tween`: AnimationTimer-based easing.
  - `BackgroundRunner`: JavaFX Task plus a ticket so only the newest result is delivered.
- `ui/render/`: `ResizableCanvas` base plus `SortingCanvas`, `GridCanvas`, `TreeCanvas` (x = inorder rank, y = depth), `LinkedListCanvas`.
- `ui/component/`: `PlaybackBar`, `MetricsPanel`, `PseudocodePanel`, `ExplanationCard`, `ComplexityCard`, `Legend`, `EmptyState`, `NotificationCenter`, `Icons`/`IconType` (custom SVG paths), `BrandMark`.
- `ui/theme/`: `Palette` (mirrors the CSS tokens), `FontLoader` (bundled Inter and JetBrains Mono, both OFL).
- Resources:
  - `resources/com/algoviz/fxml/*.fxml`: 8 views.
  - `css/app.css`: design tokens, defined as looked-up colours in `.root`.
  - `css/components.css` and `css/modules.css`.
- Tests in `src/test/java/com/algoviz/`:
  - Engine, service and util tests, plus `ui/UiSmokeTest`.
  - `tools/ScreenshotHarness`: a dev tool that scripts UI clicks and saves PNGs.

## Key design decisions (and why)
- **Step Backward = snapshot cursor.** Every step stores a full copy of the data and the counters, so going back restores the exact earlier state. Nothing is undone or recomputed.
- **Engine has zero JavaFX imports**, so it is testable without a UI.
- **All UI and playback changes happen on the FX thread.** Heavy trace building (sorting, graph) runs in a background Task on copies of the input. Stale results are dropped via tickets.
- **Honest metrics.**
  - Writes count array assignments; a swap counts as 2 writes. Insertion shifts and merge copies count as writes, never swaps.
  - Elapsed time is playback wall-clock time, not a benchmark.
  - Complexity cards are labelled "theoretical".
- **Never mark an element sorted early.** A test enforces this for every step of every sort.
- **BST does not self-balance**, and the app says so. AVL was deliberately not built.
- **Input limits**: sort 2–150 values in the range −999..999; tree ≤ 63 nodes; list ≤ 10 nodes.
- Single dark theme and no sound, so neither has a setting (this is stated in the Settings screen).

## Bugs found and fixed during the build (don't reintroduce)
- `Timeline.playFromStart()` writes `rate`, so the ticker's rate must NOT be bound. A listener on speed sets it instead.
- Nav and card focus rings use `:focus-visible`. Using `:focused` caused a double-selected look.
- Long pseudocode lines wrapped badly. They were shortened and the side panel widened (`minWidth 340`, divider at 0.7).
- In tests and the harness, matching buttons by tooltip prefix "Play" also hits the Settings nav tooltip ("Playback, …"). Use `.play-button` instead.
- `SettingsServiceTest`: `Files.move` replaces an *empty* directory, so the unwritable-file test uses a non-empty directory.

## Session history (chronological)
1. User gave a full master spec for "AlgoViz". Built from an empty repo:
   1. Maven/JavaFX pom.
   2. Engine core.
   3. The 5 sorts, the graph searches, BST and linked list.
   4. 1085 engine tests (all passing), committed as "Add AlgoViz engine…".
2. Built the full JavaFX UI (shell, 7 screens, CSS design system, canvases, components). Verified it with screenshots under Xvfb, fixed the visual issues, added `UiSmokeTest`, and wrote the README (17 sections, Mermaid diagram) and `docs/VIVA.md`. 1086 tests passed. Committed and pushed.
3. User asked for time estimates for extras: AVL ~30–45 min, A* ~15–20 min, resizable grid ~15 min, light theme ~20–30 min, jpackage installer ~15 min.
4. Sent screenshots (`docs/screenshots/`). Explained that a cloud session cannot run on the laptop.
5. Added the Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`) and launchers (`run-windows.bat`, `run-mac-linux.sh`). Sent `AlgoViz.zip`. User unzipped it to `Documents\AlgoViz` on Windows and the app opened (Java 27).
6. User is moving to a local session on the laptop. This file was written as the handoff.
7. Local session (2026-10-27): ran the suite on Windows/Java 27 (all pass). winget is broken on the laptop, so Git for Windows 2.56.0.4 was installed from the official GitHub release. The working copy was attached to the remote with `git init` + fetch + `git reset origin/<branch>`.

## Known limitations / possible next work
- AVL (or Red-Black) tree with rotation animation.
- A* in the Graph Explorer; adjustable grid size and weight value.
- Light theme.
- jpackage Windows installer.
- On Windows, `UiSmokeTest` is skipped unless a `DISPLAY` env var is set; it can be enabled by removing `@EnabledIfEnvironmentVariable`.
- The JavaFX 21 + Java 27 combination works for launching. If issues appear, bump `javafx.version` in `pom.xml`.

## Git
- The only branch is `main` (renamed on 2026-10-27 from the original `claude/algoviz-javafx-dsa-qsn47c`).
- Remote: `origin` = `https://github.com/krishavshaurya0303/AlgViz-.git`. Local `main` tracks `origin/main`, with `core.autocrlf true`.
- Git lives at `C:\Program Files\Git\cmd`. Shells opened before the install may need `$env:Path = "C:\Program Files\Git\cmd;$env:Path"`.
- Push only when the user says so. The first push opens a GitHub sign-in window for the user.
- Commit messages so far end with a `Co-Authored-By` line.
