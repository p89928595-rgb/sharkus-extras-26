package de.sharkus.extras;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public class HudRenderer {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static void extract(GuiGraphicsExtractor g, DeltaTracker tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        Config c = Config.get();
        Font font = mc.font;
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        if (c.appleSkin) appleSkin(g, mc, font, w, h);
        if (c.healthIndicator) healthIndicator(g, mc, font, w);
        if (c.armorStatus) armorStatus(g, mc, font, h);
        if (c.teamTracker) teamTracker(g, mc, font);
        if (c.effectTimer) effectTimer(g, mc, font, w);
    }

    // ---------- AppleSkin ----------
    private static void appleSkin(GuiGraphicsExtractor g, Minecraft mc, Font font, int w, int h) {
        LocalPlayer p = mc.player;
        ItemStack stack = p.getMainHandItem();
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            stack = p.getOffhandItem();
            food = stack.get(DataComponents.FOOD);
        }
        int level = p.getFoodData().getFoodLevel();
        float sat = p.getFoodData().getSaturationLevel();
        int x = w / 2 + 10;
        int y = h - 62;

        g.text(font, String.format("Hunger %d/20 | Sättigung %.1f", level, sat), x, y, 0xFFFFFFFF, true);
        if (food != null) {
            int newLevel = Math.min(20, level + food.nutrition());
            float newSat = Math.min(newLevel, sat + food.saturation());
            g.text(font, String.format("+%d Hunger (%d), +%.1f Sättigung (%.1f)",
                    food.nutrition(), newLevel, food.saturation(), newSat), x, y - 10, 0xFF55FF55, true);
        }
    }

    // ---------- Health Indicator ----------
    private static void healthIndicator(GuiGraphicsExtractor g, Minecraft mc, Font font, int w) {
        if (!(mc.crosshairPickEntity instanceof LivingEntity e) || !e.isAlive()) return;
        String s = String.format("♥ %.1f / %.1f", e.getHealth(), e.getMaxHealth());
        if (e.getAbsorptionAmount() > 0) s += String.format(" (+%.1f)", e.getAbsorptionAmount());
        float ratio = e.getHealth() / Math.max(1f, e.getMaxHealth());
        int color = ratio > 0.6f ? 0xFF55FF55 : ratio > 0.3f ? 0xFFFFFF55 : 0xFFFF5555;
        Component name = e.getDisplayName();
        g.text(font, name, w / 2 - font.width(name) / 2, 28, 0xFFFFFFFF, true);
        g.text(font, s, w / 2 - font.width(s) / 2, 40, color, true);
    }

    // ---------- Armor Status ----------
    private static void armorStatus(GuiGraphicsExtractor g, Minecraft mc, Font font, int h) {
        int x = 4;
        int y = h / 2 - 40;
        int row = 0;
        for (EquipmentSlot slot : ARMOR) {
            ItemStack s = mc.player.getItemBySlot(slot);
            if (s.isEmpty()) continue;
            int yy = y + row * 18;
            g.item(s, x, yy);
            if (s.isDamageableItem()) {
                int left = s.getMaxDamage() - s.getDamageValue();
                int pct = Math.round(100f * left / s.getMaxDamage());
                int color = pct > 60 ? 0xFF55FF55 : pct > 30 ? 0xFFFFFF55 : 0xFFFF5555;
                g.text(font, left + " (" + pct + "%)", x + 20, yy + 4, color, true);
            }
            row++;
        }
    }

    // ---------- Team Tracker ----------
    private static void teamTracker(GuiGraphicsExtractor g, Minecraft mc, Font font) {
        LocalPlayer me = mc.player;
        List<AbstractClientPlayer> others = mc.level.players().stream()
                .filter(p -> p != me)
                .sorted(Comparator.comparingDouble(p -> p.distanceToSqr(me)))
                .limit(8).toList();
        if (others.isEmpty()) return;
        int x = 4, y = 4;
        g.text(font, "Spieler in der Nähe:", x, y, 0xFFFFAA00, true);
        y += 10;
        for (AbstractClientPlayer p : others) {
            boolean mate = me.getTeam() != null && me.getTeam() == p.getTeam();
            int dist = (int) Math.round(me.distanceTo(p));
            Component line = Component.empty()
                    .append(mate ? Component.literal("[Team] ").withColor(0x55FF55) : Component.empty())
                    .append(p.getDisplayName())
                    .append(Component.literal(" " + dist + "m").withColor(0xAAAAAA));
            g.text(font, line, x, y, 0xFFFFFFFF, true);
            y += 10;
        }
    }

    // ---------- Effekt-Timer ----------
    private static void effectTimer(GuiGraphicsExtractor g, Minecraft mc, Font font, int w) {
        int y = 50;
        for (MobEffectInstance e : mc.player.getActiveEffects()) {
            String time = e.isInfiniteDuration() ? "∞" : fmt(e.getDuration());
            String amp = e.getAmplifier() > 0 ? " " + (e.getAmplifier() + 1) : "";
            Component t = Component.empty()
                    .append(e.getEffect().value().getDisplayName())
                    .append(amp + " - " + time);
            boolean low = !e.isInfiniteDuration() && e.getDuration() < 200;
            g.text(font, t, w - font.width(t) - 4, y, low ? 0xFFFF5555 : 0xFFFFFFFF, true);
            y += 10;
        }
    }

    private static String fmt(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
