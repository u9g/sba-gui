package com.example.mixin;

import com.example.utils.Utils;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class PlayerControllerMPMixin {
    @Inject(method = "windowClick", at = @At("HEAD"), cancellable = true)
    public void onClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> cir) {
        if (Utils.INSTANCE.getBlockNextClick()) cir.setReturnValue(null);
    }
}
