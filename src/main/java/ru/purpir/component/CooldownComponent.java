package ru.purpir.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CooldownComponent(long lastUsedTime) {
    
    public static final Codec<CooldownComponent> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.fieldOf("last_used_time").forGetter(CooldownComponent::lastUsedTime)
        ).apply(instance, CooldownComponent::new)
    );
    
    public static final CooldownComponent DEFAULT = new CooldownComponent(0L);
    
    public boolean isOnCooldown(long currentTime, long cooldownTicks) {
        return currentTime - lastUsedTime < cooldownTicks;
    }
    
    public long getRemainingCooldown(long currentTime, long cooldownTicks) {
        long remaining = cooldownTicks - (currentTime - lastUsedTime);
        return Math.max(0, remaining);
    }
}
