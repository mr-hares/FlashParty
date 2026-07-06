package ctx.mr_hares.flashParty.command;

import ctx.mr_hares.flashParty.utils.CommandTemplate;
import ctx.mr_hares.flashParty.utils.Member;
import ctx.mr_hares.flashParty.utils.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static ctx.mr_hares.flashParty.FlashParty.*;

import java.util.List;
import java.util.stream.Collectors;

public class flashparty extends CommandTemplate {
    public flashparty() {
        super("flashparty", getInstance());
    }

    private int getClanId(String party_name) {
        List<Party> parties = getDataBase().getParties();
        for (Party party: parties) {
            if (party.getName().equals(party_name)) {
                return party.getClan_id();
            }
        }

        return -1;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("flashparty.use")) {
            sender.sendMessage(color(getLocaleManager().getString("not-permission")));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(color("{prefix}&fСписок системных команд\n&7/fp reload - перезагрузить " +
                    "конфигурацию\n&7/fp remove [пати] - удалить пати"));
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("flashparty.reload")) {
                sender.sendMessage(color(getLocaleManager().getString("not-permission")));
                return;
            }

            getLocaleManager().reload();
            getInstance().reloadConfig();
            sender.sendMessage(color(getLocaleManager().getString("reload")));
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("flashparty.remove")) {
                sender.sendMessage(color(getLocaleManager().getString("not-permission")));
                return;
            }

            if (args.length < 2) {
                sender.sendMessage(color("{prefix}Используйте: &#4dff4d/fp remove [пати]"));
                return;
            }

            String party_name = args[1];
            int clan_id = getClanId(party_name);
            if (clan_id == -1) {
                sender.sendMessage(color(getLocaleManager().getString("not-found-party")));
                return;
            }

            List<Member> members = getDataBase().getMembers(clan_id);
            getDataBase().removeParty(clan_id);
            sender.sendMessage(color(getLocaleManager().getString("remove")
                    .replace("{party}", party_name)
            ));
            for (Member member: members) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    player.sendMessage(color(getLocaleManager().getString("remove-notify")
                            .replace("{party}", party_name)
                    ));
                }
            }
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "remove");
        }

        if (args.length > 1) {
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("remove")) {
                    List<Party> parties = getDataBase().getParties();
                    return parties.stream().map(Party::getName).collect(Collectors.toList());
                }
            }
        }

        return List.of();
    }
}
