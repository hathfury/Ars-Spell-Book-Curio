package com.example.an_addon;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;
import java.util.function.Supplier;

import com.hollingsworth.arsnouveau.common.items.SpellBook;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.*;

public class CurioBookUtil {
    public static ItemStack getCurioBookStack(LivingEntity entity) {
        LazyOptional<ICuriosItemHandler> itemHandler = CuriosApi.getCuriosInventory(entity);
        if(!itemHandler.isPresent()) return ItemStack.EMPTY;

        Optional<ICurioStacksHandler> stacksHandler = itemHandler.orElse(null).getStacksHandler("arsspellbook");
        if(stacksHandler.isEmpty()) return ItemStack.EMPTY;

        IDynamicStackHandler stacks = stacksHandler.orElse(null).getStacks();
        for(int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            if(!stack.isEmpty() && (stack.getItem() instanceof SpellBook)) {
                // Find and return the first spell book in the curio spellbook slots (in case there are more than 1)
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
