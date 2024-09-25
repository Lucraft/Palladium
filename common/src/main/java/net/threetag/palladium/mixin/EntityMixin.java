package net.threetag.palladium.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.threetag.palladium.entity.PalladiumEntityExtension;
import net.threetag.palladium.entity.TrailHandler;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.ability.IntangibilityAbility;
import net.threetag.palladium.util.property.EntityPropertyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"ConstantValue", "rawtypes", "DataFlowIssue"})
@Mixin(Entity.class)
public class EntityMixin implements PalladiumEntityExtension {

    @Unique
    private EntityPropertyHandler palladium$propertyHandler;

    @Unique
    private TrailHandler palladium$trailHandler;

    @Shadow
    public Level level;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(EntityType entityType, Level level, CallbackInfo ci) {
        this.palladium$propertyHandler = new EntityPropertyHandler((Entity) (Object) this);
        this.palladium$trailHandler = new TrailHandler((Entity) (Object) this);
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", shift = At.Shift.AFTER))
    public void load(CompoundTag compound, CallbackInfo ci) {
        CompoundTag palladiumTag = compound.contains("Palladium") ? compound.getCompound("Palladium") : new CompoundTag();
        if (palladiumTag.contains("Properties", Tag.TAG_COMPOUND)) {
            this.palladium$propertyHandler.fromNBT(palladiumTag.getCompound("Properties"));
        }
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", shift = At.Shift.AFTER))
    public void saveWithoutId(CompoundTag compound, CallbackInfoReturnable<CompoundTag> ci) {
        CompoundTag palladiumTag = compound.contains("Palladium") ? compound.getCompound("Palladium") : new CompoundTag();
        palladiumTag.put("Properties", this.palladium$propertyHandler.toNBT(true));
        compound.put("Palladium", palladiumTag);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"), method = "moveTowardsClosestSpace", cancellable = true)
    protected void pushOutOfBlocks(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity living) {
            for (AbilityInstance<IntangibilityAbility> entry : AbilityUtil.getEnabledInstances(living, AbilitySerializers.INTANGIBILITY.get())) {
                if (IntangibilityAbility.canGoThrough(entry, this.level.getBlockState(BlockPos.containing(x, y, z)))) {
                    ci.cancel();
                    return;
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        var entity = (Entity) (Object) this;
        if (entity.level().isClientSide && !(entity instanceof LivingEntity)) {
            this.palladium$trailHandler.tick();
        }
    }

    @Override
    public EntityPropertyHandler palladium$getPropertyHandler() {
        return this.palladium$propertyHandler;
    }

    @Override
    public TrailHandler palladium$getTrailHandler() {
        return this.palladium$trailHandler;
    }
}
