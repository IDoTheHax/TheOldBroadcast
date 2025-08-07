package net.idothehax.theoldbroadcast.entity;

import net.idothehax.theoldbroadcast.Theoldbroadcast;
import net.idothehax.theoldbroadcast.sound.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShadowGhostEntity extends Monster {
    private static final EntityDataAccessor<Boolean> VISIBLE = SynchedEntityData.defineId(ShadowGhostEntity.class, EntityDataSerializers.BOOLEAN);

    public ShadowGhostEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
        this.setSilent(true);
        this.xpReward = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VISIBLE, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            Player nearest = level().getNearestPlayer(this, 16);
            if (nearest != null) {
                double dist = this.distanceToSqr(nearest);
                if (dist < 16) {
                    this.entityData.set(VISIBLE, true);
                    if (tickCount % 40 == 0) {
                        level().playSound(null, this.getX(), this.getY(), this.getZ(),
                                Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get(
                                        ModSounds.SHADOW_GHOST_APPEAR.get().getLocation()
                                )),
                            SoundSource.HOSTILE, 0.5f, 0.7f + random.nextFloat() * 0.3f);
                    }
                } else {
                    this.entityData.set(VISIBLE, false);
                }
            }
        }
    }

    @Override
    public boolean isInvisible() {
        return !this.entityData.get(VISIBLE);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        // Ghosts are immune to all damage
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }

    @Override
    public void aiStep() {
        // Prevent gravity and vertical motion
        this.setDeltaMovement(this.getDeltaMovement().x, 0, this.getDeltaMovement().z);
        super.aiStep();
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D);
    }
}
