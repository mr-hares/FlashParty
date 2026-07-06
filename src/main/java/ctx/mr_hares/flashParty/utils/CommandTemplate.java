package ctx.mr_hares.flashParty.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ctx.mr_hares.flashParty.FlashParty.color;

public abstract class CommandTemplate implements CommandExecutor, TabCompleter {
    public CommandTemplate(String command, JavaPlugin plugin) {
        PluginCommand pluginCommand = plugin.getCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
        }
    }

    public abstract void execute(CommandSender sender, String label, String[] args);

    public abstract List<String> complete(CommandSender sender, String[] args);

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        execute(sender, label, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command,
                                                @NonNull String alias, @NonNull String[] args) {
        return filter(complete(sender, args), args);
    }

    private List<String> filter(List<String> list, String[] args) {
        if (list == null) return null;
        String last = args[args.length - 1];
        List<String> result = new ArrayList<>();
        for (String arg : list) {
            if (arg.toLowerCase().startsWith(last.toLowerCase())) result.add(arg);
        }

        return result;
    }

    public TextComponent parseBBClickable(String rawText) {
        Pattern pattern = Pattern.compile("<on_click:([^>]+)>([^<]+)</on_click>");
        Matcher matcher = pattern.matcher(rawText);

        TextComponent result = new TextComponent("");
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String text = rawText.substring(lastEnd, matcher.start());
                BaseComponent[] components = TextComponent.fromLegacyText(color(text));
                for (BaseComponent comp : components) {
                    result.addExtra(comp);
                }
            }

            String command = matcher.group(1);
            String buttonText = matcher.group(2);

            BaseComponent[] buttonComponents = TextComponent.fromLegacyText(color(buttonText));
            TextComponent button = new TextComponent("");
            for (BaseComponent comp : buttonComponents) {
                button.addExtra(comp);
            }
            button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

            result.addExtra(button);
            lastEnd = matcher.end();
        }

        if (lastEnd < rawText.length()) {
            String text = rawText.substring(lastEnd);
            BaseComponent[] components = TextComponent.fromLegacyText(color(text));
            for (BaseComponent comp : components) {
                result.addExtra(comp);
            }
        }

        return result;
    }

    public String stripColor(String coloredMessage) {
        if (coloredMessage == null || coloredMessage.isEmpty()) {
            return "";
        }

        return coloredMessage.replaceAll("§[0-9A-Fa-fK-ORx]", "");
    }
}
