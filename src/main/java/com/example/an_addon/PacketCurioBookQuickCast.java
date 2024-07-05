package com.hathfury.ars_spell_book_curio;

import com.hollingsworth.arsnouveau.api.spell.ISpellCaster;
import com.hollingsworth.arsnouveau.api.item.ICasterTool;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.entity.LivingEntity;
import java.util.function.Supplier;

public class PacketCurioBookQuickCast {
    int slot;

    public PacketCurioBookQuickCast(int slot){
        this.slot = slot;
    }

    //Decoder
    public PacketCurioBookQuickCast(FriendlyByteBuf buf){
        this.slot = buf.readInt();
    }

    //Encoder
    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(slot);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player == null) return;
            ItemStack stack = CurioBookUtil.getCurioBookStack((LivingEntity)player);
            if(!stack.isEmpty()) {
                CurioBookCaster caster = new CurioBookCaster(stack);
                caster.castCurioBookSpell(player.level(), (LivingEntity)player, null, caster.getSpell(slot));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}