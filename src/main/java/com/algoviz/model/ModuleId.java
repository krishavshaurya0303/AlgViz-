package com.algoviz.model;

/** The screens reachable from the navigation sidebar. */
public enum ModuleId {
    DASHBOARD("Dashboard", "Overview and quick start", "DashboardView.fxml"),
    SORTING("Sorting Studio", "Compare, swap and merge — watch five sorting algorithms work", "SortingView.fxml"),
    GRAPH("Graph Explorer", "Breadth-first, depth-first and Dijkstra on an editable grid", "GraphView.fxml"),
    TREE("Tree Visualizer", "Binary search tree insertion, search, deletion and traversals", "TreeView.fxml"),
    LINKED_LIST("Linked List Lab", "Pointers, nodes and links in a singly linked list", "LinkedListView.fxml"),
    LIBRARY("Algorithm Library", "Explanations, pseudocode and complexity for every algorithm", "LibraryView.fxml"),
    SETTINGS("Settings", "Animation, defaults and preferences", "SettingsView.fxml");

    private final String title;
    private final String subtitle;
    private final String fxml;

    ModuleId(String title, String subtitle, String fxml) {
        this.title = title;
        this.subtitle = subtitle;
        this.fxml = fxml;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }

    public String fxml() {
        return fxml;
    }

    /** True for the four visualizer modules (the ones that run algorithms). */
    public boolean isVisualizer() {
        return this == SORTING || this == GRAPH || this == TREE || this == LINKED_LIST;
    }
}
