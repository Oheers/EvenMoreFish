/**
 * This file is part of PlaceholderAPI
 * <p>
 * PlaceholderAPI
 * Copyright (c) 2015 - 2021 PlaceholderAPI Team
 * <p>
 * PlaceholderAPI free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PlaceholderAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


package com.oheers.fish.api.addons;


import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class FileUtil {
    private FileUtil() {
        throw new UnsupportedOperationException();
    }

    public static <T> @NotNull List<CompletableFuture<Class<? extends T>>> findClassesAsync(@NotNull final File file, @NotNull final Class<T> clazz) throws CompletionException {
        if (!file.exists()) {
            return Collections.emptyList();
        }

        final List<CompletableFuture<Class<? extends T>>> futures = new ArrayList<>();

        final List<String> matches = new ArrayList<>();

        futures.add(
                CompletableFuture.supplyAsync(() -> {
                            try {
                                final URL jar = file.toURI().toURL();
                                try (final URLClassLoader loader = new URLClassLoader(new URL[]{jar}, clazz.getClassLoader())) {
                                    try (final JarInputStream stream = new JarInputStream(jar.openStream())) {
                                        JarEntry entry;
                                        while ((entry = stream.getNextJarEntry()) != null) {
                                            final String name = entry.getName();
                                            if (!name.endsWith(".class")) {
                                                continue;
                                            }

                                            matches.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
                                        }

                                        for (final String match : matches) {
                                            Class<? extends T> addonClass = loadClass(loader, match, clazz);
                                            if (addonClass != null) {
                                                return addonClass;
                                            }
                                        }
                                    }
                                }
                            } catch (final VerifyError ex) {
                                //todo, this can't be here it's blocking
                                Bukkit.getLogger().severe(() -> String.format("Failed to load addon class %s", file.getName()));
                                Bukkit.getLogger().severe(() -> String.format("Cause: %s %s", ex.getClass().getSimpleName(), ex.getMessage()));
                                return null;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new CompletionException(e.getCause());
                            }
                            return null;
                        }
                )
        );
        return futures;
    }

    private static <T> @Nullable Class<? extends T> loadClass(final @NotNull URLClassLoader loader, final String match, @NotNull final Class<T> clazz) throws ClassNotFoundException {
        try {
            final Class<?> loaded = loader.loadClass(match);
            if (clazz.isAssignableFrom(loaded)) {
                return (loaded.asSubclass(clazz));
            }
        } catch (final NoClassDefFoundError ignored) {
        }
        return null;
    }


}