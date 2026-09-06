package com.clovercraftsmp.clover.mixin.feature.filter;

import com.clovercraftsmp.clover.duck.FilterDuck;
import com.clovercraftsmp.clover.util.filter.Filter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void onUseItemOn(
            ServerPlayer serverPlayer,
            Level level,
            ItemStack itemStack,
            InteractionHand interactionHand,
            BlockHitResult blockHitResult,
            CallbackInfoReturnable<InteractionResult> cir,
            @Share("result") LocalRef<BlockHitResult> result
    ) {
        result.set(blockHitResult);
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/ItemInteractionResult;"))
    private ItemInteractionResult addFilterInteraction(
            BlockState instance,
            ItemStack stack,
            Level level,
            Player player,
            InteractionHand interactionHand,
            BlockHitResult blockHitResult,
            Operation<ItemInteractionResult> original,
            @Share("result") LocalRef<BlockHitResult> result
    ) {
        if (!level.isClientSide() && level.getBlockEntity(result.get().getBlockPos()) instanceof FilterDuck filterDuck && Filter.isFilter(stack)) {
            filterDuck.clover$setFilter(Filter.fromItem(stack));
            player.displayClientMessage(Component.literal("Applied filter!"), true);
            ((ServerPlayer) player).closeContainer();
            return ItemInteractionResult.SUCCESS;
        }

        return original.call(instance, stack, level, player, interactionHand, blockHitResult);
    }
}
