package com.otherworld.owrp;

import com.otherworld.owrp.commands.*;
import com.otherworld.owrp.dependencies.DependencyManager;
import com.otherworld.owrp.handlers.CommandsHandler;
import com.otherworld.owrp.listeners.ExpressionsChatListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public final class OWRP extends JavaPlugin {

    private final Map<Class<?>, Object> dependenciesMap = new HashMap<>();

    private static OWRP instance;

    public static OWRP instance() {
        return OWRP.instance;
    }

    @Override
    public void onEnable() {
        OWRP.instance = OWRP.this;

        handleConfigurationFile();

        register(DependencyManager.class, new DependencyManager(this));

        getCommand("owrp").setExecutor(new ReloadCommand(this));

        new CommandsHandler(this).registerCommands();

        Bukkit.getPluginManager().registerEvents(new ExpressionsChatListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void handleConfigurationFile() {
        saveDefaultConfig();

        Reader defaultStream = null;
        YamlConfiguration defaultConfig = null;
        try {
            defaultStream = new InputStreamReader(getResource("config.yml"), "UTF8");
        } catch (UnsupportedEncodingException ex) { }
        if (defaultStream != null) {
            defaultConfig = YamlConfiguration.loadConfiguration(defaultStream);
        }

        Integer actualVersion = defaultConfig.getInt("configVersion");
        Integer loadedVersion = getConfig().getInt("configVersion");

        if (actualVersion > loadedVersion) {
            File file = new File(getDataFolder(), "config.yml");
            file.renameTo(new File(getDataFolder(), "config.yml.old"));
        }
    }

    @SuppressWarnings("all")
    public <T> T getExact(Class<T> clazz) {
        return (T) dependenciesMap.get(clazz);
    }

    public <T> void register(Class<T> clazz, T object) {
        if (dependenciesMap.containsKey(clazz)) {
            throw new IllegalStateException("Dependency is already registered");
        }

        dependenciesMap.put(clazz, object);
    }

    public <T> void unregister(Class<T> clazz) {
        if (!dependenciesMap.containsKey(clazz)) {
            throw new IllegalStateException("Dependency is not registered");
        }

        dependenciesMap.remove(clazz);
    }
}
