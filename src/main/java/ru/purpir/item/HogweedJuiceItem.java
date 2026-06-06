package ru.purpir.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class HogweedJuiceItem extends Item {
    private static final int DRINK_TIME = 32;

    public HogweedJuiceItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK.value(), SoundCategory.PLAYERS, 0.55F, 0.95F);
        }
        user.setCurrentHand(hand);
        return ActionResult.SUCCESS;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient()) {
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK.value(), SoundCategory.PLAYERS, 0.75F, 1.0F);
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 30 * 20, 2));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 20 * 20, 1));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 5 * 20, 0));
        }

        if (user instanceof PlayerEntity player && player.isCreative()) {
            return stack;
        }

        stack.decrement(1);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) {
            return bottle;
        }

        if (user instanceof PlayerEntity player) {
            if (!player.getInventory().insertStack(bottle)) {
                player.dropItem(bottle, false);
            }
        }

        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return DRINK_TIME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }
}
