package com.psionics.data;

import com.psionics.net.PsiSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.text.Text;
import org.joml.Vector3f;

/** Lógica central: activar poderes, cobrar hambre, contar usos y evolucionar. */
public final class PsiManager {
    private PsiManager() {}

    public static PsiData data(ServerPlayerEntity player) {
        return PsiState.get(player.getServer()).get(player.getUuid());
    }

    public static void save(ServerPlayerEntity player) {
        PsiState.get(player.getServer()).markDirty();
    }

    public static void sync(ServerPlayerEntity player) {
        PsiData d = data(player);
        ServerPlayNetworking.send(player,
                new PsiSyncPayload(d.unlocked, d.active, d.level, d.uses, d.hungerImmune));
    }

    /** Tecla V. */
    public static void toggle(ServerPlayerEntity player) {
        PsiData d = data(player);
        if (!d.unlocked) {
            player.sendMessage(Text.literal("Aún no tienes poderes psiónicos."), true);
            failSound(player);
            return;
        }
        d.active = !d.active;
        save(player);
        sync(player);

        ServerWorld world = player.getServerWorld();
        world.spawnParticles(new DustParticleEffect(new Vector3f(0.6f, 0.2f, 0.9f), 1.2f),
                player.getX(), player.getEyeY(), player.getZ(), 25, 0.35, 0.35, 0.35, 0.0);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, d.active ? 1.3f : 0.8f);
        player.sendMessage(Text.literal(d.active ? "Poderes psiónicos: ACTIVADOS" : "Poderes psiónicos: DESACTIVADOS"), true);
    }

    /**
     * Las habilidades (fases siguientes) llaman a esto ANTES de ejecutarse.
     * Devuelve true si se puede usar: poderes activos y hambre suficiente.
     * Cobra 1 punto de hambre (salvo inmunidad), suma +1 uso y evoluciona al llegar al límite.
     */
    public static boolean tryUseAbility(ServerPlayerEntity player) {
        PsiData d = data(player);
        if (!d.active) return false;

        if (!d.hungerImmune) {
            HungerManager hunger = player.getHungerManager();
            if (hunger.getFoodLevel() <= 0) {
                failSound(player);
                return false;
            }
            hunger.setFoodLevel(hunger.getFoodLevel() - 1);
        }

        d.uses++;
        PsiState state = PsiState.get(player.getServer());
        if (d.level < 2 && d.uses >= state.usesRequired) {
            d.level = 2;
            player.sendMessage(Text.literal("¡Has evolucionado al Nivel 2!"), false);
        }
        state.markDirty();
        sync(player);
        return true;
    }

    public static void failSound(ServerPlayerEntity player) {
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.8f, 1.0f);
    }
}
