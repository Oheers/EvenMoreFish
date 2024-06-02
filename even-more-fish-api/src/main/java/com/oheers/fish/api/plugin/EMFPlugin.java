package com.oheers.fish.api.plugin;


import java.util.logging.Logger;

public interface EMFPlugin {

    void reload();

    static Logger getLogger() {
        return Logger.getLogger("EvenMoreFish");
    }

}
