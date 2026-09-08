package ru.purpir.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.minecart.MinecartChainCarrier;
import ru.purpir.minecart.MinecartTransportHandler;

import java.util.UUID;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin implements MinecartChainCarrier {
    @Unique
    private static final TrackedData<Float> CAVEBORN$SPEED = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.FLOAT);
    @Unique
    private float caveborn$speed;
    @Unique
    private UUID caveborn$prevUuid;
    @Unique
    private UUID caveborn$nextUuid;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void caveborn$initMinecartDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(CAVEBORN$SPEED, 0.0F);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void caveborn$applyMinecartSpeed(CallbackInfo ci) {
        if (!MinecartTransportHandler.ENABLED) {
            return;
        }

        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        if (self.getEntityWorld().isClient()) {
            return;
        }

        MinecartTransportHandler.applyMinecartSpeed(self);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void caveborn$applyMinecartLinks(CallbackInfo ci) {
        if (!MinecartTransportHandler.ENABLED) {
            return;
        }

        AbstractMinecartEntity self = (AbstractMinecartEntity) (Object) this;
        if (self.getEntityWorld().isClient()) {
            return;
        }

        MinecartTransportHandler.applyMinecartLinks(self, (ServerWorld) self.getEntityWorld());
    }

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void caveborn$raiseMinecartMaxSpeed(ServerWorld world, CallbackInfoReturnable<Double> cir) {
        if (!MinecartTransportHandler.ENABLED) {
            return;
        }

        cir.setReturnValue(Math.max(cir.getReturnValue(), 36.0D / 20.0D));
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void caveborn$readMinecartData(ReadView view, CallbackInfo ci) {
        caveborn$speed = view.getFloat("caveborn_minecart_speed", 0.0F);
        caveborn$prevUuid = view.read("caveborn_minecart_prev", net.minecraft.util.Uuids.CODEC).orElse(null);
        caveborn$nextUuid = view.read("caveborn_minecart_next", net.minecraft.util.Uuids.CODEC).orElse(null);
        ((AbstractMinecartEntity) (Object) this).getDataTracker().set(CAVEBORN$SPEED, caveborn$speed);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void caveborn$writeMinecartData(WriteView view, CallbackInfo ci) {
        view.putFloat("caveborn_minecart_speed", caveborn$speed);
        if (caveborn$prevUuid != null) {
            view.put("caveborn_minecart_prev", net.minecraft.util.Uuids.CODEC, caveborn$prevUuid);
        }
        if (caveborn$nextUuid != null) {
            view.put("caveborn_minecart_next", net.minecraft.util.Uuids.CODEC, caveborn$nextUuid);
        }
    }

    @Override
    public float caveborn$getMinecartSpeed() {
        return ((AbstractMinecartEntity) (Object) this).getDataTracker().get(CAVEBORN$SPEED);
    }

    @Override
    public void caveborn$setMinecartSpeed(float speed) {
        caveborn$speed = Math.max(0.0F, Math.min(36.0F, speed));
        ((AbstractMinecartEntity) (Object) this).getDataTracker().set(CAVEBORN$SPEED, caveborn$speed);
    }

    @Override
    public UUID caveborn$getMinecartPrevUuid() {
        return caveborn$prevUuid;
    }

    @Override
    public void caveborn$setMinecartPrevUuid(UUID uuid) {
        caveborn$prevUuid = uuid;
    }

    @Override
    public UUID caveborn$getMinecartNextUuid() {
        return caveborn$nextUuid;
    }

    @Override
    public void caveborn$setMinecartNextUuid(UUID uuid) {
        caveborn$nextUuid = uuid;
    }
}
