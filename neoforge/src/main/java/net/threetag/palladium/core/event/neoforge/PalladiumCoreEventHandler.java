package net.threetag.palladium.core.event.neoforge;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.core.event.PalladiumEntityEvents;
import net.threetag.palladium.core.event.PalladiumLifecycleEvents;

@EventBusSubscriber(modid = Palladium.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PalladiumCoreEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void entityTickPre(EntityTickEvent.Pre e) {
        PalladiumEntityEvents.TICK_PRE.invoker().entityTick(e.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void entityTickPost(EntityTickEvent.Post e) {
        PalladiumEntityEvents.TICK_POST.invoker().entityTick(e.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDataPackSync(OnDatapackSyncEvent e) {
        PalladiumLifecycleEvents.DATA_PACK_SYNC.invoker().onDataPackSync(e.getPlayerList(), e.getPlayer());
    }

}
