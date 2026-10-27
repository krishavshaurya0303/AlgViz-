package com.algoviz.controller;

/**
 * Optional instruction passed when navigating, e.g. "select Dijkstra" or
 * "run the reverse quick-start". Modules ignore requests they do not understand.
 *
 * @param algorithmId algorithm / entry to select, or null
 * @param action      quick-start action name, or null
 */
public record NavigationRequest(String algorithmId, String action) {

    public static final NavigationRequest NONE = new NavigationRequest(null, null);

    public static NavigationRequest select(String algorithmId) {
        return new NavigationRequest(algorithmId, null);
    }

    public static NavigationRequest action(String algorithmId, String action) {
        return new NavigationRequest(algorithmId, action);
    }
}
