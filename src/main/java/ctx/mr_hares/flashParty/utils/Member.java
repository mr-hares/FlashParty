package ctx.mr_hares.flashParty.utils;

import ctx.mr_hares.flashParty.FlashParty;

import java.util.UUID;

public class Member {
    private Party party;
    private UUID uuid;
    private int rank;

    public Member(Party party, UUID uuid, int rank) {
        this.party = party;
        this.uuid = uuid;
        this.rank = rank;
    }

    public Party getParty() {
        return party;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getRank() {
        return rank;
    }

    public String getNameRank() {
        String member = FlashParty.getInstance().getConfig().getString("ranks.1", "Участник");
        String zam = FlashParty.getInstance().getConfig().getString("ranks.2", "Заместитель");
        String owner = FlashParty.getInstance().getConfig().getString("ranks.3", "Основатель");

        return switch (rank) {
          case 2 -> zam;
          case 3 -> owner;
          default -> member;
        };
    }
}
