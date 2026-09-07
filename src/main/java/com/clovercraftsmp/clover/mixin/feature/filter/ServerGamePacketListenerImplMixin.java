package com.clovercraftsmp.clover.mixin.feature.filter;
//? if <=1.21.1 {
/*import com.clovercraftsmp.clover.util.filter.Filter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @WrapOperation(method = "handleUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult onUseItem(ServerPlayerGameMode instance, ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, Operation<InteractionResult> original) {
        if (serverPlayer.gameMode.getGameModeForPlayer().isBlockPlacingRestricted() || !itemStack.is(Items.WRITABLE_BOOK) || !Filter.isFilter(itemStack) || !serverPlayer.isCrouching()) {
            return original.call(instance, serverPlayer, level, itemStack, interactionHand);
        }

        Filter filter = Objects.requireNonNull(Filter.fromItem(itemStack));
        return filter.handleUnpack(serverPlayer, level, itemStack, interactionHand) ? InteractionResult.CONSUME : original.call(instance, serverPlayer, level, itemStack, interactionHand);
    }

    @WrapOperation(method = "signBook", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void onSignBook(Inventory instance, int i, ItemStack itemStack, Operation<Void> original) {
        if (Filter.isFilter(itemStack)) {
            itemStack.remove(DataComponents.ITEM_NAME);
        }
        original.call(instance, i, itemStack);
    }
}
*///?}