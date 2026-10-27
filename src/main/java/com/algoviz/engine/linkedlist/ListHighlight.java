package com.algoviz.engine.linkedlist;

public enum ListHighlight {
    /** Node the "current" pointer is on. */
    CURRENT,
    /** Already traversed. */
    VISITED,
    /** Search hit. */
    FOUND,
    /** Newly created node. */
    NEW,
    /** Node being removed. */
    REMOVING
}
