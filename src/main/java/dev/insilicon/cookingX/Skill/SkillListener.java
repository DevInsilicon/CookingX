package dev.insilicon.cookingX.Skill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SkillListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SkillManager.instance.checkPlayerStatus(event.getPlayer());
    }

}
