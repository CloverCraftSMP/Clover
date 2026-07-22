package com.clovercraftsmp.clover.mixin.fix.supplementaries;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.mehvahdjukaar.supplementaries.common.items.components.SafeOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@IfModLoaded("supplementaries")
@Mixin(SafeOwner.class)
public interface SafeOwnerAccessor {
    @Invoker("<init>")
    static SafeOwner invokeNew(
            Optional<UUID> owner,
            Optional<String> ownerName,
            Optional<String> password
    ) {
        throw new AssertionError();
    }
}
