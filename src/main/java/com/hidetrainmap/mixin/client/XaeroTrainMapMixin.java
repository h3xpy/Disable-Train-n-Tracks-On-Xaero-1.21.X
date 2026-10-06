package com.hidetrainmap.mixin.client;

import com.hidetrainmap.PatchedMarker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Turns Create's Xaero's World Map integration into a no-op: no data request,
 * no track/train/station rendering, no toggle button, no click handling.
 */
@Mixin(targets = "com.simibubi.create.compat.trainmap.XaeroTrainMap", remap = false)
public abstract class XaeroTrainMapMixin implements PatchedMarker {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void hidetrainmap$cancelTick(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onRender", at = @At("HEAD"), cancellable = true)
    private static void hidetrainmap$cancelRender(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "mouseClick", at = @At("HEAD"), cancellable = true)
    private static void hidetrainmap$cancelMouseClick(CallbackInfo ci) {
        ci.cancel();
    }
}
