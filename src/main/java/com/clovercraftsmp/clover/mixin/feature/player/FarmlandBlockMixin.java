package com.clovercraftsmp.clover.mixin.feature.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public class FarmlandBlockMixin {
    @Inject(method = "turnToDirt", at = @At("HEAD"), cancellable = true)
    private static void preventTrampling(@Nullable Entity entity, BlockState blockState, Level level, BlockPos blockPos, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity living)) return; // skip natural dehydration etc
        if (!(living instanceof Player player)) {
            ci.cancel();
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> featherFalling = registry.getHolderOrThrow(Enchantments.FEATHER_FALLING);

        if (EnchantmentHelper.getItemEnchantmentLevel(featherFalling, boots) > 0) {
            ci.cancel();
        }
    }
}