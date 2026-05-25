package com.clovercraftsmp.clover.mixin.fix.supplementaries;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.mehvahdjukaar.supplementaries.common.items.components.SafeOwner;
import net.minecraft.core.UUIDUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@IfModLoaded("supplementaries")
@Mixin(SafeOwner.class)
public class SafeOwnerMixin {
    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
            )
    )
    private static Codec<SafeOwner> fixCodec(Codec<SafeOwner> original) {
        return RecordCodecBuilder.create(
                instance -> instance.group(
                                UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(s -> Optional.ofNullable(s.owner())),
                                Codec.STRING.optionalFieldOf("owner_name").forGetter(s -> Optional.ofNullable(s.ownerName())),
                                Codec.STRING.optionalFieldOf("password").forGetter(s -> Optional.ofNullable(s.password()))
                        )
                        .apply(instance, SafeOwnerAccessor::invokeNew)
        );
    }
}
