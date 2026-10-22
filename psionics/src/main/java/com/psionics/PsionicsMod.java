package com.psionics;

import com.psionics.command.PsiCommands;
import com.psionics.data.PsiData;
import com.psionics.data.PsiManager;
import com.psionics.net.PsiSyncPayload;
import com.psionics.net.TogglePsiPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PsionicsMod implements ModInitializer {
    public static final String MOD_ID = "psionics";
    public static final int WITCHES_REQUIRED = 10;

    @Override
    public void onInitialize() {
        // Paquetes
        PayloadTypeRegistry.playC2S().register(TogglePsiPayload.ID, TogglePsiPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PsiSyncPayload.ID, PsiSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TogglePsiPayload.ID,
                (payload, context) -> PsiManager.toggle(context.player()));

        // Sincronizar al entrar al mundo
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PsiManager.sync(handler.player));

        // Comandos /modpsy
        PsiCommands.register();

        // Contador de brujas (solo Survival)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof WitchEntity)) return;
            if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;
            if (!player.interactionManager.getGameMode().isSurvivalLike()) return;

            PsiData d = PsiManager.data(player);
            if (d.hungerImmune) return;
            d.witchKills++;
            if (d.witchKills >= WITCHES_REQUIRED) {
                d.hungerImmune = true;
                player.sendMessage(Text.literal("¡Has alcanzado la Bendición Psiónica! Tu hambre está completamente sellada (10/10 brujas)."), false);
            }
            PsiManager.save(player);
            PsiManager.sync(player);
        });
    }
}
