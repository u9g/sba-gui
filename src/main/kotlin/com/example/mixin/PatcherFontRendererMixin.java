package com.example.mixin;

import com.example.fonts.FontRendererHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = club.sk1er.patcher.hooks.FontRendererHook.class, remap = false)
public class PatcherFontRendererMixin {
    @Inject(method = "renderStringAtPos", at = @At("HEAD"), cancellable = true)
    public void onRenderStringAtPos(String text, boolean shadow, CallbackInfoReturnable<Boolean> cir) {
        if (FontRendererHook.INSTANCE.shouldOverridePatcher(text)) cir.setReturnValue(false);
    }

    @Redirect(method = "renderStringAtPos", at = @At(value = "INVOKE", target = "Ljava/lang/String;indexOf(I)I", ordinal = 0))
    public int onRenderChar(String instance, int ch) {
        return "0123456789abcdefklmnorz".indexOf(ch);
    }
}
