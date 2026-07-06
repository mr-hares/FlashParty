package ctx.mr_hares.flashParty.utils;

import ctx.mr_hares.flashParty.FlashParty;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LocaleManager {
    private YamlConfiguration yamlConfiguration;
    private JavaPlugin plugin;

    public LocaleManager(JavaPlugin plugin) {
        File messages = new File(plugin.getDataFolder(), "messages.yml");
        if (!messages.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.yamlConfiguration = YamlConfiguration.loadConfiguration(messages);
        this.plugin = plugin;

        FlashParty.sendConsole("(FlashParty) Инициализация messages.yml");
    }

    public String getString(String path) {
        try (InputStream defMessage = plugin.getResource("messages.yml");
             InputStreamReader reader = new InputStreamReader(defMessage, StandardCharsets.UTF_8)) {

            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
            return yamlConfiguration.getString(path, defaultConfig.getString(path));

        } catch (Exception e) { return null; }
    }

    public void reload() {
        File messages = new File(plugin.getDataFolder(), "messages.yml");
        if (!messages.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.yamlConfiguration = YamlConfiguration.loadConfiguration(messages);
    }

    public YamlConfiguration getYAML() { return yamlConfiguration; }
}
