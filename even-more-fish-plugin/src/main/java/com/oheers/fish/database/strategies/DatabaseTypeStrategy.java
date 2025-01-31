package com.oheers.fish.database.strategies;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jooq.conf.Settings;



public interface DatabaseTypeStrategy {
    default Settings applySettings(Settings settings, String tablePrefix, String dbName) {
        return settings;
    }
    default FluentConfiguration configureFlyway(FluentConfiguration flywayConfig) {
        return flywayConfig;
    }
}
