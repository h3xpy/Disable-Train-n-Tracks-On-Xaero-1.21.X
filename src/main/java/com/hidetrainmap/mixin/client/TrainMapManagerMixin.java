package com.hidetrainmap.mixin.client;

import com.hidetrainmap.PatchedMarker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Safety net shared by every map integration of Create: the overlay toggle can
 * never be hovered nor clicked, so {@code showTrainMapOverlay} is never flipped.
 */
@Mixin(targets = "com.simibubi.create.compat.trainmap.TrainMapManager", remap = false)
public abstract class TrainMapManagerMixin implements PatchedMarker {

    @Inject(method = "handleToggleWidgetClick", at = @At("HEAD"), cancellable = true)
    private static void hidetrainmap$neverToggle(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isToggleWidgetHovered", at = @At("HEAD"), cancellable = true)
    private static void hidetrainmap$neverHovered(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
