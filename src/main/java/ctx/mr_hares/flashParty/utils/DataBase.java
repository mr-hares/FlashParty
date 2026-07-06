package ctx.mr_hares.flashParty.utils;

import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.*;

public class DataBase {
    private Connection connection;

    public DataBase(Plugin plugin) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() +
                    "/database.db");

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "clan_id INTEGER NOT NULL," +
                        "name TEXT NOT NULL," +
                        "tag TEXT NOT NULL)", "settings"));
            }

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "clan_id TEXT NOT NULL," +
                        "uuid TEXT NOT NULL," +
                        "rank INTEGER NOT NULL)", "players"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка подключения к базе данных");
        }
    }

    public void createParty(UUID uuid, String name) {
        String sql = "INSERT INTO settings (clan_id, name, tag) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int clan_id = getParties().size();
            stmt.setInt(1, clan_id);
            stmt.setString(2, name);
            stmt.setString(3, "not");

            stmt.executeUpdate();

            addMember(clan_id, uuid, 3);
        } catch (SQLException ignored) {}
    }

    public void removeParty(int clan_id) {
        String sql = "DELETE FROM settings WHERE clan_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clan_id);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM players WHERE clan_id = ?")) {
            stmt.setInt(1, clan_id);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void editNameParty(int clan_id, String name) {
        String sql = "UPDATE settings SET name = ? WHERE clan_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(2, clan_id);
            stmt.setString(1, name);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void editTagParty(int clan_id, String tag) {
        String sql = "UPDATE settings SET tag = ? WHERE clan_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(2, clan_id);
            stmt.setString(1, tag);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void editRank(UUID uuid, int rank) {
        String sql = "UPDATE players SET rank = ? WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(2, uuid.toString());
            stmt.setInt(1, rank);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void addMember(int clan_id, UUID uuid, int rank) {
        String sql = "INSERT INTO players (clan_id, uuid, rank) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clan_id);
            stmt.setString(2, uuid.toString());
            stmt.setInt(3, rank);

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void removeMember(int clan_id, UUID uuid) {
        String sql = "DELETE FROM players WHERE clan_id = ? AND uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clan_id);
            stmt.setString(2, uuid.toString());

            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public Member getMember(UUID uuid) {
        String sql = "SELECT * FROM players WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Member(getParty(rs.getInt("clan_id")), UUID.fromString(rs.getString("uuid")), rs.getInt(
                        "rank"));
            }
        } catch (SQLException ignored) {
        }

        return null;
    }

    public Party getParty(int clan_id) {
        String sql = "SELECT * FROM settings WHERE clan_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clan_id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Party(rs.getInt("clan_id"), rs.getString("tag"), rs.getString("name"));
            }
        } catch (SQLException ignored) {}

        return null;
    }

    public List<Member> getMembers(int clan_id) {
        String sql = "SELECT * FROM players WHERE clan_id = ?";
        List<Member> members = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clan_id);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(new Member(getParty(rs.getInt("clan_id")), UUID.fromString(rs.getString("uuid")), rs.getInt("rank")));
            }
        } catch (SQLException ignored) {
        }

        return members;
    }

    public List<Party> getParties() {
        String sql = "SELECT * FROM settings";
        List<Party> parties = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                parties.add(new Party(rs.getInt("clan_id"), rs.getString("tag"), rs.getString("name")));
            }
        } catch (SQLException ignored) {
        }

        return parties;
    }
}