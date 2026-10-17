package com.clovercraftsmp.clover.companion;

public enum PlayerState {
    NEW, PENDING, APPROVED, BANNED, FAILED;

    public static PlayerState fromWireId(int id) {
        return switch (id) {
            case 0 -> NEW;
            case 1 -> PENDING;
            case 2 -> APPROVED;
            case 3 -> BANNED;
            default -> FAILED;
        };
    }
}
