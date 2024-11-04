package com.oheers.fish.fishing.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.requirement.Requirement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rarity {

    public String loreOverride;
    public String permission;
    String value, colour;
    double weight;
    boolean announce;
    boolean fishWeighted;
    boolean hasCompExemptFish;
    boolean useConfigCasing;
    boolean shouldDisableFisherman;
    String displayName;
    Requirement requirement = new Requirement();
    double minSize;
    double maxSize;

    /**
     * Constructs a Rarity from its config section.
     * @param section The section for this rarity.
     */
    public Rarity(@NotNull Section section) {
        // This should never be null, but we have this check just to be safe.
        this.value = Objects.requireNonNull(section.getNameAsString());
        this.colour = section.getString("colour", "&f");
        this.weight = section.getDouble("weight");
        this.announce = section.getBoolean("broadcast");
        this.loreOverride = section.getString("override-lore");
        this.useConfigCasing = section.getBoolean("use-this-casing");
        this.permission = section.getString("permission");
        this.displayName = section.getString("displayname");
        this.shouldDisableFisherman = section.getBoolean("disable-fisherman", false);
        this.minSize = section.getDouble("size.minSize");
        this.maxSize = section.getDouble("size.maxSize");
        handleRequirements(section);
    }

    /**
     * Constructs a rarity with the provided values.
     * @deprecated Use {@link Rarity#Rarity(Section)} instead.
     */
    @Deprecated(forRemoval = true)
    public Rarity(String value, String colour, double weight, boolean announce, boolean useConfigCasing, String loreOverride) {
        this.value = value;
        this.colour = colour;
        this.weight = weight;
        this.announce = announce;
        this.loreOverride = loreOverride;
        this.useConfigCasing = useConfigCasing;
    }

    private void handleRequirements(@NotNull Section raritySection) {
        Section requirementSection = raritySection.getSection("requirements");
        requirement = new Requirement();
        if (requirementSection == null) {
            return;
        }
        requirementSection.getRoutesAsStrings(false).forEach(requirementString -> {
            List<String> values = new ArrayList<>();
            if (requirementSection.isList(requirementString)) {
                values.addAll(requirementSection.getStringList(requirementString));
            } else {
                values.add(requirementSection.getString(requirementString));
            }
            requirement.add(requirementString, values);
        });
    }

    public String getValue() {
        return this.value;
    }

    public String getColour() {
        return this.colour;
    }

    public double getWeight() {
        return this.weight;
    }

    public boolean getAnnounce() {
        return this.announce;
    }

    public boolean getUseConfigCasing() { return this.useConfigCasing; }

    public boolean isFishWeighted() {
        return fishWeighted;
    }

    public void setFishWeighted(boolean fishWeighted) {
        this.fishWeighted = fishWeighted;
    }

    public String getDisplayName() {
        if (displayName == null) {
            return value;
        }
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    public String getLorePrep() {
        if (loreOverride != null) return FishUtils.translateColorCodes(loreOverride);
        else {
            if (this.displayName != null) {
                return this.displayName;
            } else {
                String finalName = this.getValue();
                if (!useConfigCasing) {
                    finalName = finalName.toUpperCase();
                }
                return this.getColour() + "&l" + finalName;
            }
        }
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean hasCompExemptFish() {
        return hasCompExemptFish;
    }

    public void setHasCompExemptFish(boolean hasCompExemptFish) {
        this.hasCompExemptFish = hasCompExemptFish;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public boolean isShouldDisableFisherman() {
        return shouldDisableFisherman;
    }

    public double getMinSize() {
        return minSize;
    }

    public double getMaxSize() {
        return maxSize;
    }

}
