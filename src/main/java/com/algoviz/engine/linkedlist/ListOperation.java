package com.algoviz.engine.linkedlist;

import java.util.List;

/** Operations of the Linked List Lab with their pseudocode and complexity. */
public enum ListOperation {
    INSERT_HEAD("Insert at head", "O(1)", List.of(
            "node = new Node(v)",
            "node.next = head",
            "head = node")),
    INSERT_TAIL("Insert at tail", "O(n)", List.of(
            "node = new Node(v)",
            "if head is null: head = node; return",
            "current = head",
            "while current.next is not null: current = current.next",
            "current.next = node")),
    INSERT_AT("Insert at index", "O(n)", List.of(
            "if index == 0: insert at head; return",
            "current = head",
            "repeat index - 1 times: current = current.next",
            "node = new Node(v)",
            "node.next = current.next",
            "current.next = node")),
    DELETE_HEAD("Delete head", "O(1)", List.of(
            "if head is null: report empty list",
            "removed = head",
            "head = head.next")),
    DELETE_TAIL("Delete tail", "O(n)", List.of(
            "if head is null: report empty list",
            "if head.next is null: head = null; return",
            "current = head",
            "while current.next.next is not null: current = current.next",
            "current.next = null")),
    DELETE_AT("Delete at index", "O(n)", List.of(
            "if index == 0: delete head; return",
            "current = head",
            "repeat index - 1 times: current = current.next",
            "removed = current.next",
            "current.next = removed.next")),
    SEARCH("Search", "O(n)", List.of(
            "current = head; index = 0",
            "while current is not null:",
            "    if current.value == v: return index",
            "    current = current.next; index = index + 1",
            "return NOT FOUND")),
    TRAVERSE("Traverse", "O(n)", List.of(
            "current = head",
            "while current is not null:",
            "    visit(current)",
            "    current = current.next")),
    REVERSE("Reverse", "O(n)", List.of(
            "prev = null; current = head",
            "while current is not null:",
            "    next = current.next      // save the rest of the list",
            "    current.next = prev      // redirect the pointer",
            "    prev = current           // advance prev",
            "    current = next           // advance current",
            "head = prev")),
    CLEAR("Clear", "O(1)", List.of(
            "head = null   // nodes become unreachable and are garbage-collected"));

    private final String label;
    private final String time;
    private final List<String> pseudocode;

    ListOperation(String label, String time, List<String> pseudocode) {
        this.label = label;
        this.time = time;
        this.pseudocode = pseudocode;
    }

    public String label() {
        return label;
    }

    /** Worst-case time complexity for a list without a tail pointer. */
    public String time() {
        return time;
    }

    public List<String> pseudocode() {
        return pseudocode;
    }
}
