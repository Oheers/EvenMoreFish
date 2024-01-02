package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;

public class ConfigManager {
    private final EvenMoreFish plugin;

    public ConfigManager(EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    private FishFile fishFile;
    private RaritiesFile raritiesFile;
    private BaitFile baitFile;
    private CompetitionConfig competitionConfig;
    private Xmas2022Config xmas2022Config;

    private GUIConfig guiConfig;

    public void load() {
        this.fishFile = new FishFile(plugin);
        this.raritiesFile = new RaritiesFile(plugin);
        this.baitFile = new BaitFile(plugin);
        this.competitionConfig = new CompetitionConfig(plugin);
        this.xmas2022Config = new Xmas2022Config(plugin);
        if (EvenMoreFish.mainConfig.debugSession()) {
            this.guiConfig = new GUIConfig(plugin);
        }
    }

    public void reload() {
        this.fishFile.reload();
        this.raritiesFile.reload();
        this.baitFile.reload();
        this.competitionConfig.reload();
        this.xmas2022Config.reload();
        if (EvenMoreFish.mainConfig.debugSession()) {
            this.guiConfig.reload();
        }
    }

    public FishFile getFishFile() {
        return fishFile;
    }

    public RaritiesFile getRaritiesFile() {
        return raritiesFile;
    }

    public BaitFile getBaitFile() {
        return baitFile;
    }

    public CompetitionConfig getCompetitionConfig() {
        return competitionConfig;
    }

    public Xmas2022Config getXmas2022Config() {
        return xmas2022Config;
    }

    public GUIConfig getGuiConfig() {
        return guiConfig;
    }
}
