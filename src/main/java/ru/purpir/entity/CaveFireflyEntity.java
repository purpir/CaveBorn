package ru.purpir.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.ServerWorldAccess;

public class CaveFireflyEntity extends AmbientEntity {
    private double targetX;
    private double targetY;
    private double targetZ;
    private int targetCooldown;

    public CaveFireflyEntity(EntityType<? extends CaveFireflyEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.setGlowing(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.MAX_HEALTH, 2.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.12)
            .add(EntityAttributes.FLYING_SPEED, 0.18)
            .add(EntityAttributes.FOLLOW_RANGE, 8.0);
    }

    public static boolean canSpawn(EntityType<CaveFireflyEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return pos.getY() < 64
            && pos.getY() > world.getBottomY() + 8
            && world.isAir(pos)
            && !world.isSkyVisibleAllowingSea(pos)
            && world.getLightLevel(pos) <= 11;
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (this.getEntityWorld().isClient()) {
            spawnGlowParticles();
            return;
        }

        tickFlight();
    }

    private void tickFlight() {
        if (targetCooldown-- <= 0 || isTargetBad()) {
            pickNewTarget();
        }

        Vec3d target = new Vec3d(targetX - getX(), targetY - getY(), targetZ - getZ());
        double distance = target.length();
        if (distance > 0.1) {
            Vec3d steering = target.normalize().multiply(0.018);
            setVelocity(getVelocity().add(steering).multiply(0.92));
        } else {
            setVelocity(getVelocity().multiply(0.75));
            targetCooldown = 0;
        }

        move(MovementType.SELF, getVelocity());
    }

    private boolean isTargetBad() {
        BlockPos targetPos = BlockPos.ofFloored(targetX, targetY, targetZ);
        return squaredDistanceTo(targetX, targetY, targetZ) > 36.0
            || !getEntityWorld().isAir(targetPos)
            || getEntityWorld().isSkyVisibleAllowingSea(targetPos);
    }

    private void pickNewTarget() {
        Random random = getRandom();
        for (int attempt = 0; attempt < 8; attempt++) {
            double x = getX() + random.nextBetween(-5, 5) + 0.5;
            double y = MathHelper.clamp(getY() + random.nextBetween(-2, 2), getEntityWorld().getBottomY() + 6, 54);
            double z = getZ() + random.nextBetween(-5, 5) + 0.5;
            BlockPos pos = BlockPos.ofFloored(x, y, z);
            if (getEntityWorld().isAir(pos) && !getEntityWorld().isSkyVisibleAllowingSea(pos)) {
                targetX = x;
                targetY = y;
                targetZ = z;
                targetCooldown = 40 + random.nextInt(80);
                return;
            }
        }

        targetX = getX();
        targetY = getY();
        targetZ = getZ();
        targetCooldown = 20;
    }

    private void spawnGlowParticles() {
        World world = getEntityWorld();
        Random random = getRandom();
        double pulse = 0.5 + 0.5 * Math.sin((age + getId()) * 0.18);
        if (random.nextFloat() < 0.75f) {
            world.addParticleClient(
                ParticleTypes.FIREFLY,
                getX() + (random.nextDouble() - 0.5) * 0.16,
                getY() + 0.12 + (random.nextDouble() - 0.5) * 0.12,
                getZ() + (random.nextDouble() - 0.5) * 0.16,
                (random.nextDouble() - 0.5) * 0.01,
                0.005 + pulse * 0.004,
                (random.nextDouble() - 0.5) * 0.01
            );
        }

        if (age % 8 == 0) {
            world.addParticleClient(ParticleTypes.GLOW, getX(), getY() + 0.1, getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return true;
    }

    @Override
    public int getLimitPerChunk() {
        return 10;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }
}
