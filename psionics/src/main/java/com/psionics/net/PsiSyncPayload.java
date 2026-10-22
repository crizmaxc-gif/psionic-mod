package com.psionics.net;

import com.psionics.PsionicsMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Servidor -> cliente: estado psiónico del jugador. */
public record PsiSyncPayload(boolean unlocked, boolean active, int level, int uses, boolean hungerImmune)
        implements CustomPayload {
    public static final CustomPayload.Id<PsiSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(PsionicsMod.MOD_ID, "sync"));
    public static final PacketCodec<RegistryByteBuf, PsiSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, PsiSyncPayload::unlocked,
            PacketCodecs.BOOL, PsiSyncPayload::active,
            PacketCodecs.VAR_INT, PsiSyncPayload::level,
            PacketCodecs.VAR_INT, PsiSyncPayload::uses,
            PacketCodecs.BOOL, PsiSyncPayload::hungerImmune,
            PsiSyncPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
