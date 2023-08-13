package com.oheers.fish.addons.impl;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.addons.ItemAddon;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.inventory.ItemStack;

public class Head64ItemAddon extends ItemAddon {
    @Override
    public String getPrefix() {
        return "head64";
    }

    @Override
    public String getPluginName() {
        return null;
    }

    @Override
    public String getAuthor() {
        return "EvenMoreFish";
    }

    @Override
    public ItemStack getItemStack(String id) {
        if(!Base64.isBase64(id)) {
            EvenMoreFish.logger.warning(() -> String.format("%s is not a valid base64 string.", id));
            return null;
        }

        return FishUtils.get(id);
    }

    @Override
    public boolean canRegister() {
        return true;
    }
}
