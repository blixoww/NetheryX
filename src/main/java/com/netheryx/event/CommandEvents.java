package com.netheryx.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.netheryx.NetheryX;
import com.netheryx.world.NetherEventType;
import com.netheryx.world.NetherEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

/** Commande {@code /netheryx event start <type>|stop} (operateurs) pour declencher les evenements du Nether. */
@Mod.EventBusSubscriber(modid = NetheryX.MOD_ID)
public final class CommandEvents {
    private CommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("netheryx").requires(source -> source.hasPermission(2))
                .then(Commands.literal("event")
                        .then(Commands.literal("start").then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(NetherEventType.values())
                                        .filter(type -> type != NetherEventType.NONE).map(NetherEventType::id), builder))
                                .executes(context -> start(context.getSource(), StringArgumentType.getString(context, "type")))))
                        .then(Commands.literal("stop").executes(context -> stop(context.getSource())))));
    }

    private static int start(CommandSourceStack source, String id) {
        ServerLevel nether = source.getServer().getLevel(Level.NETHER);
        NetherEventType type = Arrays.stream(NetherEventType.values()).filter(t -> t.id().equals(id) && t != NetherEventType.NONE)
                .findFirst().orElse(null);
        if (nether == null || type == null) {
            source.sendFailure(Component.translatable("commands.netheryx.event.unknown", id));
            return 0;
        }
        NetherEvents.start(nether, type);
        source.sendSuccess(() -> Component.translatable("commands.netheryx.event.started", type.id()), true);
        return 1;
    }

    private static int stop(CommandSourceStack source) {
        ServerLevel nether = source.getServer().getLevel(Level.NETHER);
        if (nether == null) {
            return 0;
        }
        NetherEvents.stop(nether);
        source.sendSuccess(() -> Component.translatable("commands.netheryx.event.stopped"), true);
        return 1;
    }
}
