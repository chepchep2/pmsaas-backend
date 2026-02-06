package com.chep.demo.todo.domain.workspace;

public enum MemberType {
    MEMBER(0),
    INVITATION(1);

    private final int priority;

    MemberType(int priority) {this.priority = priority;}
    public int getPriority() {return priority;}
}
