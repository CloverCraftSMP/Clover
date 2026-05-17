package com.clovercraftsmp.clover.mixin.feature.status;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.maxhenkel.status.Status;
import de.maxhenkel.status.playerstate.PlayerState;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;

@IfModLoaded("status")
@Mixin(GameRules.class)
public class GameRulesMixin {
    @WrapMethod(method = "getInt")
    private int wrapSleepingPercentage(GameRules.Key<GameRules.IntegerValue> key, Operation<Integer> original) {
        if (key == GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) {
            boolean noSleep = Status.STATE_MANAGER.getStates().stream().anyMatch(PlayerState::isNoSleep);
            if (noSleep) return 101;
        }

        return original.call(key);
    }
}
