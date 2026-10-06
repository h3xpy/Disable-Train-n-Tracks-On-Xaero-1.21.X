package com.hidetrainmap.mixin;

import com.hidetrainmap.PatchedMarker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server side: never register a player as requesting train map data, so
 * {@code TrainMapSync.send} always finds an empty list and sends nothing.
 */
@Mixin(targets = "com.simibubi.create.compat.trainmap.TrainMapSync", remap = false)
public abstract class TrainMapSyncMixin implements PatchedMarker {

    @Inject(method = "requestReceived", at = @At("HEAD"), cancellable = true)
    private static void hidetrainmap$ignoreRequest(CallbackInfo ci) {
        ci.cancel();
    }
}
