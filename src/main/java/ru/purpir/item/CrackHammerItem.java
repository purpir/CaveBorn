package ru.purpir.item;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CrackHammerItem extends Item {
    private static final int MAX_CRACK_LENGTH = 5;
    private static final int CRACK_STAGE = 7;
    private static final int CRACK_LIFETIME_TICKS = 200;
    private static final int USE_COOLDOWN_TICKS = 8;
    private static final Map<RegistryKey<World>, Map<BlockPos, CrackLine>> CRACKS = new HashMap<>();

    public CrackHammerItem(Settings settings) {
        super(settings);
    }

    public static void registerTicker() {
        ServerTickEvents.END_WORLD_TICK.register(CrackHammerItem::tickWorld);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World rawWorld = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (rawWorld.isClient() || !(rawWorld instanceof ServerWorld world) || player == null) {
            return ActionResult.SUCCESS;
        }

        ItemStack stack = context.getStack();
        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.FAIL;
        }

        BlockPos clickedPos = context.getBlockPos();
        Map<BlockPos, CrackLine> worldCracks = CRACKS.computeIfAbsent(world.getRegistryKey(), key -> new HashMap<>());
        CrackLine existingLine = worldCracks.get(clickedPos);
        if (existingLine != null && existingLine.expiresAt >= world.getTime()) {
            collapseLine(world, player, stack, worldCracks, existingLine);
            player.getItemCooldownManager().set(stack, USE_COOLDOWN_TICKS);
            return ActionResult.SUCCESS;
        }

        clearExpired(world, worldCracks);
        Direction direction = getCrackDirection(context, player);
        List<BlockPos> positions = collectCrackLine(world, stack, clickedPos, direction);
        if (positions.isEmpty()) {
            world.playSound(null, clickedPos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS, 0.45F, 0.7F);
            return ActionResult.FAIL;
        }

        CrackLine line = new CrackLine(positions, world.getTime() + CRACK_LIFETIME_TICKS);
        for (BlockPos pos : positions) {
            worldCracks.put(pos, line);
            world.setBlockBreakingInfo(overlayId(pos), pos, CRACK_STAGE);
            spawnCrackParticles(world, pos, world.getBlockState(pos), 6);
        }

        world.playSound(null, clickedPos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.8F, 0.65F);
        world.playSound(null, clickedPos, SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.BLOCKS, 0.45F, 0.55F);
        stack.damage(1, player, player.getPreferredEquipmentSlot(stack));
        player.getItemCooldownManager().set(stack, USE_COOLDOWN_TICKS);
        return ActionResult.SUCCESS;
    }

    private static Direction getCrackDirection(ItemUsageContext context, PlayerEntity player) {
        Direction direction = context.getSide().getOpposite();
        if (direction.getAxis() == Direction.Axis.Y) {
            return player.getHorizontalFacing();
        }
        return direction;
    }

    private static List<BlockPos> collectCrackLine(ServerWorld world, ItemStack stack, BlockPos start, Direction direction) {
        List<BlockPos> positions = new ArrayList<>();
        for (int i = 0; i < MAX_CRACK_LENGTH; i++) {
            BlockPos pos = start.offset(direction, i);
            BlockState state = world.getBlockState(pos);
            if (!canCrack(world, stack, pos, state)) {
                break;
            }
            positions.add(pos.toImmutable());
        }
        return positions;
    }

    private static boolean canCrack(ServerWorld world, ItemStack stack, BlockPos pos, BlockState state) {
        return !state.isAir() && state.getHardness(world, pos) >= 0.0F && stack.isSuitableFor(state);
    }

    private static void collapseLine(ServerWorld world, PlayerEntity player, ItemStack stack,
                                     Map<BlockPos, CrackLine> worldCracks, CrackLine line) {
        int broken = 0;
        for (BlockPos pos : line.positions) {
            clearOverlay(world, pos);
            worldCracks.remove(pos);

            BlockState state = world.getBlockState(pos);
            if (!canCrack(world, stack, pos, state)) {
                continue;
            }

            spawnCrackParticles(world, pos, state, 18);
            if (world.breakBlock(pos, true, player)) {
                stack.damage(1, player, player.getPreferredEquipmentSlot(stack));
                broken++;
            }
        }

        if (broken > 0) {
            BlockPos soundPos = line.positions.getFirst();
            world.playSound(null, soundPos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.9F, 0.7F);
            world.playSound(null, soundPos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.BLOCKS, 0.35F, 1.4F);
        }
    }

    private static void tickWorld(ServerWorld world) {
        Map<BlockPos, CrackLine> worldCracks = CRACKS.get(world.getRegistryKey());
        if (worldCracks == null || world.getTime() % 20 != 0) {
            return;
        }
        clearExpired(world, worldCracks);
    }

    private static void clearExpired(ServerWorld world, Map<BlockPos, CrackLine> worldCracks) {
        long time = world.getTime();
        Iterator<Map.Entry<BlockPos, CrackLine>> iterator = worldCracks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, CrackLine> entry = iterator.next();
            if (entry.getValue().expiresAt < time) {
                clearOverlay(world, entry.getKey());
                iterator.remove();
            }
        }
    }

    private static void clearOverlay(ServerWorld world, BlockPos pos) {
        world.setBlockBreakingInfo(overlayId(pos), pos, -1);
    }

    private static void spawnCrackParticles(ServerWorld world, BlockPos pos, BlockState state, int count) {
        world.spawnParticles(
            new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            count,
            0.32,
            0.32,
            0.32,
            0.08
        );
    }

    private static int overlayId(BlockPos pos) {
        return 0x4C000000 ^ Long.hashCode(pos.asLong());
    }

    private record CrackLine(List<BlockPos> positions, long expiresAt) {
    }
}
