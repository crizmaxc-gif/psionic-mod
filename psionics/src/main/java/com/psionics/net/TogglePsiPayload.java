package com.psionics.net;

import com.psionics.PsionicsMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Cliente -> servidor: el jugador pulsó V. */
public record TogglePsiPayload() implements CustomPayload {
    public static final CustomPayload.Id<TogglePsiPayload> ID =
            new CustomPayload.Id<>(Identifier.of(PsionicsMod.MOD_ID, "toggle"));
    public static final PacketCodec<RegistryByteBuf, TogglePsiPayload> CODEC =
            PacketCodec.unit(new TogglePsiPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
