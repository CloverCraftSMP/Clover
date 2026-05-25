package com.clovercraftsmp.clover.mixin.fix.modpack_checker;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import modpackChecker.ModpackChecker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("modpack-checker")
@Mixin(ModpackChecker.class)
public class ModpackCheckerMixin {
    @Unique
    private static boolean listenerRegistered = false;

    @Inject(
            method = "onServerStarted",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelBadReloadRegister(MinecraftServer mcserver, CallbackInfo ci) {
        if (!listenerRegistered) {
            ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
                    (server, resourceManager, success) -> {
                        if (success)
                            modpackChecker.ConfigManager.reload();
                    }
            );
            listenerRegistered = true;
        }

        ci.cancel();
    }
}
