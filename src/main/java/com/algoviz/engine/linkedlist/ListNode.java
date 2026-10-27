package com.algoviz.engine.linkedlist;

/** Mutable singly-linked node; only {@link SinglyLinkedList} modifies it. */
final class ListNode {
    final int id;
    final int value;
    ListNode next;

    ListNode(int id, int value) {
        this.id = id;
        this.value = value;
    }
}
