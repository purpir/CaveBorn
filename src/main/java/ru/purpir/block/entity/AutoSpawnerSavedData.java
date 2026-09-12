package ru.purpir.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AutoSpawnerSavedData extends PersistentState {
    public static final int INVENTORY_SIZE = 54;
    private static final String NAME = "caveborn_automatic_spawners";
    private final Map<BlockPos, SpawnerData> spawners;

    public static final Codec<SpawnerData> SPAWNER_CODEC = RecordCodecBuilder.create(i -> i.group(
        ItemStack.OPTIONAL_CODEC.fieldOf("egg").forGetter(s -> s.egg),
        ItemStack.OPTIONAL_CODEC.listOf().fieldOf("loot").forGetter(s -> java.util.Arrays.asList(s.loot)),
        Codec.LONG.fieldOf("experience").forGetter(s -> s.experience)
    ).apply(i, (egg, loot, xp) -> new SpawnerData(egg, loot.toArray(ItemStack[]::new), xp)));
    public static final Codec<AutoSpawnerSavedData> CODEC = RecordCodecBuilder.create(i -> i.group(
        SpawnerRecord.CODEC.listOf().fieldOf("spawners").forGetter(d -> d.spawners.entrySet().stream().map(e -> new SpawnerRecord(e.getKey(), e.getValue())).toList())
    ).apply(i, records -> { Map<BlockPos, SpawnerData> map = new HashMap<>(); records.forEach(r -> map.put(r.pos(), r.data())); return new AutoSpawnerSavedData(map); }));
    public static final PersistentStateType<AutoSpawnerSavedData> TYPE = new PersistentStateType<>(NAME,
        context -> new AutoSpawnerSavedData(), context -> CODEC, null);
    private record SpawnerRecord(BlockPos pos, SpawnerData data) {
        private static final Codec<SpawnerRecord> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(SpawnerRecord::pos), SPAWNER_CODEC.fieldOf("data").forGetter(SpawnerRecord::data)
        ).apply(i, SpawnerRecord::new));
    }

    public AutoSpawnerSavedData() { this.spawners = new HashMap<>(); }
    public AutoSpawnerSavedData(Map<BlockPos, SpawnerData> spawners) { this.spawners = new HashMap<>(spawners); }
    public static AutoSpawnerSavedData get(ServerWorld world) { return world.getPersistentStateManager().getOrCreate(TYPE); }

    public SpawnerData getOrCreate(BlockPos pos) {
        SpawnerData data = spawners.computeIfAbsent(pos.toImmutable(), p -> new SpawnerData());
        return data;
    }
    public void remove(BlockPos pos) { if (spawners.remove(pos) != null) markDirty(); }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(AutoSpawnerSavedData::tickWorld);
    }
    private static void tickWorld(ServerWorld world) {
        AutoSpawnerSavedData data = get(world);
        for (Map.Entry<BlockPos, SpawnerData> entry : data.spawners.entrySet()) {
            BlockPos pos = entry.getKey();
            if (entry.getValue().tick(world, pos)) {
                data.markDirty();
            }
        }
    }

    public static final class SpawnerData {
        private ItemStack egg;
        private final ItemStack[] loot;
        private long experience;
        private int cooldown;
        private static final int CYCLE_TICKS = 100;

        public SpawnerData() { this(ItemStack.EMPTY, new ItemStack[INVENTORY_SIZE], 0); }
        public SpawnerData(ItemStack egg, ItemStack[] loot, long experience) {
            this.egg = egg == null ? ItemStack.EMPTY : egg;
            this.loot = new ItemStack[INVENTORY_SIZE];
            for (int i=0; i<INVENTORY_SIZE; i++) this.loot[i] = i < loot.length && loot[i] != null ? loot[i] : ItemStack.EMPTY;
            this.experience = Math.max(0, experience);
        }
        public boolean isEmpty() { for (ItemStack stack : loot) if (!stack.isEmpty()) return false; return true; }
        public ItemStack getStack(int slot) { return slot == 0 ? egg : loot[slot - 1]; }
        public void setStack(int slot, ItemStack stack) {
            if (slot == 0) { egg = stack.copy(); egg.setCount(Math.min(1, egg.getCount())); }
            else { loot[slot - 1] = stack.copy(); loot[slot - 1].capCount(loot[slot - 1].getMaxCount()); }
        }
        public ItemStack removeStack(int slot, int amount) {
            if (slot == 0) { ItemStack result=egg.copy(); result.setCount(Math.min(amount, 1)); egg=ItemStack.EMPTY; return result; }
            ItemStack old=loot[slot - 1]; if (old.isEmpty()) return ItemStack.EMPTY; int n=Math.min(amount, old.getCount()); ItemStack result=old.copy(); result.setCount(n); old.decrement(n); return result;
        }
        public ItemStack removeStack(int slot) { ItemStack result=getStack(slot).copy(); if(slot==0)egg=ItemStack.EMPTY; else loot[slot - 1]=ItemStack.EMPTY; return result; }
        public void clear() { egg=ItemStack.EMPTY; for(int i=0;i<INVENTORY_SIZE;i++) loot[i]=ItemStack.EMPTY; }
        public boolean isValidEgg(ItemStack stack) { return stack.getItem() instanceof SpawnEggItem; }
        public long experience() { return experience; }
        public void collectExperience(ServerWorld world, PlayerEntity player) {
            long amount=experience; experience=0;
            while (amount > 0) { int value=(int)Math.min(amount, 2477); ExperienceOrbEntity.spawn(world, player.getEntityPos(), value); amount-=value; }
        }
        private boolean tick(ServerWorld world, BlockPos pos) {
            if (egg.isEmpty() || !(egg.getItem() instanceof SpawnEggItem) || ++cooldown < CYCLE_TICKS) return false;
            cooldown=0;
            EntityType<?> type=((SpawnEggItem) egg.getItem()).getEntityType(egg);
            if (type == null) return false;
            boolean processed = false;
            int mobCount = 1 + world.random.nextInt(3);
            for (int i = 0; i < mobCount; i++) {
                if (processMob(world, pos, type)) {
                    processed = true;
                    world.spawnParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        16, 0.35, 0.45, 0.35, 0.08);
                }
            }
            return processed;
        }
        private boolean processMob(ServerWorld world, BlockPos pos, EntityType<?> type) {
            var entity=type.create(world, SpawnReason.SPAWN_ITEM_USE);
            if (!(entity instanceof MobEntity mob)) return false;
            mob.refreshPositionAndAngles(pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, world.random.nextFloat()*360F, 0);
            mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.SPAWN_ITEM_USE, null);
            int mobExperience = Math.max(0, mob.getExperienceToDrop(world, null));
            if (mob instanceof net.minecraft.entity.mob.SlimeEntity slime) {
                slime.setSize(net.minecraft.entity.mob.SlimeEntity.MIN_SIZE, true);
            }
            ServerPlayerEntity killer = world.getPlayers().stream().findFirst().orElse(null);
            DamageSource source = killer != null
                ? world.getDamageSources().playerAttack(killer)
                : world.getDamageSources().generic();
            if (killer != null) {
                // LivingEntity.generateLoot uses the mob's attacking-player state
                // for the killed_by_player loot condition; the DamageSource alone
                // only fills the attacker context and is not enough here.
                mob.setAttacking(killer, 100);
            }
            if (world.getGameRules().getBoolean(net.minecraft.world.GameRules.DO_MOB_LOOT)) {
                boolean magmaCube = mob instanceof net.minecraft.entity.mob.MagmaCubeEntity;
                boolean[] magmaCreamDropped = {false};
                if (magmaCube) {
                    // Vanilla's magma-cube loot table requires size >= 2 for magma cream.
                    slimeSizeForLoot(mob, 2);
                }
                type.getLootTableKey().ifPresent(key ->
                    mob.generateLoot(world, source, true, key, stack -> {
                        if (stack.isOf(Items.MAGMA_CREAM)) magmaCreamDropped[0] = true;
                        insert(stack.copy());
                    })
                );
                if (magmaCube) {
                    slimeSizeForLoot(mob, net.minecraft.entity.mob.SlimeEntity.MIN_SIZE);
                    if (!magmaCreamDropped[0]) insert(new ItemStack(Items.MAGMA_CREAM));
                }
            }
            experience += mobExperience;
            mob.discard();
            return true;
        }
        private void slimeSizeForLoot(MobEntity mob, int size) {
            if (mob instanceof net.minecraft.entity.mob.SlimeEntity slime) slime.setSize(size, true);
        }
        private void insert(ItemStack incoming) {
            for(int i=0;i<INVENTORY_SIZE && !incoming.isEmpty();i++) { ItemStack s=loot[i]; if(!s.isEmpty() && ItemStack.areItemsAndComponentsEqual(s,incoming)){int n=Math.min(incoming.getCount(),s.getMaxCount()-s.getCount());s.increment(n);incoming.decrement(n);} }
            for(int i=0;i<INVENTORY_SIZE && !incoming.isEmpty();i++) if(loot[i].isEmpty()){loot[i]=incoming.copy();incoming.setCount(0);}
        }
    }
}
