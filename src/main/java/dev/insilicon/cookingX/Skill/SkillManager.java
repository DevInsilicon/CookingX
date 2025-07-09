package dev.insilicon.cookingX.Skill;

import dev.insilicon.cookingX.CookingX;
import dev.insilicon.cookingX.DatabaseManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;

class PlayerSkillStats {
    private String uuid;
    private double xp;
    private double skillPoints;

    public PlayerSkillStats(String uuid, double xp, double skillPoints) {
        this.uuid = uuid;
        this.xp = xp;
        this.skillPoints = skillPoints;
    }

    public String getUuid() {
        return uuid;
    }

    public double getXp() {
        return xp;
    }

    public double getSkillPoints() {
        return skillPoints;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setXp(double xp) {
        this.xp = xp;
    }

    public void setSkillPoints(double skillPoints) {
        this.skillPoints = skillPoints;
    }
}

public class SkillManager {
    public static SkillManager instance;

    public SkillManager() {

        createPlayercolumn();
        CookingX.instance.getServer().getPluginManager().registerEvents(new SkillListener(), CookingX.instance);


        instance = this;
    }


    public void writePlayerSkillStats(OfflinePlayer player, PlayerSkillStats playerSkillStats) {
        Connection connection = DatabaseManager.instance.getConnection();
        if (connection == null) {
            throw new RuntimeException("Failed to get database connection for writing player skill stats.");
        }

        String sql = """
                INSERT INTO player_skills (uuid, xp, skill_points) VALUES (?, ?, ?)
                """;

        try (var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setDouble(2, playerSkillStats.getXp());
            preparedStatement.setDouble(3, playerSkillStats.getSkillPoints());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write player skill stats", e);
        }

    }

    public PlayerSkillStats getPlayerData(OfflinePlayer player) {
        Connection connection = DatabaseManager.instance.getConnection();
        if (connection == null) {
            throw new RuntimeException("Failed to get database connection for getting player data.");
        }

        String sql = """
                SELECT xp, skill_points FROM player_skills WHERE uuid = ?
                """;

        try (var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String uuid = player.getUniqueId().toString();
                double xp = resultSet.getDouble("xp");
                double skillPoints = resultSet.getDouble("skill_points");
                return new PlayerSkillStats(uuid, xp, skillPoints);
            } else {
                registerPlayer(player);
                return new PlayerSkillStats(player.getUniqueId().toString(), 0, 0);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get player data", e);
        }

    }

    public void checkPlayerStatus(OfflinePlayer player) {

        Connection connection = DatabaseManager.instance.getConnection();
        if (connection == null) {
            throw new RuntimeException("Failed to get database connection for checking player status.");
        }

        String sql = """
                SELECT xp, skill_points FROM player_skills WHERE uuid = ?
                """;
        try (var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // found
            } else {
                // not found
                registerPlayer(player);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check player status", e);
        }

    }

    private static void registerPlayer(OfflinePlayer player) {

        Connection connection = DatabaseManager.instance.getConnection();
        if (connection == null) {
            throw new RuntimeException("Failed to get database connection for registering player.");
        }

        String sql = "INSERT INTO player_skills (uuid, xp, skill_points) VALUES (?, 0, 0)";
        try (var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to register player", e);
        }

    }

    public static double calculateLevel(double xp) {
        // each level requires 1/3 more xp than the previous level
        double level = 0;
        double requiredXp = 100;
        while (xp >= requiredXp) {
            xp -= requiredXp;
            level++;
            requiredXp *= 1.33;
        }
        return level;
    }

    public static double calculateXpForNextLevel(double currentXp) {
        // each level requires 1/3 more xp than the previous level
        double level = calculateLevel(currentXp);
        double requiredXp = 100;
        for (int i = 0; i < level; i++) {
            requiredXp *= 1.33;
        }
        return requiredXp - (currentXp % requiredXp);
    }

    public static double calculatePercentCompletetion(double currentXp) {
        double level = calculateLevel(currentXp);
        double requiredXp = 100;
        for (int i = 0; i < level; i++) {
            requiredXp *= 1.33;
        }
        double nextLevelXp = requiredXp;
        return (currentXp % nextLevelXp) / nextLevelXp * 100;
    }


    //db stuff
    private void createPlayercolumn() {

        // varchar 64 uuid, double xp, double skillPoints
        Connection connection = DatabaseManager.instance.getConnection();
        if (connection == null) {
            throw new RuntimeException("Failed to get database connection for creating player column.");
        }

        try {
            String sql = "CREATE TABLE IF NOT EXISTS player_skills (" +
                    "uuid VARCHAR(64) PRIMARY KEY, " +
                    "xp DOUBLE DEFAULT 0, " +
                    "skill_points DOUBLE DEFAULT 0" +
                    ")";
            connection.createStatement().executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create player_skills table", e);
        }

    }

}
