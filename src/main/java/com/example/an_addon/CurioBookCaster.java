package com.hathfury.ars_spell_book_curio;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.ANFakePlayer;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.IWrappedCaster;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.api.util.SpellUtil;
import com.hollingsworth.arsnouveau.common.block.tile.ScribesTile;
import com.hollingsworth.arsnouveau.common.datagen.BlockTagProvider;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSensitive;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

public class CurioBookCaster extends SpellCaster {
    public CurioBookCaster(ItemStack stack) {
        super(stack);
    }

    public CurioBookCaster(CompoundTag tag) {
        super(tag);
    }

    @Override
    public int getMaxSlots() {
        return 10;
    }

    public InteractionResultHolder<ItemStack> castCurioBookSpell(Level worldIn, LivingEntity entity, @Nullable Component invalidMessage, @NotNull Spell spell) {
        ItemStack stack = CurioBookUtil.getCurioBookStack(entity);

        if (worldIn.isClientSide)
            return InteractionResultHolder.pass(stack);
        spell = modifySpellBeforeCasting(worldIn, entity, InteractionHand.MAIN_HAND, spell);
        if (!spell.isValid() && invalidMessage != null) {
            PortUtil.sendMessageNoSpam(entity, invalidMessage);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        Player player = entity instanceof Player thisPlayer ? thisPlayer : ANFakePlayer.getPlayer((ServerLevel) worldIn);
        IWrappedCaster wrappedCaster = entity instanceof Player pCaster ? new PlayerCaster(pCaster) : new LivingCaster(entity);
        SpellResolver resolver = getSpellResolver(new SpellContext(worldIn, spell, entity, wrappedCaster, stack), worldIn, player, InteractionHand.MAIN_HAND);
        boolean isSensitive = resolver.spell.getBuffsAtIndex(0, entity, AugmentSensitive.INSTANCE) > 0;
        HitResult result = SpellUtil.rayTrace(entity, 0.5 + player.getBlockReach(), 0, isSensitive);
        if (result instanceof BlockHitResult blockHit) {
            BlockEntity tile = worldIn.getBlockEntity(blockHit.getBlockPos());
            if (tile instanceof ScribesTile)
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);

            if (!entity.isShiftKeyDown() && tile != null && !(worldIn.getBlockState(blockHit.getBlockPos()).is(BlockTagProvider.IGNORE_TILE))) {
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }
        }

        if (result instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity) {
            if (resolver.onCastOnEntity(stack, entityHitResult.getEntity(), InteractionHand.MAIN_HAND))
                playSound(entity.getOnPos(), worldIn, entity, getCurrentSound(), SoundSource.PLAYERS);
            return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
        }

        if (result instanceof BlockHitResult blockHitResult && (result.getType() == HitResult.Type.BLOCK || isSensitive)) {
            if (entity instanceof Player) {
                UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, (BlockHitResult) result);
                if (resolver.onCastOnBlock(context))
                    playSound(entity.getOnPos(), worldIn, entity, getCurrentSound(), SoundSource.PLAYERS);
            } else if (resolver.onCastOnBlock(blockHitResult)) {
                playSound(entity.getOnPos(), worldIn, entity, getCurrentSound(), SoundSource.NEUTRAL);
            }
            return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
        }

        if (resolver.onCast(stack, worldIn))
            playSound(entity.getOnPos(), worldIn, entity, getCurrentSound(), SoundSource.PLAYERS);
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }
}
