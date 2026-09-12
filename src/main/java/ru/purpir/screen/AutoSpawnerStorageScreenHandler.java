package ru.purpir.screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import ru.purpir.block.entity.AutomaticSpawnerBlockEntity;
public class AutoSpawnerStorageScreenHandler extends ScreenHandler {
 private final AutomaticSpawnerBlockEntity spawner;
 public AutoSpawnerStorageScreenHandler(int syncId, PlayerInventory inv, BlockPos pos){super(ModScreenHandlers.AUTO_SPAWNER_STORAGE_SCREEN_HANDLER,syncId);this.spawner=inv.player.getEntityWorld().getBlockEntity(pos) instanceof AutomaticSpawnerBlockEntity s?s:null;if(spawner!=null)for(int r=0;r<6;r++)for(int c=0;c<9;c++)addSlot(new Slot(spawner,1+c+r*9,8+c*18,18+r*18));for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,8+c*18,136+r*18));for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,194));}
 @Override public boolean canUse(PlayerEntity p){return spawner!=null&&spawner.canPlayerUse(p);}
 @Override public ItemStack quickMove(PlayerEntity p,int i){if(i<54){ItemStack s=slots.get(i).getStack().copy();if(!insertItem(slots.get(i).getStack(),54,slots.size(),true))return ItemStack.EMPTY;return s;}if(!insertItem(slots.get(i).getStack(),0,54,false))return ItemStack.EMPTY;return ItemStack.EMPTY;}
}
