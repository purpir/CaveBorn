package ru.purpir.element;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.List;

public class ElementCommand {

    private static final SuggestionProvider<ServerCommandSource> SUGGEST_ELEMENTS = (context, builder) -> {
        for (Element element : Element.values()) {
            builder.suggest(element.getId());
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("element")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("info")
                    .executes(ElementCommand::info)
                )
                .then(CommandManager.argument("targets", EntityArgumentType.entities())
                    .then(CommandManager.argument("element", StringArgumentType.word())
                        .suggests(SUGGEST_ELEMENTS)
                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1))
                            .executes(ElementCommand::give)
                        )
                    )
                )
        );
    }

    private static int give(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
        String elementStr = StringArgumentType.getString(context, "element");
        int seconds = IntegerArgumentType.getInteger(context, "seconds");

        Element element = Element.byId(elementStr);
        if (element == null && elementStr.startsWith("caveborn:")) {
            element = Element.byId(elementStr.substring(9));
        }
        if (element == null) {
            context.getSource().sendError(
                Text.translatable("command.caveborn.element.invalid", elementStr)
                    .formatted(Formatting.RED)
            );
            return 0;
        }

        ElementSavedData data = ElementSavedData.get(context.getSource().getServer());

        for (Entity target : targets) {
            data.addElement(target, element, seconds);
        }

        Element finalElement = element;
        if (targets.size() == 1) {
            Entity target = targets.iterator().next();
            if (target.equals(context.getSource().getEntity())) {
                context.getSource().sendFeedback(
                    () -> Text.translatable("command.caveborn.element.given.self",
                        finalElement.getDisplayName(), seconds),
                    true
                );
            } else {
                Entity finalTarget = target;
                context.getSource().sendFeedback(
                    () -> Text.translatable("command.caveborn.element.given",
                        finalElement.getDisplayName(), finalTarget.getDisplayName(), seconds),
                    true
                );
            }
        } else {
            context.getSource().sendFeedback(
                () -> Text.translatable("command.caveborn.element.given.multiple",
                    finalElement.getDisplayName(), targets.size(), seconds),
                true
            );
        }

        return 1;
    }

    private static int info(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Entity entity = source.getEntity();

        if (entity == null) {
            source.sendError(Text.literal("This command can only be used by a player or entity"));
            return 0;
        }

        ElementSavedData data = ElementSavedData.get(source.getServer());
        List<ElementInstance> elements = data.getElements(entity);

        if (elements.isEmpty()) {
            source.sendFeedback(
                () -> Text.translatable("command.caveborn.element.info.none"),
                false
            );
            return 1;
        }

        source.sendFeedback(
            () -> Text.translatable("command.caveborn.element.info.header"),
            false
        );

        for (ElementInstance instance : elements) {
            int remaining = (instance.remainingTicks() + 19) / 20;
            source.sendFeedback(
                () -> Text.literal("  ")
                    .append(instance.element().getDisplayName())
                    .append(Text.literal(": " + remaining + "s")),
                false
            );
        }

        return 1;
    }
}
