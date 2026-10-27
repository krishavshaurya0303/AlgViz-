package com.algoviz.model;

/** Top-level groups shown in the Algorithm Library; each maps to one visualizer module. */
public enum AlgorithmCategory {
    SORTING("Sorting", ModuleId.SORTING),
    GRAPH("Graph & Pathfinding", ModuleId.GRAPH),
    TREE("Trees", ModuleId.TREE),
    LINKED_LIST("Linked Lists", ModuleId.LINKED_LIST);

    private final String displayName;
    private final ModuleId module;

    AlgorithmCategory(String displayName, ModuleId module) {
        this.displayName = displayName;
        this.module = module;
    }

    public String displayName() {
        return displayName;
    }

    /** The module that visualizes this category. */
    public ModuleId module() {
        return module;
    }
}
