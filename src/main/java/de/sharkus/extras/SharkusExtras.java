package de.sharkus.extras;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SharkusExtras implements ClientModInitializer {
    public static final String MOD_ID = "sharkusextras";
    private static KeyMapping openKey;
    private static int lastAppliedColor = -1;

    @Override
    public void onInitializeClient() {
        Config.get();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.sharkusextras.open", InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) client.gui.setScreen(new ConfigScreen());
            updateHitColor(client);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "hud"), HudRenderer::extract);

        // Wortfilter: blockierte Nachrichten werden gar nicht erst angezeigt
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signed, sender, params, ts) ->
                !isBlocked(message.getString()));
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) ->
                overlay || !isBlocked(message.getString()));
    }

    public static boolean isBlocked(String text) {
        Config c = Config.get();
        if (!c.wordFilter || c.blockedWords.isEmpty()) return false;
        String raw = text.toLowerCase();
        String stripped = raw.replaceAll("[^\\p{L}\\p{N}]", "");
        for (String w : c.blockedWords) {
            String word = w.toLowerCase().trim();
            if (word.isEmpty()) continue;
            if (raw.contains(word)) return true;
            String ws = word.replaceAll("[^\\p{L}\\p{N}]", "");
            if (ws.length() >= 3 && stripped.contains(ws)) return true;
        }
        return false;
    }

    // ---------- Hit Color (per Reflection, damit Namensänderungen den Build nicht brechen) ----------
    private static Object findFieldOfType(Object holder, String typeName) throws Exception {
        for (Class<?> k = holder.getClass(); k != null; k = k.getSuperclass()) {
            for (Field f : k.getDeclaredFields()) {
                if (f.getType().getName().equals(typeName)) {
                    f.setAccessible(true);
                    return f.get(holder);
                }
            }
        }
        return null;
    }

    private static void updateHitColor(Minecraft mc) {
        Config c = Config.get();
        int rgb = c.hitColor ? c.currentHitColor() : 0xFF0000; // Vanilla = Rot
        if (rgb == lastAppliedColor) return;
        try {
            Object overlay = findFieldOfType(mc, "net.minecraft.client.renderer.texture.OverlayTexture");
            if (overlay == null) {
                Object gr = findFieldOfType(mc, "net.minecraft.client.renderer.GameRenderer");
                if (gr == null) return; // Spiel noch nicht bereit, später erneut versuchen
                overlay = findFieldOfType(gr, "net.minecraft.client.renderer.texture.OverlayTexture");
            }
            if (overlay == null) { lastAppliedColor = rgb; return; }
            Object tex = findFieldOfType(overlay, "net.minecraft.client.renderer.texture.DynamicTexture");
            if (tex == null) { lastAppliedColor = rgb; return; }
            Object img = tex.getClass().getMethod("getPixels").invoke(tex);
            if (img == null) { lastAppliedColor = rgb; return; }
            Method set = img.getClass().getMethod("setPixel", int.class, int.class, int.class);
            int argb = (178 << 24) | rgb; // gleiche Deckkraft wie Vanilla
            for (int y = 0; y < 8; y++)
                for (int x = 0; x < 16; x++)
                    set.invoke(img, x, y, argb);
            tex.getClass().getMethod("upload").invoke(tex);
            lastAppliedColor = rgb;
        } catch (Throwable t) {
            System.err.println("[Sharkus Extras] Hit Color konnte nicht gesetzt werden: " + t);
            lastAppliedColor = rgb;
        }
    }
}
