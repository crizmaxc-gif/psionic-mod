package com.psionics.client;

import com.psionics.net.PsiSyncPayload;

/** Copia local del estado psiónico (la usarán HUD, animaciones y teclas en fases siguientes). */
public final class ClientPsiState {
    public static boolean unlocked, active, hungerImmune;
    public static int level = 1, uses;

    private ClientPsiState() {}

    public static void update(PsiSyncPayload p) {
        unlocked = p.unlocked();
        active = p.active();
        level = p.level();
        uses = p.uses();
        hungerImmune = p.hungerImmune();
    }
}
