package com.psionics.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Guarda en el mundo los datos de todos los jugadores. */
public class PsiState extends PersistentState {
    public static final int DEFAULT_USES_REQUIRED = 175;

    private final Map<UUID, PsiData> players = new HashMap<>();
    public int usesRequired = DEFAULT_USES_REQUIRED;

    public PsiData get(UUID id) {
        return players.computeIfAbsent(id, k -> new PsiData());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putInt("usesRequired", usesRequired);
        NbtCompound list = new NbtCompound();
        players.forEach((id, d) -> list.put(id.toString(), d.toNbt()));
        nbt.put("players", list);
        return nbt;
    }

    public static PsiState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        PsiState s = new PsiState();
        if (nbt.contains("usesRequired")) s.usesRequired = nbt.getInt("usesRequired");
        NbtCompound list = nbt.getCompound("players");
        for (String key : list.getKeys()) {
            s.players.put(UUID.fromString(key), PsiData.fromNbt(list.getCompound(key)));
        }
        return s;
    }

    private static final PersistentState.Type<PsiState> TYPE =
            new PersistentState.Type<>(PsiState::new, PsiState::fromNbt, null);

    public static PsiState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "psionics_state");
    }
}
