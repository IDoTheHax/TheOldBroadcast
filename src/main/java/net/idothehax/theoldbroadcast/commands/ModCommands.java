package net.idothehax.theoldbroadcast.commands;

import net.idothehax.theoldbroadcast.Theoldbroadcast;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SanityCommand.register(event.getDispatcher());
    }
}
