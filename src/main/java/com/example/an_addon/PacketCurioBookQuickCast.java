package com.example.an_addon;

import com.hollingsworth.arsnouveau.api.spell.ISpellCaster;
import com.hollingsworth.arsnouveau.api.item.ICasterTool;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

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
            System.out.println("PacketCurioBookQuickCast: Handling packet");
            ServerPlayer player = ctx.get().getSender();
            if(player == null) return;
            ItemStack stack = CurioBookUtil.getCurioBookStack((LivingEntity)player);
            if(!stack.isEmpty()) {
                CurioBookCaster caster = new CurioBookCaster(stack);
                System.out.println("PacketCurioBookQuickCast: Casting spell " + caster.getSpell(slot).name + " from slot " + slot);
                ISpellCaster casterTest = ((ICasterTool)stack.getItem()).getSpellCaster(stack);
                System.out.println("PacketCurioBookQuickCast: Casting spell " + casterTest.getSpell(slot).name + " from slot " + slot);
                caster.castCurioBookSpell(player.level(), (LivingEntity)player, null, caster.getSpell(slot));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}