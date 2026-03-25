package ru.purpir.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.purpir.config.SolarAbilityConfig;

public class CavebornCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("caveborn")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("reload")
                    .executes(CavebornCommand::reloadConfig)
                )
        );
    }
    
    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            SolarAbilityConfig.reload();
            source.sendFeedback(
                () -> Text.translatable("command.caveborn.reload.success")
                    .formatted(Formatting.GREEN),
                true
            );
            return 1;
        } catch (Exception e) {
            source.sendError(
                Text.translatable("command.caveborn.reload.failed")
                    .formatted(Formatting.RED)
            );
            return 0;
        }
    }
}
