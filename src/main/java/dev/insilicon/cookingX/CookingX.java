package dev.insilicon.cookingX;

import dev.insilicon.cookingX.Skill.SkillManager;
import dev.insilicon.cookingX.Skill.SkillsCMD;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class CookingX extends JavaPlugin {
    public static CookingX instance;
    public static Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        // init config

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();

        // init Database Manager
        new DatabaseManager();

    }

    public void startSystems() {
        logger.info("DB fully initalized.");

        // Skill System
        new SkillManager();


        registerCommands();
    }

    public void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> event.registrar().register("skills", new SkillsCMD()));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public void shutDownPlugin() {
        if (instance != null) {
            instance.getLogger().info("Shutting down CookingX plugin...");
            instance.getServer().getPluginManager().disablePlugin(instance);
            instance = null;
        } else {
            getLogger().warning("CookingX plugin is not currently running.");
        }
    }
}
