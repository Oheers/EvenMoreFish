package com.oheers.fish.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

public class ManifestUtil {
    private static final Logger logger = LoggerFactory.getLogger(ManifestUtil.class.getName());
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";

    private ManifestUtil(){
        throw new UnsupportedOperationException();
    }

    public static String getAttributeFromManifest(final String key, final String defaultValue) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        try (InputStream inputStream = ManifestUtil.class.getClassLoader().getResourceAsStream(MANIFEST_PATH)) {
            if (inputStream != null) {
                Manifest manifest = new Manifest(inputStream);
                String value = manifest.getMainAttributes().getValue(key);
                return value != null ? value : defaultValue;
            }
        } catch (IOException e) {
            logger.error("Error reading manifest file: {}", e.getMessage());
        }

        // Fallback to default value
        return defaultValue;
    }
}
