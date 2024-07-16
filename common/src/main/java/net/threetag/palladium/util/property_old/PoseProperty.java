package net.threetag.palladium.util.property_old;

import net.minecraft.world.entity.Pose;

import java.util.Locale;

public class PoseProperty extends EnumPalladiumProperty<Pose> {

    public PoseProperty(String key) {
        super(key);
    }

    @Override
    public Pose[] getValues() {
        return Pose.values();
    }

    @Override
    public String getNameFromEnum(Pose value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getPropertyType() {
        return "pose";
    }
}
