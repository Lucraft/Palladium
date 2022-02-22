package net.threetag.palladium.client;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.threetag.palladium.client.screen.AbilityBarRenderer;
import net.threetag.palladium.network.AbilityKeyPressedMessage;
import net.threetag.palladium.power.ability.AbilityEntry;
import org.lwjgl.glfw.GLFW;

public class PalladiumKeyMappings {

    public static final String CATEGORY = "key.palladium.categories.abilities";
    public static AbilityKeyMapping[] ABILITY_KEYS = new AbilityKeyMapping[5];

    public static void init() {
        for (int i = 0; i < ABILITY_KEYS.length; i++) {
            KeyMappingRegistry.register(ABILITY_KEYS[i] = new AbilityKeyMapping("key.palladium.ability_" + i, i == 1 ? 86 : i == 2 ? 66 : i == 3 ? 78 : i == 4 ? 77 : i == 5 ? 44 : -1, CATEGORY, i));
        }

        ClientRawInputEvent.KEY_PRESSED.register((client, keyCode, scanCode, action, modifiers) -> {
            if (AbilityBarRenderer.ABILITY_LIST != null && action != GLFW.GLFW_REPEAT) {
                for (AbilityKeyMapping key : ABILITY_KEYS) {
                    AbilityEntry entry = AbilityBarRenderer.ABILITY_LIST.getAbilities()[key.index];

                    if (key.matches(keyCode, scanCode) && entry != null) {
                        new AbilityKeyPressedMessage(AbilityBarRenderer.ABILITY_LIST.getProvider(), entry.id, action == GLFW.GLFW_PRESS).sendToServer();
                    }
                }
            }
            return EventResult.pass();
        });
    }

    public static class AbilityKeyMapping extends KeyMapping {

        public final int index;

        public AbilityKeyMapping(String description, int keyCode, String category, int index) {
            super(description, keyCode, category);
            this.index = index;
        }
    }

}