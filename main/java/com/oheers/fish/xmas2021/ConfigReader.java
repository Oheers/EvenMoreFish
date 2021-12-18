package com.oheers.fish.xmas2021;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class ConfigReader {

    private final EvenMoreFish plugin;
    private FileConfiguration xmas2021Config;

    public ConfigReader(EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
        xmas2021Config = getConfig();
    }

    // Makes sure all th
    public void reload() {

        File xmas2021File = new File(this.plugin.getDataFolder(), "xmas2021/xmas2021.yml");

        if (!xmas2021File.exists()) {
            xmas2021File.getParentFile().mkdirs();
            this.plugin.saveResource("xmas2021/xmas2021.yml", false);
        }

        this.xmas2021Config = new YamlConfiguration();

        try {
            this.xmas2021Config.load(xmas2021File);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }

        EvenMoreFish.xmas2021Config = this;
    }

    public FileConfiguration getConfig() {
        if (this.xmas2021Config == null) reload();
        return this.xmas2021Config;
    }

    // Config methods

    private String getString(String path, String defaultValue) {
        String returning = xmas2021Config.getString(path);

        if (returning != null) return returning;
        else {
            EvenMoreFish.logger.log(Level.SEVERE, "You are missing the following value from your xmas2021.yml file: " + path);
            return defaultValue;
        }
    }

    private Boolean getBoolean(String path) {
        return xmas2021Config.getBoolean(path);
    }

    private Material getMaterial(String path, String defaultValue) {
        String returning = xmas2021Config.getString(path);

        if (returning != null) {
            try {
                return Material.valueOf(returning);
            } catch (IllegalArgumentException exception) {
                EvenMoreFish.logger.log(Level.SEVERE, "Error in xmas2021.yml file: (" + path + "). " + returning + " is not a Material.");
                return Material.AIR;
            }
        } else {
            EvenMoreFish.logger.log(Level.SEVERE, "You are missing the following value from your xmas2021.yml file: " + path);
            return Material.valueOf(defaultValue);
        }
    }

    private List<String> getLore(String path) {
        List<String> configLore = xmas2021Config.getStringList(path);

        if (configLore.size() == 0) {
            EvenMoreFish.logger.log(Level.SEVERE, "You are missing the following value from your xmas2021.yml file: " + path);
        } else {
            for (int i = 0; i < configLore.size(); i++) {
                configLore.set(i, FishUtils.translateHexColorCodes(configLore.get(i)));
            }
        }

        return configLore;
    }

    public String getGUIName() {
        return getString("gui.gui-name", "&8&m      &4 Christmas Fish | 2021 &8&m      ");
    }

    public Material getFillerMaterial() {
        return getMaterial("gui.filler-item", "GRAY_STAINED_GLASS_PANE");
    }

    public String getAdventItemName() {
        return getString("gui.fish-name", FishUtils.translateHexColorCodes("&#74d680Day {day} - {name}"));
    }

    public String getAdventLockedItemName() {
        return getString("gui.locked-fish-name", FishUtils.translateHexColorCodes("&cDay {day} - ???"));
    }

    public String getLengthFormat() {
        return getString("gui.largest-length-format", FishUtils.translateHexColorCodes("{length}cm"));
    }

    public List<String> getAdventItemLore() {
        return getLore("gui.fish-lore");
    }

    public List<String> getAdventLockedItemLore() {
        return getLore("gui.locked-fish-lore");
    }

    public Material getLockedFishMaterial() {
        return getMaterial("gui.locked-fish-material", "COD");
    }

    public Boolean isOneFishPerDay() {
        return getBoolean("general.one-fish-per-day");
    }

    public String getTimeUnitDay() {
        return getString("time-units.day", "d");
    }

    public String getTimeUnitHour() {
        return getString("time-units.hour", "h");
    }

    public String getTimeUnitMinute() {
        return getString("time-units.minute", "m");
    }

    public String getTimeUnitSecond() {
        return getString("time-units.second", "s");
    }

    public String getParticleMessage() {
        return getString("gui.particle-message", "You feel a sense of christmas spirit.");
    }

    public boolean doXmas2021Particles() {
        return getBoolean("general.do-particles");
    }

}