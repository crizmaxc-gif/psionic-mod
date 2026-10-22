package com.psionics.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.psionics.PsionicsMod;
import com.psionics.data.PsiData;
import com.psionics.data.PsiManager;
import com.psionics.data.PsiState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public final class PsiCommands {
    private PsiCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Minecraft distingue mayúsculas: registramos "modpsy" y "Modpsy".
            for (String root : new String[]{"modpsy", "Modpsy"}) {
                dispatcher.register(build(root));
            }
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> build(String root) {
        LiteralArgumentBuilder<ServerCommandSource> b = CommandManager.literal(root);

        // Survival (todos)
        for (String name : new String[]{"brujas", "Brujas"}) {
            b.then(CommandManager.literal(name).executes(PsiCommands::witches));
        }

        // Solo OP (nivel 2)
        b.then(CommandManager.literal("mepowers").requires(s -> s.hasPermissionLevel(2))
                .executes(ctx -> modify(ctx, d -> {
                    d.unlocked = true;
                    d.active = true;
                }, "Poderes psiónicos base otorgados.")));

        b.then(CommandManager.literal("evolve").requires(s -> s.hasPermissionLevel(2))
                .then(CommandManager.literal("hungry").executes(ctx -> modify(ctx,
                        d -> d.hungerImmune = true, "Inmunidad al hambre otorgada.")))
                .then(CommandManager.literal("powers").executes(ctx -> modify(ctx, d -> {
                    d.unlocked = true;
                    d.level = Math.max(d.level, 2);
                }, "Evolución Nivel 2 activada.")))
                .then(CommandManager.literal("master").executes(ctx -> modify(ctx, d -> {
                    d.unlocked = true;
                    d.level = 3;
                }, "Evolución Maestra (Nivel 3) activada."))));

        b.then(CommandManager.literal("use").requires(s -> s.hasPermissionLevel(2))
                .then(CommandManager.argument("cantidad", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int n = IntegerArgumentType.getInteger(ctx, "cantidad");
                            PsiState.get(ctx.getSource().getServer()).usesRequired = n;
                            PsiState.get(ctx.getSource().getServer()).markDirty();
                            ctx.getSource().sendFeedback(() -> Text.literal(
                                    "Usos requeridos para evolucionar: " + n), true);
                            return 1;
                        })));
        return b;
    }

    private static int witches(CommandContext<ServerCommandSource> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        PsiData d = PsiManager.data(p);
        int n = Math.min(d.witchKills, PsionicsMod.WITCHES_REQUIRED);
        String msg = d.hungerImmune
                ? "¡Has alcanzado la Bendición Psiónica! Tu hambre está completamente sellada (10/10 brujas)."
                : "Llevas " + n + "/10 brujas eliminadas. Te faltan " + (PsionicsMod.WITCHES_REQUIRED - n) + " para la inmunidad al hambre.";
        ctx.getSource().sendFeedback(() -> Text.literal(msg), false);
        return 1;
    }

    private static int modify(CommandContext<ServerCommandSource> ctx, Consumer<PsiData> change, String msg)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        change.accept(PsiManager.data(p));
        PsiManager.save(p);
        PsiManager.sync(p);
        ctx.getSource().sendFeedback(() -> Text.literal(msg), true);
        return 1;
    }
}
