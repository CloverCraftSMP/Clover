package com.clovercraftsmp.clover.mixin.companion;

import com.clovercraftsmp.clover.companion.CcbotClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.1
import net.minecraft.server.players.NameAndId;

//? if <=1.21.1
//import com.mojang.authlib.GameProfile;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @WrapOperation(method = "canPlayerLogin", at = @At(
            value = "INVOKE",
            //? if >=26.1
            target = "Lnet/minecraft/server/players/PlayerList;isWhiteListed(Lnet/minecraft/server/players/NameAndId;)Z"
            //? if <=1.21.1
            //target = "Lnet/minecraft/server/players/PlayerList;isWhiteListed(Lcom/mojang/authlib/GameProfile;)Z"
    ))
    private boolean wrapIsWhitelisted(
            PlayerList instance,
            //? if <=1.21.1
            //GameProfile profile,
            //? if >=26.1
            NameAndId profile,
            Operation<Boolean> original
    ) {
        boolean preApproved = CcbotClient.APPROVED.contains(
                //? if <=1.21.1
                //profile.getId()
                //? if >=26.1
                profile.id()
        );
        return preApproved || original.call(instance, profile);
    }

}
