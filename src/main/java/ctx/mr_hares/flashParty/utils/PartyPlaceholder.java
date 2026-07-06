package ctx.mr_hares.flashParty.utils;

import ctx.mr_hares.flashParty.FlashParty;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static ctx.mr_hares.flashParty.FlashParty.*;

public class PartyPlaceholder extends PlaceholderExpansion {

    private String stripColor(String coloredMessage) {
        if (coloredMessage == null || coloredMessage.isEmpty()) {
            return "";
        }

        return coloredMessage.replaceAll("§[0-9A-Fa-fK-ORx]", "");
    }

    private final FlashParty plugin;

    public PartyPlaceholder(FlashParty plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "flashparty";
    }

    @Override
    public @NotNull String getAuthor() {
        return "mr_hares";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        if (params.equalsIgnoreCase("tag")) {
            Member member = getDataBase().getMember(player.getUniqueId());
            if (member != null) {
                return Objects.equals(member.getParty().getTag(), "отсутствует") ? "" :
                        " " + member.getParty().getTag();
            }
        } else if (params.equalsIgnoreCase("name")) {
            Member member = getDataBase().getMember(player.getUniqueId());
            if (member != null) {
                return member.getParty().getName();
            }
        }

        return null;
    }
}