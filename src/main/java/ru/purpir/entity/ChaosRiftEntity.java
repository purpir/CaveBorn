package ru.purpir.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ChaosRiftEntity extends Entity {
    private static final int MAX_SPAWNS = 20;
    private static final int SPAWN_INTERVAL_TICKS = 20;
    private int spawned;
    private int spawnCooldown = 10;

    public ChaosRiftEntity(EntityType<? extends ChaosRiftEntity> entityType, World world) {
        super(entityType, world);
        setNoGravity(true);
        noClip = true;
    }

    public ChaosRiftEntity(World world, Vec3d pos) {
        this(ModEntities.CHAOS_RIFT, world);
        refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0F, 0.0F);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        noClip = true;

        if (getEntityWorld().isClient()) {
            spawnClientParticles();
            return;
        }
        if (!(getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        serverWorld.spawnParticles(ParticleTypes.PORTAL, getX(), getY() + 1.3, getZ(), 2, 0.5, 1.0, 0.5, 0.02);
        if (spawnCooldown-- > 0) {
            return;
        }

        if (spawned >= MAX_SPAWNS) {
            collapse(serverWorld);
            return;
        }

        MobEntity mob = serverWorld.random.nextBoolean()
            ? EntityType.ZOMBIE.create(serverWorld, SpawnReason.TRIGGERED)
            : EntityType.SKELETON.create(serverWorld, SpawnReason.TRIGGERED);
        if (mob != null) {
            mob.refreshPositionAndAngles(
                getX() + (serverWorld.random.nextDouble() - 0.5) * 1.5,
                getY(),
                getZ() + (serverWorld.random.nextDouble() - 0.5) * 1.5,
                serverWorld.random.nextFloat() * 360.0F,
                0.0F
            );
            if (mob instanceof SkeletonEntity skeleton) {
                skeleton.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BOW));
            }
            if (serverWorld.spawnEntity(mob)) {
                spawned++;
                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.45, getZ(), 12, 0.55, 0.25, 0.55, 0.03);
                serverWorld.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.65F, 0.65F + serverWorld.random.nextFloat() * 0.2F);
            }
        }

        spawnCooldown = SPAWN_INTERVAL_TICKS;
    }

    private void collapse(ServerWorld world) {
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.2, getZ(), 90, 0.75, 1.1, 0.75, 0.08);
        world.playSound(null, getBlockPos(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 0.9F, 1.35F);
        discard();
    }

    private void spawnClientParticles() {
        if (age % 2 == 0) {
            getEntityWorld().addParticleClient(ParticleTypes.PORTAL, getX(), getY() + 1.2, getZ(), 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        spawned = view.getInt("spawned", 0);
        spawnCooldown = view.getInt("spawn_cooldown", 10);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putInt("spawned", spawned);
        view.putInt("spawn_cooldown", spawnCooldown);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }
}
