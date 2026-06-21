package com.clovercraftsmp.clover.minigame.backrooms.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class BackroomsEndermanEntity extends EnderMan {

    private UUID targetPlayerId;

    public BackroomsEndermanEntity(EntityType<? extends EnderMan> type, Level level) {
        super(type, level);
    }

    public void setTargetPlayerId(UUID uuid) {
        this.targetPlayerId = uuid;
    }

    public UUID getTargetPlayerId() {
        return this.targetPlayerId;
    }

    @Override
    protected void registerGoals() {
        // no super = no normal enderman logic

        this.goalSelector.addGoal(1, new RelentlessStalkerGoal(this, 1.2D));
    }

    // no random tp
    @Override
    protected boolean teleport() {
        return false;
    }
}
