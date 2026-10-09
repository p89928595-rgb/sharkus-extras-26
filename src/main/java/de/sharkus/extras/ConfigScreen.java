package de.sharkus.extras;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConfigScreen extends Screen {
    private EditBox wordField;

    public ConfigScreen() {
        super(Component.literal("Sharkus Extras"));
    }

    private static Component label(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "§aAN" : "§cAUS"));
    }

    private void toggle(String name, int x, int y, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
            Config.save();
        }).bounds(x, y, 150, 20).build());
    }

    @Override
    protected void init() {
        Config c = Config.get();
        int L = width / 2 - 155, R = width / 2 + 5;
        int y = 30;

        toggle("AppleSkin", L, y, () -> c.appleSkin, v -> c.appleSkin = v);
        toggle("Health Indicator", R, y, () -> c.healthIndicator, v -> c.healthIndicator = v);
        y += 24;
        toggle("Armor Status", L, y, () -> c.armorStatus, v -> c.armorStatus = v);
        toggle("Team Tracker", R, y, () -> c.teamTracker, v -> c.teamTracker = v);
        y += 24;
        toggle("Effekt-Timer", L, y, () -> c.effectTimer, v -> c.effectTimer = v);
        toggle("Wortfilter", R, y, () -> c.wordFilter, v -> c.wordFilter = v);
        y += 24;
        toggle("Hit Color", L, y, () -> c.hitColor, v -> c.hitColor = v);
        addRenderableWidget(Button.builder(Component.literal("Farbe: " + Config.COLOR_NAMES[c.hitColorIndex]), b -> {
            c.hitColorIndex = (c.hitColorIndex + 1) % Config.COLORS.length;
            b.setMessage(Component.literal("Farbe: " + Config.COLOR_NAMES[c.hitColorIndex]));
            Config.save();
        }).bounds(R, y, 150, 20).build());
        y += 34;

        // Wortfilter-Liste
        wordField = new EditBox(font, L, y, 230, 20, Component.literal("Wort"));
        wordField.setHint(Component.literal("Wort zum Blockieren eingeben..."));
        wordField.setMaxLength(40);
        addRenderableWidget(wordField);
        addRenderableWidget(Button.builder(Component.literal("Hinzufügen"), b -> addWord())
                .bounds(L + 235, y, 75, 20).build());
        y += 26;

        int i = 0;
        for (String word : c.blockedWords) {
            if (i >= 12) break;
            int x = (i % 2 == 0) ? L : R;
            int yy = y + (i / 2) * 22;
            final String wd = word;
            addRenderableWidget(Button.builder(Component.literal("§c✖ §f" + word), b -> {
                c.blockedWords.remove(wd);
                Config.save();
                rebuildWidgets();
            }).bounds(x, yy, 150, 20).build());
            i++;
        }

        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(width / 2 - 50, height - 28, 100, 20).build());
    }

    private void addWord() {
        String w = wordField.getValue().trim();
        if (w.isEmpty()) return;
        Config c = Config.get();
        if (c.blockedWords.stream().noneMatch(x -> x.equalsIgnoreCase(w))) c.blockedWords.add(w);
        Config.save();
        rebuildWidgets();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        Component title = Component.literal("§b§lSharkus Extras");
        g.text(font, title, width / 2 - font.width(title) / 2, 12, 0xFFFFFFFF, true);
    }

    @Override
    public void onClose() {
        Config.save();
        super.onClose();
    }
}
