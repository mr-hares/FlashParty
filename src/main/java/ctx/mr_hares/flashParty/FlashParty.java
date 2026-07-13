package ctx.mr_hares.flashParty;

import ctx.mr_hares.flashParty.command.flashparty;
import ctx.mr_hares.flashParty.command.party;
import ctx.mr_hares.flashParty.listener.PlayerChat;
import ctx.mr_hares.flashParty.utils.DataBase;
import ctx.mr_hares.flashParty.utils.LocaleManager;
import ctx.mr_hares.flashParty.utils.MiniManager;
import ctx.mr_hares.flashParty.utils.PartyPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class FlashParty extends JavaPlugin {
    private static FlashParty instance;
    private static DataBase DataBase;
    private static LocaleManager localeManager = null;
    private static MiniManager miniManager;

    @Override
    public void onEnable() {
        instance = this;
        miniManager = new MiniManager(this);

        sendConsole("&f&r\n  <gradient:#96FB57:#70FFC3>FlashParty</gradient> " +
                "&7by " +
                "mr_hares&r\n  &fEnable plugin &7| &fVersions " + getDescription().getVersion() + "&r\n&f&r");

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
        sendConsole("&f&r\n  <gradient:#96FB57:#70FFC3>FlashParty</gradient> &7by mr_hares&r\n  &fDisable plugin &7| &fVersions " + getDescription().getVersion() + "&r\n&f&r");
        miniManager.disable();
    }

    public static FlashParty getInstance() {
        return instance;
    }

    public static LocaleManager getLocaleManager() {
        return localeManager;
    }

    public static MiniManager getMiniManager() { return miniManager; }

    public static DataBase getDataBase() {
        return DataBase;
    }

    public static void sendConsole(String text) {
        Bukkit.getServer().getConsoleSender().sendMessage(miniManager.deserialize(text));
    }
}
