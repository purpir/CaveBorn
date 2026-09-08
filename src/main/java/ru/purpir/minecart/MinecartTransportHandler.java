package ru.purpir.minecart;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ru.purpir.network.ModPackets;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MinecartTransportHandler {
    /**
     * Temporary switch for the custom minecart system.
     * Keep the implementation in place, but let vanilla minecart behaviour run
     * until the custom system is enabled again.
     */
    public static final boolean ENABLED = false;

    private static final float MAX_SPEED = 36.0F;
    private static final float ACCELERATION_PER_TICK = 0.9F;
    private static final float BRAKE_PER_TICK = 1.2F;
    private static final double LINK_DISTANCE = 1.5D;
    private static final double MAX_LINK_DISTANCE = 3.0D;
    private static final double SPRING_STRENGTH = 0.22D;
    private static final Map<UUID, PendingSelection> PENDING_SELECTIONS = new HashMap<>();
    private static final Map<UUID, Deque<Vec3d>> PATH_TRAILS = new HashMap<>();
    private static final int MAX_TRAIL_LENGTH = 64;

    private MinecartTransportHandler() {
    }

    public static void register() {
        if (!ENABLED) {
            return;
        }

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.MinecartControlPayload.ID, (payload, context) ->
            context.server().execute(() -> handleControl(context.player(), payload.forward(), payload.back())));
    }

    public static ActionResult handleEntityInteract(AbstractMinecartEntity cart, PlayerEntity player, Hand hand, Vec3d hitPos) {
        if (!ENABLED) {
            return ActionResult.PASS;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(Items.IRON_CHAIN)) {
            return ActionResult.PASS;
        }

        MinecartChainCarrier carrier = cast(cart);
        if (carrier == null) {
            return ActionResult.PASS;
        }

        LinkEnd clickedEnd = detectEnd(cart, player, hitPos);
        ServerWorld world = serverPlayer.getEntityWorld();
        long time = world.getTime();
        PendingSelection pending = PENDING_SELECTIONS.get(player.getUuid());
        if (pending == null || pending.expiresAt < time || !pending.worldKey.equals(world.getRegistryKey())) {
            PENDING_SELECTIONS.put(player.getUuid(), new PendingSelection(cart.getUuid(), clickedEnd, world.getRegistryKey(), time + 120));
            sendMessage(serverPlayer, "message.caveborn.minecart.chain.first");
            return ActionResult.SUCCESS;
        }

        if (pending.cartUuid.equals(cart.getUuid())) {
            PENDING_SELECTIONS.put(player.getUuid(), new PendingSelection(cart.getUuid(), clickedEnd, world.getRegistryKey(), time + 120));
            sendMessage(serverPlayer, "message.caveborn.minecart.chain.first");
            return ActionResult.SUCCESS;
        }

        AbstractMinecartEntity firstCart = getCart(world, pending.cartUuid);
        if (firstCart == null) {
            PENDING_SELECTIONS.put(player.getUuid(), new PendingSelection(cart.getUuid(), clickedEnd, world.getRegistryKey(), time + 120));
            sendMessage(serverPlayer, "message.caveborn.minecart.chain.first");
            return ActionResult.SUCCESS;
        }

        MinecartChainCarrier firstCarrier = cast(firstCart);
        if (firstCarrier == null) {
            return ActionResult.SUCCESS;
        }

        if (pending.end == clickedEnd) {
            PENDING_SELECTIONS.put(player.getUuid(), new PendingSelection(cart.getUuid(), clickedEnd, world.getRegistryKey(), time + 120));
            sendMessage(serverPlayer, "message.caveborn.minecart.chain.first");
            return ActionResult.SUCCESS;
        }

        if (!areFacingEachOther(firstCart, pending.end, cart, clickedEnd)) {
            sendMessage(serverPlayer, "message.caveborn.minecart.chain.not_facing");
            return ActionResult.SUCCESS;
        }

        AbstractMinecartEntity anchorCart;
        MinecartChainCarrier anchorCarrier;
        AbstractMinecartEntity followerCart;
        MinecartChainCarrier followerCarrier;

        if (pending.end == LinkEnd.FRONT && clickedEnd == LinkEnd.BACK) {
            anchorCart = firstCart;
            anchorCarrier = firstCarrier;
            followerCart = cart;
            followerCarrier = carrier;
        } else if (pending.end == LinkEnd.BACK && clickedEnd == LinkEnd.FRONT) {
            anchorCart = cart;
            anchorCarrier = carrier;
            followerCart = firstCart;
            followerCarrier = firstCarrier;
        } else {
            sendMessage(serverPlayer, "message.caveborn.minecart.chain.invalid_pair");
            return ActionResult.SUCCESS;
        }

        if (!canAttach(anchorCarrier, LinkEnd.FRONT) || !canAttach(followerCarrier, LinkEnd.BACK)) {
            sendMessage(serverPlayer, "message.caveborn.minecart.chain.occupied");
            return ActionResult.SUCCESS;
        }

        double distance = distance(anchorCart, followerCart);
        if (distance > MAX_LINK_DISTANCE) {
            sendMessage(serverPlayer, "message.caveborn.minecart.chain.too_far");
            return ActionResult.SUCCESS;
        }

        link(anchorCart, anchorCarrier, followerCart, followerCarrier);
        consumeChain(player, hand);
        PENDING_SELECTIONS.remove(player.getUuid());
        sendMessage(serverPlayer, "message.caveborn.minecart.chain.linked");
        return ActionResult.SUCCESS;
    }

    public static void applyMinecartSpeed(AbstractMinecartEntity cart) {
        if (!ENABLED) {
            return;
        }

        MinecartChainCarrier carrier = cast(cart);
        if (carrier == null) {
            return;
        }

        float speed = carrier.caveborn$getMinecartSpeed();
        Vec3d direction = getForwardVector(cart);

        cart.setVelocity(direction.multiply(speed / 20.0D));
    }

    public static void applyMinecartLinks(AbstractMinecartEntity cart, ServerWorld world) {
        if (!ENABLED) {
            return;
        }

        MinecartChainCarrier carrier = cast(cart);
        if (carrier == null) {
            return;
        }

        if (carrier.caveborn$getMinecartPrevUuid() != null) {
            return;
        }

        float speed = carrier.caveborn$getMinecartSpeed();

        Deque<Vec3d> trail = PATH_TRAILS.computeIfAbsent(cart.getUuid(), k -> new ArrayDeque<>());
        trail.addFirst(position(cart));
        while (trail.size() > MAX_TRAIL_LENGTH) {
            trail.removeLast();
        }

        if (speed <= 0.0F) {
            cart.setVelocity(Vec3d.ZERO);
            return;
        }

        Vec3d direction = getForwardVector(cart);
        Vec3d velocity = cart.getVelocity();
        if (velocity.lengthSquared() > 0.0001D) {
            direction = velocity.normalize();
        }
        cart.setVelocity(direction.multiply(speed / 20.0D));

        int chainIndex = 1;
        UUID nextUuid = carrier.caveborn$getMinecartNextUuid();
        while (nextUuid != null) {
            AbstractMinecartEntity nextCart = getCart(world, nextUuid);
            if (nextCart == null) {
                carrier.caveborn$setMinecartNextUuid(null);
                broadcastChains(world);
                return;
            }
            MinecartChainCarrier nextCarrier = cast(nextCart);
            if (nextCarrier == null) {
                carrier.caveborn$setMinecartNextUuid(null);
                broadcastChains(world);
                return;
            }

            Vec3d target = findTrailPosition(trail, chainIndex);
            if (target != null) {
                nextCart.refreshPositionAndAngles(target.x, target.y, target.z, nextCart.getYaw(), nextCart.getPitch());
            } else {
                Vec3d anchorDir = getForwardVector(cart);
                Vec3d fallback = position(cart).subtract(anchorDir.multiply(LINK_DISTANCE * chainIndex));
                nextCart.refreshPositionAndAngles(fallback.x, fallback.y, fallback.z, nextCart.getYaw(), nextCart.getPitch());
            }

            Vec3d dir = getForwardVector(cart);
            nextCart.setVelocity(dir.multiply(speed / 20.0D));

            nextUuid = nextCarrier.caveborn$getMinecartNextUuid();
            chainIndex++;
        }
    }

    private static Vec3d findTrailPosition(Deque<Vec3d> trail, int chainIndex) {
        double targetDist = LINK_DISTANCE * chainIndex;
        double accumulated = 0.0D;
        Vec3d prev = null;
        for (Vec3d pos : trail) {
            if (prev != null) {
                accumulated += pos.distanceTo(prev);
                if (accumulated >= targetDist) {
                    return pos;
                }
            }
            prev = pos;
        }
        return null;
    }

    private static void handleControl(ServerPlayerEntity player, boolean forward, boolean back) {
        if (!ENABLED) {
            return;
        }

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof AbstractMinecartEntity cart)) {
            return;
        }

        MinecartChainCarrier carrier = cast(cart);
        if (carrier == null || (carrier.caveborn$getMinecartPrevUuid() != null && carrier.caveborn$getMinecartNextUuid() != null)) {
            return;
        }

        float speed = carrier.caveborn$getMinecartSpeed();
        if (forward && !back) {
            speed = Math.min(MAX_SPEED, speed + ACCELERATION_PER_TICK);
        } else if (back && !forward) {
            speed = Math.max(0.0F, speed - BRAKE_PER_TICK);
        }

        carrier.caveborn$setMinecartSpeed(speed);
    }

    private static void link(AbstractMinecartEntity anchorCart, MinecartChainCarrier anchorCarrier, AbstractMinecartEntity followerCart, MinecartChainCarrier followerCarrier) {
        anchorCarrier.caveborn$setMinecartNextUuid(followerCart.getUuid());
        followerCarrier.caveborn$setMinecartPrevUuid(anchorCart.getUuid());
        moveCart(anchorCart, followerCart);
        broadcastChains(anchorCart.getEntityWorld());
    }

    public static void broadcastChains(World world) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        MinecraftServer server = serverWorld.getServer();
        List<Integer> links = new ArrayList<>();
        for (ServerWorld ws : server.getWorlds()) {
            for (Entity entity : ws.iterateEntities()) {
                if (entity instanceof AbstractMinecartEntity cart) {
                    MinecartChainCarrier carrier = cast(cart);
                    if (carrier != null && carrier.caveborn$getMinecartPrevUuid() != null) {
                        Entity prev = ws.getEntity(carrier.caveborn$getMinecartPrevUuid());
                        if (prev != null) {
                            links.add(prev.getId());
                            links.add(cart.getId());
                        }
                    }
                }
            }
        }
        ModPackets.MinecartChainsPayload payload = new ModPackets.MinecartChainsPayload(links);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private static void moveCart(AbstractMinecartEntity anchorCart, AbstractMinecartEntity followerCart) {
        Vec3d anchorPos = position(anchorCart);
        Vec3d direction = getForwardVector(anchorCart);
        Vec3d target = anchorPos.subtract(direction.multiply(LINK_DISTANCE));
        followerCart.refreshPositionAndAngles(target.x, target.y, target.z, followerCart.getYaw(), followerCart.getPitch());
    }

    private static boolean canAttach(MinecartChainCarrier carrier, LinkEnd end) {
        return end == LinkEnd.FRONT ? carrier.caveborn$getMinecartNextUuid() == null : carrier.caveborn$getMinecartPrevUuid() == null;
    }

    private static void consumeChain(PlayerEntity player, Hand hand) {
        if (player instanceof ServerPlayerEntity serverPlayer && !serverPlayer.getAbilities().creativeMode) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(Items.IRON_CHAIN)) {
                stack.decrement(1);
            }
        }
    }

    private static AbstractMinecartEntity getCart(ServerWorld world, UUID uuid) {
        Entity entity = world.getEntity(uuid);
        return entity instanceof AbstractMinecartEntity cart ? cart : null;
    }

    private static MinecartChainCarrier cast(AbstractMinecartEntity cart) {
        return cart instanceof MinecartChainCarrier carrier ? carrier : null;
    }

    private static Vec3d getForwardVector(AbstractMinecartEntity cart) {
        Vec3d velocity = cart.getVelocity();
        if (velocity.horizontalLengthSquared() > 0.0001D) {
            return new Vec3d(velocity.x, 0.0D, velocity.z).normalize();
        }
        Direction direction = cart.getMovementDirection();
        if (!direction.getAxis().isHorizontal()) {
            direction = Direction.NORTH;
        }
        return new Vec3d(direction.getOffsetX(), 0.0D, direction.getOffsetZ());
    }

    private static LinkEnd detectEnd(AbstractMinecartEntity cart, PlayerEntity player, Vec3d hitPos) {
        Vec3d relative = hitPos.subtract(position(cart));
        if (relative.lengthSquared() > 9.0D) {
            relative = position(player).subtract(position(cart));
        }

        Vec3d forward = getForwardVector(cart);
        return relative.dotProduct(forward) < 0.0D ? LinkEnd.FRONT : LinkEnd.BACK;
    }

    private static double distance(AbstractMinecartEntity left, AbstractMinecartEntity right) {
        return position(left).distanceTo(position(right));
    }

    private static Vec3d position(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
    }

    private static boolean areFacingEachOther(AbstractMinecartEntity cartA, LinkEnd endA, AbstractMinecartEntity cartB, LinkEnd endB) {
        Vec3d posA = position(cartA);
        Vec3d posB = position(cartB);
        Vec3d forwardA = getForwardVector(cartA);
        Vec3d forwardB = getForwardVector(cartB);

        Vec3d toB = posB.subtract(posA);
        Vec3d toA = posA.subtract(posB);

        boolean aFacesB = endA == LinkEnd.FRONT ? toB.dotProduct(forwardA) < 0.0D : toB.dotProduct(forwardA) > 0.0D;
        boolean bFacesA = endB == LinkEnd.FRONT ? toA.dotProduct(forwardB) < 0.0D : toA.dotProduct(forwardB) > 0.0D;

        return aFacesB && bFacesA;
    }

    private static void sendMessage(ServerPlayerEntity player, String key) {
        player.sendMessage(Text.translatable(key), true);
    }

    private record PendingSelection(UUID cartUuid, LinkEnd end, RegistryKey<World> worldKey, long expiresAt) {
    }

    private enum LinkEnd {
        FRONT,
        BACK
    }
}
