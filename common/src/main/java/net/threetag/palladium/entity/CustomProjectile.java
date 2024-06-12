package net.threetag.palladium.entity;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.threetag.palladiumcore.network.ExtendedEntitySpawnData;
import net.threetag.palladiumcore.network.NetworkManager;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class CustomProjectile extends ThrowableProjectile implements ExtendedEntitySpawnData {

    public static final Map<String, Function<CompoundTag, Appearance>> APPEARANCE_REGISTRY = new HashMap<>();
    public static Consumer<CustomProjectile> KUBEJS_EVENT_HANDLER = null;
    public float damage = 3F;
    public float gravity = 0.03F;
    public boolean dieOnBlockHit = true;
    public boolean dieOnEntityHit = true;
    public boolean preventShooterInteraction = false;
    public int lifetime = -1;
    public int setEntityOnFireSeconds = 0;
    public float explosionRadius = 0F;
    public boolean explosionCausesFire = false;
    public Explosion.BlockInteraction explosionBlockInteraction = Explosion.BlockInteraction.KEEP;
    public float knockbackStrength = 0F;
    public String commandOnEntityHit = null;
    public String commandOnBlockHit = null;
    public EntityDimensions dimensions = new EntityDimensions(0.1F, 0.1F, false);
    public List<Appearance> appearances = new ArrayList<>();

    static {
        APPEARANCE_REGISTRY.put("item", ItemAppearance::new);
        APPEARANCE_REGISTRY.put("particles", ParticleAppearance::new);
        APPEARANCE_REGISTRY.put("laser", LaserAppearance::new);
        APPEARANCE_REGISTRY.put("renderLayer", RenderLayerAppearance::new);
        APPEARANCE_REGISTRY.put("trail", TrailAppearance::new);
    }

    public CustomProjectile(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkManager.createAddEntityPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.dimensions;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected float getGravity() {
        return this.gravity;
    }

    @Override
    public boolean ownedBy(Entity entity) {
        return super.ownedBy(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) {
            Entity entity = result.getEntity();

            if (entity != this.getOwner() || !this.preventShooterInteraction) {
                if (this.commandOnEntityHit != null && !this.commandOnEntityHit.isBlank()) {
                    this.level().getServer().getCommands()
                            .performPrefixedCommand(this.createCommandSourceStack()
                                    .withMaximumPermission(2)
                                    .withSuppressedOutput(), this.commandOnEntityHit);
                }

                if (this.damage > 0F) {
                    entity.hurt(entity.level().damageSources().thrown(this, this.getOwner()), this.damage);
                }

                if (this.setEntityOnFireSeconds > 0) {
                    entity.setSecondsOnFire(this.setEntityOnFireSeconds);
                }

                if (this.explosionRadius > 0F) {
                    this.explode(this, this.level().damageSources().thrown(this, this.getOwner()), this.getX(), this.getEyeY(), this.getZ(), this.explosionRadius, this.explosionCausesFire, this.explosionBlockInteraction);
                }

                if (this.knockbackStrength > 0F && entity instanceof LivingEntity living) {
                    living.knockback(this.knockbackStrength, -this.getDeltaMovement().x, -this.getDeltaMovement().z);
                }

                if (this.dieOnEntityHit) {
                    this.level().broadcastEntityEvent(this, (byte) 3);
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);

        if (!this.level().isClientSide) {
            if (this.commandOnBlockHit != null && !this.commandOnBlockHit.isBlank()) {
                this.level().getServer().getCommands()
                        .performPrefixedCommand(this.createCommandSourceStack()
                                .withMaximumPermission(2)
                                .withSuppressedOutput(), this.commandOnBlockHit);
            }

            if (this.explosionRadius > 0F) {
                this.explode(this, this.level().damageSources().thrown(this, this.getOwner()), this.getX(), this.getEyeY(), this.getZ(), this.explosionRadius, this.explosionCausesFire, this.explosionBlockInteraction);
            }

            if (this.dieOnBlockHit) {
                this.level().broadcastEntityEvent(this, (byte) 3);
                this.discard();
            }
        }
    }

    public Explosion explode(Entity source, @Nullable DamageSource damageSource, double x, double y, double z, float radius, boolean fire, Explosion.BlockInteraction blockInteraction) {
        Explosion explosion = new Explosion(source.level(), source, damageSource, null, x, y, z, radius, fire, blockInteraction);
        explosion.explode();
        explosion.finalizeExplosion(true);

        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

        for (Player player : source.level().players()) {
            if (player.distanceToSqr(x, y, z) < 4096.0 && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundExplodePacket(x, y, z, radius, explosion.getToBlow(), explosion.getHitPlayers().get(serverPlayer)));
            }
        }

        return explosion;
    }

    @Override
    public void handleEntityEvent(byte state) {
        if (state == 3) {
            for (Appearance appearance : this.appearances) {
                appearance.spawnParticlesOnHit(this);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        for (Appearance appearance : this.appearances) {
            appearance.onTick(this);
        }

        if (KUBEJS_EVENT_HANDLER != null) {
            KUBEJS_EVENT_HANDLER.accept(this);
        }

        if (this.lifetime > 0 && this.tickCount >= this.lifetime && !this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Damage", this.damage);
        compound.putFloat("Gravity", this.gravity);
        compound.putBoolean("DieOnEntityHit", this.dieOnEntityHit);
        compound.putBoolean("DieOnBlockHit", this.dieOnBlockHit);
        compound.putBoolean("PreventShooterInteraction", this.preventShooterInteraction);
        compound.putFloat("Size", this.dimensions.width);
        compound.putFloat("Lifetime", this.lifetime);
        compound.putFloat("SetEntityOnFireSeconds", this.setEntityOnFireSeconds);
        compound.putFloat("ExplosionRadius", this.explosionRadius);
        compound.putBoolean("ExplosionCausesFire", this.explosionCausesFire);
        compound.putString("ExplosionBlockInteraction", this.explosionBlockInteraction.toString().toLowerCase(Locale.ROOT));
        compound.putFloat("KnockbackStrength", this.knockbackStrength);

        if (this.commandOnEntityHit != null)
            compound.putString("CommandOnEntityHit", this.commandOnEntityHit);
        if (this.commandOnBlockHit != null)
            compound.putString("CommandOnBlockHit", this.commandOnBlockHit);

        ListTag appearanceList = new ListTag();
        for (Appearance appearance : this.appearances) {
            CompoundTag aTag = new CompoundTag();
            aTag.putString("Type", appearance.getId());
            appearance.toNBT(aTag);
            appearanceList.add(aTag);
        }
        compound.put("Appearances", appearanceList);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Damage", Tag.TAG_ANY_NUMERIC))
            this.damage = compound.getFloat("Damage");
        if (compound.contains("Gravity", Tag.TAG_ANY_NUMERIC))
            this.gravity = compound.getFloat("Gravity");
        if (compound.contains("Lifetime", Tag.TAG_ANY_NUMERIC))
            this.lifetime = compound.getInt("Lifetime");
        if (compound.contains("SetEntityOnFireSeconds", Tag.TAG_ANY_NUMERIC))
            this.setEntityOnFireSeconds = compound.getInt("SetEntityOnFireSeconds");
        if (compound.contains("DieOnEntityHit"))
            this.dieOnEntityHit = compound.getBoolean("DieOnEntityHit");
        if (compound.contains("DieOnBlockHit"))
            this.dieOnBlockHit = compound.getBoolean("DieOnBlockHit");
        if (compound.contains("PreventShooterInteraction"))
            this.preventShooterInteraction = compound.getBoolean("PreventShooterInteraction");
        if (compound.contains("Size", Tag.TAG_ANY_NUMERIC))
            this.dimensions = new EntityDimensions(compound.getFloat("Size"), compound.getFloat("Size"), false);
        if (compound.contains("ExplosionRadius", Tag.TAG_ANY_NUMERIC))
            this.explosionRadius = compound.getFloat("ExplosionRadius");
        if (compound.contains("ExplosionCausesFire"))
            this.explosionCausesFire = compound.getBoolean("ExplosionCausesFire");
        if (compound.contains("ExplosionBlockInteraction")) {
            var type = compound.getString("ExplosionBlockInteraction");
            this.explosionBlockInteraction = type.equalsIgnoreCase("break") ? Explosion.BlockInteraction.DESTROY : (type.equalsIgnoreCase("destroy") ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.KEEP);
        }
        if (compound.contains("KnockbackStrength", Tag.TAG_ANY_NUMERIC))
            this.knockbackStrength = compound.getFloat("KnockbackStrength");
        if (compound.contains("CommandOnEntityHit"))
            this.commandOnEntityHit = compound.getString("CommandOnEntityHit");
        if (compound.contains("CommandOnBlockHit"))
            this.commandOnBlockHit = compound.getString("CommandOnBlockHit");

        if (compound.contains("Appearances")) {
            this.appearances = new ArrayList<>();
            ListTag listTag = compound.getList("Appearances", 10);

            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag aTag = listTag.getCompound(i);
                var type = APPEARANCE_REGISTRY.get(aTag.getString("Type"));

                if (type != null) {
                    this.appearances.add(type.apply(aTag));
                }
            }
        }

        this.refreshDimensions();
    }

    @Override
    public void saveAdditionalSpawnData(FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        this.addAdditionalSaveData(tag);
        buf.writeNbt(tag);
        buf.writeFloat(this.getXRot());
        buf.writeFloat(this.getYRot());
    }

    @Override
    public void loadAdditionalSpawnData(FriendlyByteBuf buf) {
        this.readAdditionalSaveData(Objects.requireNonNull(buf.readNbt()));
        this.setXRot(buf.readFloat());
        this.setYRot(buf.readFloat());
    }

    public static abstract class Appearance {

        public Appearance(CompoundTag tag) {
        }

        public abstract String getId();

        public void onTick(CustomProjectile projectile) {

        }

        public void spawnParticlesOnHit(CustomProjectile projectile) {

        }

        public abstract void toNBT(CompoundTag nbt);

    }

    @SuppressWarnings("unchecked")
    public static class ParticleAppearance extends Appearance {

        public final ParticleType type;
        public final int amount;
        public final float spread;
        public final String options;

        public ParticleAppearance(CompoundTag tag) {
            super(tag);
            this.type = tag.contains("ParticleType") ? BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(tag.getString("ParticleType"))) : ParticleTypes.FLAME;
            this.amount = tag.contains("Amount") ? tag.getInt("Amount") : 1;
            this.spread = tag.contains("Spread") ? tag.getFloat("Spread") : 1;
            this.options = tag.contains("Options") ? tag.getString("Options") : "";
        }

        @Override
        public String getId() {
            return "particles";
        }

        @Override
        public void toNBT(CompoundTag nbt) {
            if (this.type != null) {
                nbt.putString("ParticleType", BuiltInRegistries.PARTICLE_TYPE.getKey(this.type).toString());
            }
            nbt.putInt("Amount", this.amount);
            nbt.putFloat("Spread", this.spread);
            nbt.putString("Options", this.options);
        }

        @Override
        public void onTick(CustomProjectile projectile) {
            if (this.type == null) {
                return;
            }

            Random random = new Random();
            for (int i = 0; i < this.amount; i++) {
                float sX = (random.nextFloat() - 0.5F) * this.spread;
                float sY = (random.nextFloat() - 0.5F) * this.spread;
                float sZ = (random.nextFloat() - 0.5F) * this.spread;

                try {
                    projectile.level().addParticle(this.type.getDeserializer().fromCommand(this.type, new StringReader(this.options)), projectile.getX(), projectile.getY(), projectile.getZ(), sX, sY, sZ);
                } catch (CommandSyntaxException ignored) {
                }
            }
        }

        @Override
        public void spawnParticlesOnHit(CustomProjectile projectile) {
            if (this.type == null) {
                return;
            }

            for (int i = 0; i < this.amount; i++) {
                Random random = new Random();
                float sX = (random.nextFloat() - 0.5F) * this.spread * 2F;
                float sY = (random.nextFloat() - 0.5F) * this.spread * 2F;
                float sZ = (random.nextFloat() - 0.5F) * this.spread * 2F;

                try {
                    projectile.level().addParticle(this.type.getDeserializer().fromCommand(this.type, new StringReader(this.options)), projectile.getX(), projectile.getY(), projectile.getZ(), sX, sY, sZ);
                } catch (CommandSyntaxException ignored) {
                }
            }
        }
    }

    public static class ItemAppearance extends Appearance {

        public final ItemStack item;

        public ItemAppearance(CompoundTag tag) {
            super(tag);
            var itemTag = tag.get("Item");

            if (itemTag instanceof CompoundTag compoundTag) {
                this.item = ItemStack.of(compoundTag);
            } else if (itemTag instanceof StringTag stringTag) {
                this.item = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(stringTag.getAsString())));
            } else {
                this.item = ItemStack.EMPTY;
            }
        }

        @Override
        public String getId() {
            return "item";
        }

        @Override
        public void toNBT(CompoundTag nbt) {
            nbt.put("Item", this.item.save(new CompoundTag()));
        }

        @Override
        public void spawnParticlesOnHit(CustomProjectile projectile) {
            var data = new ItemParticleOption(ParticleTypes.ITEM, this.item);

            for (int i = 0; i < 8; ++i) {
                projectile.level().addParticle(data, projectile.getX(), projectile.getY(), projectile.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public static class LaserAppearance extends Appearance {

        public final float thickness;
        public final Color color;

        public LaserAppearance(CompoundTag tag) {
            super(tag);
            this.thickness = tag.contains("Thickness") ? tag.getFloat("Thickness") : 0.05F;
            var colorTag = tag.get("Color");

            if (colorTag instanceof StringTag stringTag) {
                this.color = Color.decode(stringTag.getAsString());
            } else if (colorTag instanceof CompoundTag compoundTag) {
                this.color = new Color(compoundTag.getInt("Red"), compoundTag.getInt("Green"), compoundTag.getInt("Blue"));
            } else {
                this.color = Color.RED;
            }
        }

        @Override
        public String getId() {
            return "laser";
        }

        @Override
        public void toNBT(CompoundTag nbt) {
            nbt.putFloat("Thickness", this.thickness);
            CompoundTag colorTag = new CompoundTag();
            colorTag.putInt("Red", this.color.getRed());
            colorTag.putInt("Green", this.color.getGreen());
            colorTag.putInt("Blue", this.color.getBlue());
            nbt.put("Color", colorTag);
        }
    }

    public static class RenderLayerAppearance extends Appearance {

        public final List<ResourceLocation> renderLayers;

        public RenderLayerAppearance(CompoundTag tag) {
            super(tag);
            this.renderLayers = new ArrayList<>();

            var layerTag = tag.get("RenderLayer");

            if (layerTag instanceof StringTag stringTag) {
                this.renderLayers.add(new ResourceLocation(stringTag.getAsString()));
            } else if (layerTag instanceof ListTag list) {
                for (Tag t : list) {
                    if (t instanceof StringTag stringTag) {
                        this.renderLayers.add(new ResourceLocation(stringTag.getAsString()));
                    }
                }
            }
        }

        @Override
        public String getId() {
            return "renderLayer";
        }

        @Override
        public void toNBT(CompoundTag nbt) {
            if (this.renderLayers.size() == 1) {
                nbt.putString("RenderLayer", this.renderLayers.get(0).toString());
            } else {
                ListTag listTag = new ListTag();
                for (ResourceLocation layer : this.renderLayers) {
                    listTag.add(StringTag.valueOf(layer.toString()));
                }
                nbt.put("RenderLayer", listTag);
            }
        }
    }

    public static class TrailAppearance extends Appearance {

        public final List<ResourceLocation> trails;

        public TrailAppearance(CompoundTag tag) {
            super(tag);
            this.trails = new ArrayList<>();

            var trailTag = tag.get("Trail");

            if (trailTag instanceof StringTag stringTag) {
                this.trails.add(new ResourceLocation(stringTag.getAsString()));
            } else if (trailTag instanceof ListTag list) {
                for (Tag t : list) {
                    if (t instanceof StringTag stringTag) {
                        this.trails.add(new ResourceLocation(stringTag.getAsString()));
                    }
                }
            }
        }

        @Override
        public String getId() {
            return "trail";
        }

        @Override
        public void toNBT(CompoundTag nbt) {
            if (this.trails.size() == 1) {
                nbt.putString("Trail", this.trails.get(0).toString());
            } else {
                ListTag listTag = new ListTag();
                for (ResourceLocation layer : this.trails) {
                    listTag.add(StringTag.valueOf(layer.toString()));
                }
                nbt.put("Trail", listTag);
            }
        }
    }

}
