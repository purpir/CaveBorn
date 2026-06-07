package ru.purpir.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import ru.purpir.entity.ChaosRiftEntity;

public class ChaosCoreBlockEntity extends BlockEntity {
    private static final int RIFT_INTERVAL_TICKS = 2400;
    private static final int RIFT_AREA_HALF_SIZE = 10;
    private long nextRiftTime;

    public ChaosCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHAOS_CORE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ChaosCoreBlockEntity blockEntity) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        long time = serverWorld.getTime();
        if (blockEntity.nextRiftTime <= 0L) {
            blockEntity.nextRiftTime = time + RIFT_INTERVAL_TICKS;
            blockEntity.markDirty();
            return;
        }

        if (time < blockEntity.nextRiftTime) {
            return;
        }

        int count = 1 + serverWorld.random.nextInt(2);
        for (int i = 0; i < count; i++) {
            spawnRift(serverWorld, pos);
        }

        blockEntity.nextRiftTime = time + RIFT_INTERVAL_TICKS;
        blockEntity.markDirty();
    }

    private static void spawnRift(ServerWorld world, BlockPos corePos) {
        for (int attempt = 0; attempt < 32; attempt++) {
            int dx = world.random.nextBetween(-RIFT_AREA_HALF_SIZE, RIFT_AREA_HALF_SIZE);
            int dz = world.random.nextBetween(-RIFT_AREA_HALF_SIZE, RIFT_AREA_HALF_SIZE);
            if (Math.abs(dx) < 3 && Math.abs(dz) < 3) {
                continue;
            }

            int x = corePos.getX() + dx;
            int z = corePos.getZ() + dz;
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos riftPos = new BlockPos(x, y, z);
            if (!world.isChunkLoaded(riftPos)) {
                continue;
            }
            if (!world.getBlockState(riftPos).isAir() || !world.getBlockState(riftPos.down()).isSolidBlock(world, riftPos.down())) {
                continue;
            }
            if (world.getBlockState(riftPos.down()).isOf(Blocks.BEDROCK)) {
                continue;
            }

            ChaosRiftEntity rift = new ChaosRiftEntity(world, new net.minecraft.util.math.Vec3d(riftPos.getX() + 0.5, riftPos.getY(), riftPos.getZ() + 0.5));
            world.spawnEntity(rift);
            world.spawnParticles(ParticleTypes.PORTAL, riftPos.getX() + 0.5, riftPos.getY() + 0.8, riftPos.getZ() + 0.5, 50, 0.45, 0.7, 0.45, 0.08);
            world.spawnParticles(ParticleTypes.REVERSE_PORTAL, riftPos.getX() + 0.5, riftPos.getY() + 0.5, riftPos.getZ() + 0.5, 28, 0.35, 0.45, 0.35, 0.05);
            world.playSound(null, riftPos, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 1.0F, 0.55F);
            return;
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        nextRiftTime = view.getLong("next_rift_time", 0L);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putLong("next_rift_time", nextRiftTime);
    }
}
