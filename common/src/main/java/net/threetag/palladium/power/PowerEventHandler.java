package net.threetag.palladium.power;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.ChatEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.threetag.palladium.condition.ChatMessageCondition;
import net.threetag.palladium.condition.Condition;
import net.threetag.palladium.core.event.PalladiumLifecycleEvents;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PowerEventHandler implements ChatEvent.Received {

    public static final List<String> CHECK_FOR_CHAT_MESSAGES = new ArrayList<>();

    public static void init() {
        var instance = new PowerEventHandler();
        ChatEvent.RECEIVED.register(instance);
    }

    @Override
    public EventResult received(@Nullable ServerPlayer player, Component component) {
        var msg = component.getString().trim();

        if (CHECK_FOR_CHAT_MESSAGES.contains(msg.toLowerCase(Locale.ROOT))) {
            for (AbilityInstance<?> entry : AbilityUtil.getInstances(player)) {
                for (Condition condition : entry.getAbility().getConditions().getUnlockingConditions()) {
                    if (condition instanceof ChatMessageCondition chat && chat.chatMessage.trim().equalsIgnoreCase(msg)) {
                        chat.onChat(player, entry);
                    }
                }
                for (Condition condition : entry.getAbility().getConditions().getEnablingConditions()) {
                    if (condition instanceof ChatMessageCondition chat && chat.chatMessage.trim().equalsIgnoreCase(msg)) {
                        chat.onChat(player, entry);
                    }
                }
            }
        }
        return EventResult.pass();
    }

}
