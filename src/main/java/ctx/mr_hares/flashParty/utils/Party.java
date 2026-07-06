package ctx.mr_hares.flashParty.utils;

import java.util.Objects;

public class Party {
    private int clan_id;
    private String name;
    private String tag;

    public Party(int clan_id, String tag, String name) {
        this.clan_id = clan_id;
        this.tag = tag;
        this.name = name;
    }

    public int getClan_id() {
        return clan_id;
    }

    public String getTag() {
        return Objects.equals(tag, "not") ? "отсутствует" : tag;
    }

    public String getName() {
        return name;
    }
}
