package com.algoviz.controller;

/**
 * Optional instruction passed when navigating, e.g. "select Dijkstra".
 * Modules ignore requests they do not understand.
 *
 * @param algorithmId algorithm / entry to select, or null
 */
public record NavigationRequest(String algorithmId) {

    public static final NavigationRequest NONE = new NavigationRequest(null);

    public static NavigationRequest select(String algorithmId) {
        return new NavigationRequest(algorithmId);
    }
}
