package ru.purpir.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import ru.purpir.ability.*;

import java.util.ArrayList;
import java.util.List;

public class SolarStrikeHandler {
    
    private static final List<SwordAbility> ABILITIES = new ArrayList<>();
    
    static {
        ABILITIES.add(new WoodenSwordAbility());
        ABILITIES.add(new StoneSwordAbility());
        ABILITIES.add(new CopperSwordAbility());
        ABILITIES.add(new GoldenSwordAbility());
        ABILITIES.add(new IronSwordAbility());
        ABILITIES.add(new DiamondSwordAbility());
        ABILITIES.add(new NetheriteSwordAbility());
        ABILITIES.add(new BronzeSwordAbility());
        ABILITIES.add(new MaceAbility());
        ABILITIES.add(new SolarStrikeAbility());
    }
    
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            
            for (SwordAbility ability : ABILITIES) {
                if (ability.canUse(stack)) {
                    boolean success = ability.tryUse(player, world, stack);
                    
                    // Если способность успешно использована, наносим урон прочности
                    if (success && !world.isClient()) {
                        stack.damage(10, player, player.getPreferredEquipmentSlot(stack));
                    }
                    
                    return ActionResult.SUCCESS;
                }
            }
            
            return ActionResult.PASS;
        });
    }
}
