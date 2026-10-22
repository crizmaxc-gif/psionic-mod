package com.psionics.data;

import net.minecraft.nbt.NbtCompound;

/** Datos psiónicos de un jugador. level: 1 = base, 2 = evolución, 3 = maestra. */
public class PsiData {
    public boolean unlocked;     // tiene los poderes base
    public boolean active;       // isPsiActive
    public boolean hungerImmune; // inmunidad al hambre
    public int level = 1;
    public int uses;             // psi_power_uses
    public int witchKills;

    public NbtCompound toNbt() {
        NbtCompound n = new NbtCompound();
        n.putBoolean("unlocked", unlocked);
        n.putBoolean("active", active);
        n.putBoolean("hungerImmune", hungerImmune);
        n.putInt("level", level);
        n.putInt("uses", uses);
        n.putInt("witchKills", witchKills);
        return n;
    }

    public static PsiData fromNbt(NbtCompound n) {
        PsiData d = new PsiData();
        d.unlocked = n.getBoolean("unlocked");
        d.active = n.getBoolean("active");
        d.hungerImmune = n.getBoolean("hungerImmune");
        d.level = Math.max(1, n.getInt("level"));
        d.uses = n.getInt("uses");
        d.witchKills = n.getInt("witchKills");
        return d;
    }
}
