package ctx.mr_hares.flashParty.utils;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import static ctx.mr_hares.flashParty.FlashParty.*;

public class MiniManager {
    private BukkitAudiences adventure;
    private MiniMessage miniMessage = MiniMessage.miniMessage();

    public MiniManager(Plugin plugin) {
        this.adventure = BukkitAudiences.create(plugin);
    }

    public void disable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("BukkitAudiences не инициализирован");
        }
        return this.adventure;
    }

    public Component deserialize(String text) {
        text = text.replace("{prefix}", getLocaleManager() != null ? getLocaleManager().getString("prefix") : "&7" +
                "(<gradient:#96FB57:#70FFC3>FlashParty</gradient>) &r");

        return miniMessage.deserialize(legacyToMiniMessage(text));
    }

    private String legacyToMiniMessage(String text) {
        return text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<b>")
                .replace("&o", "<i>")
                .replace("&n", "<u>")
                .replace("&m", "<st>")
                .replace("&k", "<obf>")
                .replace("&r", "<reset>");
    }
}
