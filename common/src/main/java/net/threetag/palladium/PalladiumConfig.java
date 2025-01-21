package net.threetag.palladium;

import eu.midnightdust.lib.config.MidnightConfig;
import net.threetag.palladium.client.screen.component.UiAlignment;
import net.threetag.palladium.client.screen.abilitybar.AbilityKeyBindDisplay;

public class PalladiumConfig extends MidnightConfig {

    public static final String CATEGORY_CLIENT = "client";

    @Entry(category = CATEGORY_CLIENT)
    public static UiAlignment ABILITY_BAR_ALIGNMENT = UiAlignment.TOP_LEFT;

    @Entry(category = CATEGORY_CLIENT)
    public static AbilityKeyBindDisplay ABILITY_BAR_KEY_BIND_DISPLAY = AbilityKeyBindDisplay.INSIDE;

}
