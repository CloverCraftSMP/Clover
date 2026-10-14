package com.clovercraftsmp.clover.mixin.feature.status;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.maxhenkel.status.Status;
import de.maxhenkel.status.playerstate.PlayerState;
import org.spongepowered.asm.mixin.Mixin;

//? if <=1.21.1 {
/*import net.minecraft.world.level.GameRules;
*///? }

//? if >=26.1 {
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
//? }

@IfModLoaded("status")
@Mixin(GameRules.class)
public class GameRulesMixin {
    //? if <=1.21.1 {
    /*@WrapMethod(method = "getInt")
    private int wrapSleepingPercentage(GameRules.Key<GameRules.IntegerValue> key, Operation<Integer> original) {
        if (key == GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) {
            boolean noSleep = Status.STATE_MANAGER.getStates().stream().anyMatch(PlayerState::isNoSleep);
            if (noSleep) return 101;
        }

        return original.call(key);
    }
    *///? }

    //? if >=26.1 {
    @WrapMethod(method = "get")
    private <T> T wrapSleepingPercentage(GameRule<T> gameRule, Operation<T> original) {
        if (gameRule == GameRules.PLAYERS_SLEEPING_PERCENTAGE) {
            boolean noSleep = Status.STATE_MANAGER.getStates().stream().anyMatch(PlayerState::isNoSleep);
            if (noSleep) //noinspection unchecked
                return (T) (Integer) 101;
        }
        return original.call(gameRule);
    }
    //? }
}