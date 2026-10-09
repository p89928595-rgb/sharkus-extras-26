package de.sharkus.extras;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public boolean appleSkin = true;
    public boolean healthIndicator = true;
    public boolean armorStatus = true;
    public boolean teamTracker = true;
    public boolean wordFilter = true;
    public boolean hitColor = true;
    public boolean effectTimer = true;
    public int hitColorIndex = 0;
    public List<String> blockedWords = new ArrayList<>();

    public static final String[] COLOR_NAMES = {"Rot", "Blau", "Grün", "Gelb", "Lila", "Cyan", "Weiß", "Orange"};
    public static final int[] COLORS = {0xFF0000, 0x2060FF, 0x20FF20, 0xFFFF00, 0xB020FF, 0x00FFFF, 0xFFFFFF, 0xFF8000};

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("sharkusextras.json");
    private static Config instance;

    public static Config get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static Config load() {
        try {
            if (Files.exists(PATH)) {
                Config c = GSON.fromJson(Files.readString(PATH), Config.class);
                if (c != null) {
                    if (c.blockedWords == null) c.blockedWords = new ArrayList<>();
                    return c;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Config();
    }

    public static void save() {
        try {
            Files.writeString(PATH, GSON.toJson(get()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int currentHitColor() {
        return COLORS[Math.floorMod(hitColorIndex, COLORS.length)];
    }
}
