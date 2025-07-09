package dev.insilicon.cookingX.Skill;

import dev.insilicon.cookingX.Utils.CmdsUtils;
import dev.insilicon.cookingX.Utils.PlayerUtils;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

@NullMarked
public class SkillsCMD implements BasicCommand {

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        if (!(commandSourceStack.getSender() instanceof Player)) {
            commandSourceStack.getSender().sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be a player to use this command!</red>"));
            return;
        }
        Player player = (Player) commandSourceStack.getSender();

        if (args.length == 0) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Use /skills general to view your skill information.</gray>"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "general":
                PlayerSkillStats playerSkillStats = SkillManager.instance.getPlayerData(player);

                if (playerSkillStats == null) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Error: Could not retrieve your skill data.</red>"));
                    return;
                }

                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Your skill information:</green>"));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Cooking level: " + SkillManager.calculateLevel(playerSkillStats.getXp()) + "</yellow>"));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Percent completion: " + SkillManager.calculatePercentCompletetion(playerSkillStats.getXp()) + "%</yellow>"));
                break;
            case "admin":
                if (!player.hasPermission("cookingx.admin")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission to use this command.</red>"));
                    return;
                }
                if (args.length < 4) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /skills admin <set|add> <player> <amount></red>"));
                    return;
                }
                String action = args[1].toLowerCase();
                OfflinePlayer targetPlayer = PlayerUtils.getOfflineplayerByName(args[2]);
                if (targetPlayer == null) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found: " + args[2] + "</red>"));
                    return;
                }
                SkillManager.instance.checkPlayerStatus(targetPlayer);
                double amount;
                try {
                    amount = Double.parseDouble(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid amount: " + args[3] + "</red>"));
                    return;
                }

                switch (action) {
                    case "set":
                        if (amount < 0) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Amount cannot be negative.</red>"));
                            return;
                        }
                        PlayerSkillStats stats = SkillManager.instance.getPlayerData(targetPlayer);
                        stats.setXp(amount);
                        SkillManager.instance.writePlayerSkillStats(targetPlayer, stats);
                        break;
                    case "add":
                        if (amount < 0) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Amount cannot be negative.</red>"));
                            return;
                        }
                        PlayerSkillStats addStats = SkillManager.instance.getPlayerData(targetPlayer);
                        addStats.setXp(addStats.getXp() + amount);
                        SkillManager.instance.writePlayerSkillStats(targetPlayer, addStats);
                        break;
                }

                break;
        }

    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {

        CmdsUtils.CompletionNode completions = CmdsUtils.completions()
                .menu("admin", CmdsUtils.node()
                        .add("set")
                            .addDynamic(CmdsUtils.getOnlinePlayerNames())
                        .add("add")
                            .addDynamic(CmdsUtils.getOnlinePlayerNames())
                )
                .menu("general", CmdsUtils.node()).build();
        return CmdsUtils.generateCompletions(commandSourceStack,args,completions);

    }
}
