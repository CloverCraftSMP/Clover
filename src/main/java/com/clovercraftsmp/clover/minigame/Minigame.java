package com.clovercraftsmp.clover.minigame;

public interface Minigame {
    String getId();

    void registerWorldGen();

    void registerEntities();

    void registerEvents();
}
