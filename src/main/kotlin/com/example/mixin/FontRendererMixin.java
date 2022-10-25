package com.example.mixin;

import com.example.fonts.FontRendererHook;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontRenderer.class)
public abstract class FontRendererMixin {
    @Shadow protected abstract void resetStyles();

    @Inject(method = "renderChar", at = @At("HEAD"))
    public void onRenderChar(char ch, boolean italic, CallbackInfoReturnable<Float> cir) {
        FontRendererHook.INSTANCE.changeTextColor();
    }

    @Redirect(method = "renderStringAtPos", at = @At(value = "INVOKE", target = "Ljava/lang/String;indexOf(I)I", ordinal = 0))
    public int renderStringColorsChance(String instance, int ch) {
        if (ch == 'z') {
            this.resetStyles();
            FontRendererHook.INSTANCE.toggleChromaOn();
        }
        return "0123456789abcdefklmnor".indexOf(ch);
    }

    @Inject(method = "renderStringAtPos", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/FontRenderer;italicStyle:Z", shift = At.Shift.AFTER, ordinal = 0))
    public void first_setItalicStyle(String text, boolean shadow, CallbackInfo ci) {
        FontRendererHook.INSTANCE.restoreChromaState();
    }

    @Inject(method = "renderStringAtPos", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/FontRenderer;italicStyle:Z", shift = At.Shift.AFTER, ordinal = 2))
    public void third_setItalicStyle(String text, boolean shadow, CallbackInfo ci) {
        FontRendererHook.INSTANCE.restoreChromaState();
    }

    @Inject(method = "renderStringAtPos", at = @At("HEAD"))
    public void onStartRenderStringAtPos(String text, boolean shadow, CallbackInfo ci) {
        FontRendererHook.INSTANCE.beginRenderString(shadow);
    }

    @Inject(method = "renderStringAtPos", at = @At("HEAD"))
    public void onEndRenderStringAtPos(String text, boolean shadow, CallbackInfo ci) {
        FontRendererHook.INSTANCE.endRenderString();
    }
}
