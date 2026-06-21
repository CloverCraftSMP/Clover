package com.clovercraftsmp.clover.minigame;

public interface Minigame {
    MinigameTypes getId();

    void registerWorldGen();

    void registerEntities();

    void registerEvents();
}
