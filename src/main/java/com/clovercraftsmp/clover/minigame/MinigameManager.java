package com.clovercraftsmp.clover.minigame;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.minigame.backrooms.BackroomsMinigame;

import java.util.HashMap;
import java.util.Map;

public class MinigameManager {
    private static final Map<MinigameTypes, Minigame> REGISTRY = new HashMap<>();
    private static Minigame activeMinigame = null;

    static {
        register(new BackroomsMinigame());
    }

    private static void register(Minigame minigame) {
        REGISTRY.put(minigame.getId(), minigame);
    }

    public static void initializeActiveMinigame(MinigameTypes configMode) {
        if (REGISTRY.containsKey(configMode)) {
            activeMinigame = REGISTRY.get(configMode);
            Clover.LOGGER.info("Starting Minigame Server Mode: " + activeMinigame.getId());

            activeMinigame.registerWorldGen();
            activeMinigame.registerEntities();
            activeMinigame.registerEvents();
        } else {
            Clover.LOGGER.info("Running in SMP Mode.");
        }
    }

    public static Minigame getActiveMinigame() {
        return activeMinigame;
    }
}
