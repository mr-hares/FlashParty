package ctx.mr_hares.flashParty.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ctx.mr_hares.flashParty.FlashParty;
import ctx.mr_hares.flashParty.utils.CommandTemplate;
import ctx.mr_hares.flashParty.utils.Member;
import ctx.mr_hares.flashParty.utils.Party;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static ctx.mr_hares.flashParty.FlashParty.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class party extends CommandTemplate {
    private Cache<UUID, UUID> invite_request = CacheBuilder.newBuilder()
            .expireAfterWrite(getInstance().getConfig().getInt("invite-requiest-time", 30), TimeUnit.SECONDS)
            .build();

    public party() {
        super("party", getInstance());
    }

    private @NotNull Boolean isOccupied(String name) {
        for (Party party: getDataBase().getParties()) {
            if (party.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    private @NotNull int getRank(String rank_name) {
        String member = FlashParty.getInstance().getConfig().getString("ranks.1", "Участник");
        String zam = FlashParty.getInstance().getConfig().getString("ranks.2", "Заместитель");
        String owner = FlashParty.getInstance().getConfig().getString("ranks.3", "Основатель");

        if (rank_name.equalsIgnoreCase(owner)) {
            return 3;
        } else if (rank_name.equalsIgnoreCase(zam)) {
            return 2;
        } else if (rank_name.equalsIgnoreCase(member)) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("{prefix}&fДанная команда доступна &#4dff4dтолько &fдля игрока"));
            return;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(color(String.join("\n",
                    getLocaleManager().getYAML().getStringList("help.command-list"))));
            return;
        }

        Member member = getDataBase().getMember(player.getUniqueId());

        if (args[0].equalsIgnoreCase("create")) {
            if (getInstance().getConfig().getInt("max-party", 7) > -1 &&
                    getDataBase().getParties().size() >= getInstance().getConfig().getInt("max-party", 7)) {
                player.sendMessage(color(getLocaleManager().getString("max-party")));
                return;
            }

            if (member != null) {
                player.sendMessage(color(getLocaleManager().getString("in-party")));
                return;
            }

            if (args.length < 2) {
                player.sendMessage(color(getLocaleManager().getString("help.use-create")));
                return;
            }

            String party_name = args[1];
            int max_length = getInstance().getConfig().getInt("party-name.max-length");
            int min_length = getInstance().getConfig().getInt("party-name.min-length");

            if (party_name.length() > max_length) {
                player.sendMessage(color(getLocaleManager().getString("name-is-long").replace("{max_length}",
                        String.valueOf(max_length))));
                return;
            }

            if (party_name.length() < min_length) {
                player.sendMessage(color(getLocaleManager().getString("name-is-short").replace("{min_length}",
                        String.valueOf(min_length))));
                return;
            }

            if (isOccupied(party_name)) {
                player.sendMessage(color(getLocaleManager().getString("name-is-occupied")));
                return;
            }

            getDataBase().createParty(player.getUniqueId(), party_name);
            player.sendMessage(color(getLocaleManager().getString("party-create")
                    .replace("{name}", party_name)
            ));
        } else if (args[0].equalsIgnoreCase("invite")) {
            if (member == null) {
                player.sendMessage(color(getLocaleManager().getString("not-in-party")));
                return;
            }

            if (member.getRank() < 2) {
                player.sendMessage(color(getLocaleManager().getString("not-rank")));
                return;
            }

            if (getInstance().getConfig().getInt(
                    "max-player-in-party", 10) > -1 && getDataBase().getMembers(member.getParty().getClan_id()).size() >= getInstance().getConfig().getInt(
                    "max-player-in-party", 10)) {
                player.sendMessage(color(getLocaleManager().getString("max-players")));
                return;
            }

            if (args.length < 2) {
                player.sendMessage(color(getLocaleManager().getString("help.use-invite")));
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(color(getLocaleManager().getString("not-found-player")));
                return;
            }

            if (target == player) {
                player.sendMessage(color(getLocaleManager().getString("with-myself")));
                return;
            }

            Member tar_member = getDataBase().getMember(target.getUniqueId());
            if (tar_member != null) {
                player.sendMessage(color(getLocaleManager().getString("player-in-party")));
                return;
            }

            if (invite_request.asMap().containsKey(target.getUniqueId())) {
                player.sendMessage(color(getLocaleManager().getString("is-invite")));
                return;
            }

            invite_request.put(target.getUniqueId(), player.getUniqueId());
            player.sendMessage(color(getLocaleManager().getString("invited").replace("{player}", target.getName())));
            target.spigot().sendMessage(parseBBClickable(getLocaleManager().getString("invite-notify")
                    .replace("{player}", player.getName())
                    .replace("{party}", member.getParty().getName())
            ));
        } else if (args[0].equalsIgnoreCase("accept")) {
            if (member != null) {
                player.sendMessage(color(getLocaleManager().getString("in-party")));
                return;
            }

            if (!invite_request.asMap().containsKey(player.getUniqueId())) {
                player.sendMessage(color(getLocaleManager().getString("not-offers")));
                return;
            }

            Member target_member = getDataBase().getMember(invite_request.asMap().get(player.getUniqueId()));
            if (target_member == null) return;

            invite_request.asMap().remove(player.getUniqueId());
            getDataBase().addMember(target_member.getParty().getClan_id(), player.getUniqueId(), 1);
            player.sendMessage(color(getLocaleManager().getString("accept")));
            Player tar = Bukkit.getPlayer(target_member.getUuid());
            if (tar != null) {
                tar.sendMessage(color(getLocaleManager().getString("accept-notify").replace("{player}", player.getName())));
            }
        } else if (args[0].equalsIgnoreCase("deny")) {
            if (!invite_request.asMap().containsKey(player.getUniqueId())) {
                player.sendMessage(color(getLocaleManager().getString("not-offers")));
                return;
            }

            Member target_member = getDataBase().getMember(invite_request.asMap().get(player.getUniqueId()));
            if (target_member == null) return;

            invite_request.asMap().remove(player.getUniqueId());
            player.sendMessage(color(getLocaleManager().getString("deny")));
            Player tar = Bukkit.getPlayer(target_member.getUuid());
            if (tar != null) {
                tar.sendMessage(color(getLocaleManager().getString("deny-notify").replace("{player}",
                        player.getName())));
            }
        } else if (args[0].equalsIgnoreCase("leave")) {
            if (member == null) {
                player.sendMessage(color(getLocaleManager().getString("not-in-party")));
                return;
            }

            if (member.getRank() == 3) {
                getDataBase().removeParty(member.getParty().getClan_id());
                player.sendMessage(color(getLocaleManager().getString("leave-owner")));
                return;
            }

            getDataBase().removeMember(member.getParty().getClan_id(), member.getUuid());
            player.sendMessage(color(getLocaleManager().getString("leave")));
        } else if (args[0].equalsIgnoreCase("tag")) {
            if (member == null) {
                player.sendMessage(color(getLocaleManager().getString("not-in-party")));
                return;
            }

            if (member.getRank() < 3) {
                player.sendMessage(color(getLocaleManager().getString("not-rank")));
                return;
            }
            
            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("remove")) {
                    getDataBase().editTagParty(member.getParty().getClan_id(), "not");
                    player.sendMessage(color(getLocaleManager().getString("tag-remove")));
                    return;
                }
            }

            if (args.length < 3) {
                player.sendMessage(color(getLocaleManager().getString("help.use-tag")));
                return;
            }

            String color = getInstance().getConfig().getString("tag.colors." + args[1], "&r");
            String tag = args[2];

            getDataBase().editTagParty(member.getParty().getClan_id(), color + tag);
            player.sendMessage(color(getLocaleManager().getString("tag-add")));
        } else if (args[0].equalsIgnoreCase("rename")) {
            if (member == null) {
                player.sendMessage(color(getLocaleManager().getString("not-in-party")));
                return;
            }

            if (member.getRank() < 3) {
                player.sendMessage(color(getLocaleManager().getString("not-rank")));
                return;
            }

            if (args.length < 2) {
                player.sendMessage(color(getLocaleManager().getString("help.use-rename")));
                return;
            }

            if (isOccupied(args[1])) {
                player.sendMessage(color(getLocaleManager().getString("name-is-occupied")));
                return;
            }

            getDataBase().editNameParty(member.getParty().getClan_id(), args[1]);
            player.sendMessage(color(getLocaleManager().getString("rename")));
        } else if (args[0].equalsIgnoreCase("kick")) {
            if (member == null) {
                player.sendMessage(color(getLocaleManager().getString("not-in-party")));
                return;
            }

            if (member.getRank() < 2) {
                player.sendMessage(color(getLocaleManager().getString("not-rank")));
                return;
            }

            if (args.length < 2) {
                player.sendMessage(color(getLocaleManager().getString("help.use-kick")));
                return;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (!target.hasPlayedBefore()) {
                player.sendMessage(color(getLocaleManager().getString("not-found-player")));
                return;
            }

            if (target.getUniqueId() == player.getUniqueId()) {
                player.sendMessage(color(getLocaleManager().getString("with-myself")));
                return;
            }

            Member tar_member = getDataBase().getMember(target.getUniqueId());
            if (tar_member == null || tar_member.getParty().getClan_id() != member.getParty().getClan_id()) {
                player.sendMessage(color(getLocaleManager().getString("player-not-in-party")));
                return;
            }

            getDataBase().removeMember(member.getParty().getClan_id(), target.getUniqueId());
            player.sendMessage(color(getLocaleManager().getString("kick-player")
                    .replace("{player}", target.getName())
            ));
            if (target.isOnline()) {
                target.getPlayer().sendMessage(color(getLocaleManager().getString("kick-notify")
                        .replace("{party}", member.getParty().getName())
                ));
            }
        } else if (args[0].equalsIgnoreCase("rank")) {
            if (member == null) {
                player.sendMessage(color(getLocaleManager().getString("not-in-party")));
                return;
            }

            if (member.getRank() < 3) {
                player.sendMessage(color(getLocaleManager().getString("not-rank")));
                return;
            }

            if (args.length < 3) {
                player.sendMessage(color(getLocaleManager().getString("help.use-rank")));
                return;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (!target.hasPlayedBefore()) {
                player.sendMessage(color(getLocaleManager().getString("not-found-player")));
                return;
            }

            if (target.getUniqueId() == player.getUniqueId()) {
                player.sendMessage(color(getLocaleManager().getString("with-myself")));
                return;
            }

            Member tar_member = getDataBase().getMember(target.getUniqueId());
            if (tar_member == null || tar_member.getParty().getClan_id() != member.getParty().getClan_id()) {
                player.sendMessage(color(getLocaleManager().getString("player-not-in-party")));
                return;
            }

            String rank = args[2];
            if (getRank(rank) == -1) {
                player.sendMessage(color(getLocaleManager().getString("not-found-rank")));
                return;
            }

            if (tar_member.getRank() == getRank(rank)) {
                player.sendMessage(color(getLocaleManager().getString("already-have-rank")));
                return;
            }

            getDataBase().editRank(target.getUniqueId(), getRank(rank));
            player.sendMessage(color(getLocaleManager().getString("edit-rank")
                    .replace("{player}", target.getName())
                    .replace("{rank}", rank)
            ));
            if (target.isOnline()) {
                target.getPlayer().sendMessage(color(getLocaleManager().getString("edit-notify")
                        .replace("{rank}", rank)
                ));
            }
        } else if (args[0].equalsIgnoreCase("info")) {
            if (member == null) {
                player.sendMessage(color(getLocaleManager().getString("not-in-party")));
                return;
            }

            Party party = getDataBase().getParty(member.getParty().getClan_id());
            List<Member> members = getDataBase().getMembers(party.getClan_id());
            List<String> deputies =
                    members.stream().filter(m -> m.getRank() == 2).map(m -> Bukkit.getOfflinePlayer(m.getUuid()).getName()).toList();
            List<String> mems =
                    members.stream().filter(m -> m.getRank() == 1).map(m -> Bukkit.getOfflinePlayer(m.getUuid()).getName()).toList();

            List<String> message = getLocaleManager().getYAML().getStringList("party-info");

            player.sendMessage(color(String.join("\n", message)
                    .replace("{owner}", getOwner(members))
                    .replace("{deputies}", deputies.isEmpty() ? "пусто" : String.join(", ", deputies))
                    .replace("{tag}", party.getTag())
                    .replace("{members}", mems.isEmpty() ? "пусто": String.join(", ", mems))
                    .replace("{party}", party.getName())));
        } else if (args[0].equalsIgnoreCase("list")) {
            List<Party> parties = getDataBase().getParties();
            if (parties.isEmpty()) {
                player.sendMessage(color(getLocaleManager().getString("list-empty")));
                return;
            }

            player.sendMessage(color(getLocaleManager().getString("list-header")));

            for (Party party: parties) {
                player.sendMessage(color(getLocaleManager().getString("list-item")
                        .replace("{name}", party.getName())
                        .replace("{members}", String.valueOf(getDataBase().getMembers(party.getClan_id()).size()))
                        .replace("{tag}", party.getTag() == "not" ? "отсутствует" : party.getTag())
                ));
            }
        } else {
            player.sendMessage(color(String.join("\n",
                    getLocaleManager().getYAML().getStringList("help.command-list"))));
            return;
        }
    }

    private String getOwner(List<Member> members) {
        for (Member m: members) {
            if (m.getRank() == 3) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(m.getUuid());
                return offlinePlayer.getName();
            }
        }

        return null;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> arglist = new ArrayList<>();
            if (getDataBase().getMember(((Player) sender).getUniqueId()) == null) {
                arglist.add("create");
            }
            arglist.addAll(List.of("invite", "accept", "deny", "tag", "rename", "kick", "rank", "list", "leave",
                    "info"));
            return arglist;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("tag")) {
                ConfigurationSection section = getInstance().getConfig().getConfigurationSection("tag.colors");
                if (section != null) {
                    List<String> ls = new ArrayList<>(section.getKeys(false));
                    ls.add("remove");
                    return ls;
                }
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("rank") || args[0].equalsIgnoreCase("kick")) {
                Member member = getDataBase().getMember(((Player) sender).getUniqueId());
                if (member == null) return List.of();
                List<Member> members = getDataBase().getMembers(member.getParty().getClan_id());
                return members.stream().map(m -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(m.getUuid());
                    return offlinePlayer.getName();
                }).collect(Collectors.toList());
            }

            if (args[0].equalsIgnoreCase("invite")) {
                return null;
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("rank")) {
                String member = FlashParty.getInstance().getConfig().getString("ranks.1", "Участник");
                String zam = FlashParty.getInstance().getConfig().getString("ranks.2", "Заместитель");

                return List.of(member, zam);
            }
        }

        return List.of();
    }
}
