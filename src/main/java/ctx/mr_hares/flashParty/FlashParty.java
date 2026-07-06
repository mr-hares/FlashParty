package ctx.mr_hares.flashParty;

import ctx.mr_hares.flashParty.command.flashparty;
import ctx.mr_hares.flashParty.command.party;
import ctx.mr_hares.flashParty.listener.PlayerChat;
import ctx.mr_hares.flashParty.utils.DataBase;
import ctx.mr_hares.flashParty.utils.LocaleManager;
import ctx.mr_hares.flashParty.utils.PartyPlaceholder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlashParty extends JavaPlugin {
    private static FlashParty instance;
    private static DataBase DataBase;
    private static LocaleManager localeManager = null;

    @Override
    public void onEnable() {
        instance = this;

        sendConsole("&f&r\n  &#4dff4dFlashParty &7by mr_hares&r\n  &fEnable plugin &7| &fVersions " + getDescription().getVersion() + "&r\n&f&r");

        saveDefaultConfig();
        DataBase = new DataBase(this);
        localeManager = new LocaleManager(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PartyPlaceholder(this).register();
            sendConsole("(FlashParty) PlaceholderAPI успешно зарегистрирован!");
        } else {
            sendConsole("(FlashParty) PlaceholderAPI не найден! Плейсхолдеры не будут работать.");
        }

        Bukkit.getPluginManager().registerEvents(new PlayerChat(), this);

        new party();
        new flashparty();
    }

    @Override
    public void onDisable() {
        sendConsole("&f&r\n  &#4dff4dFlashParty &7by mr_hares&r\n  &fDisable plugin &7| &fVersions " + getDescription().getVersion() + "&r\n&f&r");
    }

    public static FlashParty getInstance() {
        return instance;
    }

    public static LocaleManager getLocaleManager() {
        return localeManager;
    }

    public static DataBase getDataBase() {
        return DataBase;
    }

    public static String color(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        if (getLocaleManager() != null) {
            message = message.replace("{prefix}", getLocaleManager().getYAML().getString("prefix",
                    "&7(&#4dff4dFlashParty&7) &r"));
        }

        Matcher matcher = Pattern.compile("&#([A-Fa-f0-9]{6})").matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static void sendConsole(String text) {
        Bukkit.getServer().getConsoleSender().sendMessage(color(text));
    }
}
