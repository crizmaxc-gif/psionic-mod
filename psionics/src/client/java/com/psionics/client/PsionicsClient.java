package com.psionics.client;

import com.psionics.net.PsiSyncPayload;
import com.psionics.net.TogglePsiPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PsionicsClient implements ClientModInitializer {
    public static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.psionics.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.psionics.main"));

        ClientPlayNetworking.registerGlobalReceiver(PsiSyncPayload.ID,
                (payload, context) -> ClientPsiState.update(payload));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new TogglePsiPayload());
                }
            }
        });
    }
}
