package com.hathfury.ars_spell_book_curio.mixin;

import com.hathfury.ars_spell_book_curio.CurioBookUtil;
import com.hathfury.ars_spell_book_curio.PacketCurioBookQuickCast;
import com.hathfury.ars_spell_book_curio.Networking;
import com.hollingsworth.arsnouveau.client.registry.ModKeyBindings;
import com.hollingsworth.arsnouveau.client.keybindings.KeyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyHandler.class)
public abstract class KeyHandlerMixin {

    @Inject(method = "checkCasterKeys", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onCheckCasterKeys(int key, CallbackInfo ci){
        if(key == -1) return;
        int spellSlot = ModKeyBindings.usedQuickSlot(key);
        if(spellSlot == -1) return;

        Player player = Minecraft.getInstance().player;
        ItemStack stack = CurioBookUtil.getCurioBookStack((LivingEntity)player);
        if(!stack.isEmpty()) {
            Networking.INSTANCE.sendToServer(new PacketCurioBookQuickCast(spellSlot));
            ci.cancel(); // Handled here, so don't continue original method
            return;
        }
        // Fall through to original method
    }

}