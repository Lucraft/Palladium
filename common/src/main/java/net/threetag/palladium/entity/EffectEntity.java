package net.threetag.palladium.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.threetag.palladium.entity.effect.EntityEffect;
import net.threetag.palladium.registry.PalladiumRegistries;
import net.threetag.palladium.util.property.EntityPropertyHandler;
import net.threetag.palladiumcore.network.ExtendedEntitySpawnData;
import net.threetag.palladiumcore.network.NetworkManager;

import java.util.Objects;

public class EffectEntity extends Entity implements ExtendedEntitySpawnData {

    public int anchorId;
    public Entity anchor;
    public EntityEffect entityEffect;

    public EffectEntity(EntityType<?> entityType, Level world) {
        super(entityType, world);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public EffectEntity(Level worldIn, Entity anchor, EntityEffect entityEffect) {
        this(PalladiumEntityTypes.EFFECT.get(), worldIn);
        this.anchorId = anchor.getId();
        this.entityEffect = entityEffect;
        this.moveTo(anchor.getX(), anchor.getY(), anchor.getZ(), anchor.getYRot(), anchor.getXRot());
    }

    public Entity getAnchorEntity() {
        if (this.anchor == null) {
            this.anchor = this.level().getEntity(this.anchorId);
        }
        return this.anchor;
    }

    @Override
    public void tick() {
        Entity anchor = getAnchorEntity();
        if (anchor != null && this.entityEffect != null) {
            if (!anchor.isAlive() || EntityEffect.IS_DONE_PLAYING.get(this)) {
                this.discard();
            } else {
                this.entityEffect.tick(this, anchor);
                this.moveTo(anchor.getX(), anchor.getY(), anchor.getZ(), anchor.getYRot(), anchor.getXRot());
            }
        } else {
            this.discard();
        }
    }

    @Override
    public void saveAdditionalSpawnData(FriendlyByteBuf buf) {
        buf.writeResourceLocation(Objects.requireNonNull(PalladiumRegistries.ENTITY_EFFECT.getKey(this.entityEffect)));
        buf.writeInt(this.anchorId);
    }

    @Override
    public void loadAdditionalSpawnData(FriendlyByteBuf buf) {
        this.entityEffect = PalladiumRegistries.ENTITY_EFFECT.get(buf.readResourceLocation());
        this.anchorId = buf.readInt();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityEffect = PalladiumRegistries.ENTITY_EFFECT.get(ResourceLocation.parse(compound.getString("EntityEffect")));
        this.anchorId = compound.getInt("AnchorId");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("EntityEffect", Objects.requireNonNull(PalladiumRegistries.ENTITY_EFFECT.getKey(this.entityEffect)).toString());
        compound.putInt("AnchorId", this.anchorId);
    }
}
