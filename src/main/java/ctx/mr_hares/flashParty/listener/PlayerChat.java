package ctx.mr_hares.flashParty.listener;

import ctx.mr_hares.flashParty.utils.Member;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

import static ctx.mr_hares.flashParty.FlashParty.*;

public class PlayerChat implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();
        Member member = getDataBase().getMember(player.getUniqueId());
        String symbol_chat = getInstance().getConfig().getString("party-chat.symbol", "@");

        if (message.isEmpty()) return;

        if (message.startsWith(symbol_chat) && member != null) {
            event.setCancelled(true);
            String content = message.substring(1);

            List<Member> members = getDataBase().getMembers(member.getParty().getClan_id());
            for (Member m: members) {
                Player p = Bukkit.getPlayer(m.getUuid());
                if (p != null) {
                    p.sendMessage(color(getInstance().getConfig().getString("party-chat.format",
                            "&7(&#4dff4dПати&7) &r{rank} {player} &#4dff4d- &r{message}")
                            .replace("{rank}", member.getNameRank())
                            .replace("{player}", player.getName())
                            .replace("{message}", content)
                    ));
                }
            }
        }
    }
}
