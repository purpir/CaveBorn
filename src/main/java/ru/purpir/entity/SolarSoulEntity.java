package ru.purpir.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ru.purpir.solar.SolarPointBank;

import java.util.List;
import java.util.UUID;

public class SolarSoulEntity extends Entity {
    private static final int MAX_AGE = 240;
    private UUID ownerUuid;

    public SolarSoulEntity(EntityType<? extends SolarSoulEntity> entityType, World world) {
        super(entityType, world);
        setNoGravity(true);
    }

    public SolarSoulEntity(World world, Vec3d pos, UUID ownerUuid) {
        this(ModEntities.SOLAR_SOUL, world);
        this.ownerUuid = ownerUuid;
        refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0F, 0.0F);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);

        if (getEntityWorld().isClient()) {
            spawnClientParticles();
            return;
        }

        if (age > MAX_AGE) {
            discard();
            return;
        }

        PlayerEntity target = findTarget();
        if (target == null) {
            move(MovementType.SELF, getVelocity().multiply(0.92));
            return;
        }

        Vec3d targetPos = new Vec3d(target.getX(), target.getY() + 0.85, target.getZ());
        Vec3d offset = targetPos.subtract(getX(), getY(), getZ());
        if (offset.lengthSquared() < 0.55) {
            if (target instanceof ServerPlayerEntity serverPlayer) {
                SolarPointBank.addPoints(serverPlayer, 5);
            }
            discard();
            return;
        }

        Vec3d pull = offset.normalize().multiply(0.08 + Math.min(0.22, 0.04 * offset.length()));
        setVelocity(getVelocity().multiply(0.72).add(pull));
        move(MovementType.SELF, getVelocity());
    }

    private PlayerEntity findTarget() {
        if (ownerUuid != null) {
            PlayerEntity owner = getEntityWorld().getPlayerByUuid(ownerUuid);
            if (owner != null && owner.isAlive() && squaredDistanceTo(owner) < 48.0 * 48.0) {
                return owner;
            }
        }

        Box box = getBoundingBox().expand(8.0);
        List<? extends PlayerEntity> players = getEntityWorld().getPlayers();
        PlayerEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (PlayerEntity player : players) {
            if (!player.isAlive() || !box.intersects(player.getBoundingBox())) {
                continue;
            }
            double distance = squaredDistanceTo(player);
            if (distance < closestDistance) {
                closest = player;
                closestDistance = distance;
            }
        }
        return closest;
    }

    private void spawnClientParticles() {
        if (age % 2 == 0) {
            getEntityWorld().addParticleClient(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 0.15, getZ(), 0.0, 0.01, 0.0);
        }
        if (age % 5 == 0) {
            getEntityWorld().addParticleClient(ParticleTypes.GLOW, getX(), getY() + 0.15, getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        ownerUuid = view.read("owner", net.minecraft.util.Uuids.CODEC).orElse(null);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        if (ownerUuid != null) {
            view.put("owner", net.minecraft.util.Uuids.CODEC, ownerUuid);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }
}
