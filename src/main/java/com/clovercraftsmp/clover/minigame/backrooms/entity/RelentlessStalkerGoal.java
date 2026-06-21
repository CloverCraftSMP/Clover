package com.clovercraftsmp.clover.minigame.backrooms.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class RelentlessStalkerGoal extends Goal {
    private final BackroomsEndermanEntity mob;
    private final double speedModifier;
    private ServerPlayer targetPlayer;

    public RelentlessStalkerGoal(BackroomsEndermanEntity mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTargetPlayerId() == null) {
            return false;
        }

        if (this.mob.getServer() != null) {
            this.targetPlayer = this.mob.getServer().getPlayerList().getPlayer(this.mob.getTargetPlayerId());
        }

        return this.targetPlayer != null && this.targetPlayer.isAlive() && !this.targetPlayer.isSpectator();
    }

    @Override
    public void tick() {
        if (this.targetPlayer != null) {
            this.mob.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

            this.mob.getNavigation().moveTo(this.targetPlayer, this.speedModifier);
        }
    }
}
