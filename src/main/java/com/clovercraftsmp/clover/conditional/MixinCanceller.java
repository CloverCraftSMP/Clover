package com.clovercraftsmp.clover.conditional;

import java.util.List;

public class MixinCanceller implements com.bawnorton.mixinsquared.api.MixinCanceller {
    private static final List<String> cancelledMixins = List.of(
            "de.maxhenkel.status.mixin.PlayerMixin"
    );

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        return cancelledMixins.contains(mixinClassName);
    }
}
