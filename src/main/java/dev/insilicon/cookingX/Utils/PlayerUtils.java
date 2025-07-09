package dev.insilicon.cookingX.Utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public class PlayerUtils {

    public static OfflinePlayer getOfflineplayerbyUUID(UUID uuid) {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.getUniqueId().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public static OfflinePlayer getOfflineplayerByName(String name) {
        for (OfflinePlayer player : org.bukkit.Bukkit.getOfflinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

}
