package dev.insilicon.cookingX.Utils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.*;

public class CmdsUtils {

    /**
     * Represents a completion node that can have sub-completions
     */
    public static class CompletionNode {
        private final Map<String, CompletionNode> children = new HashMap<>();
        private final List<String> dynamicCompletions = new ArrayList<>();
        private boolean allowDynamic = false;

        public CompletionNode() {}

        // Add a static completion option
        public CompletionNode add(String key, CompletionNode node) {
            children.put(key, node);
            return this;
        }

        // Add a static completion option with empty node
        public CompletionNode add(String key) {
            children.put(key, new CompletionNode());
            return this;
        }

        // Add dynamic completions (like player names, numbers, etc.)
        public CompletionNode addDynamic(String... completions) {
            this.dynamicCompletions.addAll(Arrays.asList(completions));
            this.allowDynamic = true;
            return this;
        }

        // Add dynamic completions from a list
        public CompletionNode addDynamic(Collection<String> completions) {
            this.dynamicCompletions.addAll(completions);
            this.allowDynamic = true;
            return this;
        }

        // Get child node
        public CompletionNode get(String key) {
            return children.get(key);
        }

        // Get all possible completions at this level
        public List<String> getCompletions() {
            List<String> completions = new ArrayList<>(children.keySet());
            completions.addAll(dynamicCompletions);
            return completions;
        }

        // Check if this node has children
        public boolean hasChildren() {
            return !children.isEmpty() || allowDynamic;
        }
    }

    /**
     * Builder class for creating completion structures with JavaScript-like syntax
     */
    public static class CompletionBuilder {
        private final CompletionNode root = new CompletionNode();

        // JavaScript-like object creation
        public CompletionBuilder menu(String name, CompletionNode node) {
            root.add(name, node);
            return this;
        }

        public CompletionBuilder menu(String name) {
            root.add(name);
            return this;
        }

        public CompletionNode build() {
            return root;
        }
    }

    /**
     * Create a new completion structure
     */
    public static CompletionBuilder completions() {
        return new CompletionBuilder();
    }

    /**
     * Create a new completion node
     */
    public static CompletionNode node() {
        return new CompletionNode();
    }

    /**
     * Generate tab completions based on the completion structure and current args
     */
    public static Collection<String> generateCompletions(CommandSourceStack source, String[] args, CompletionNode completionStructure) {
        if (completionStructure == null) {
            return Collections.emptyList();
        }

        CompletionNode currentNode = completionStructure;

        // Navigate through the completion tree based on current args
        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i];
            CompletionNode nextNode = currentNode.get(arg);

            if (nextNode != null) {
                currentNode = nextNode;
            } else {
                // If we can't find the exact match, check if current node allows dynamic input
                if (currentNode.allowDynamic) {
                    // Continue to next level if this was a dynamic completion
                    continue;
                } else {
                    // Dead end, no completions available
                    return Collections.emptyList();
                }
            }
        }

        // Get completions for the current argument being typed
        List<String> allCompletions = currentNode.getCompletions();

        // Filter completions based on what the user has typed so far
        String currentArg = args.length > 0 ? args[args.length - 1] : "";
        return allCompletions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentArg.toLowerCase()))
                .sorted()
                .toList();
    }

    /**
     * Utility method to get online player names for dynamic completions
     */
    public static List<String> getOnlinePlayerNames() {
        return org.bukkit.Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
    }

    /**
     * Utility method to get number range as strings
     */
    public static List<String> getNumberRange(int min, int max) {
        List<String> numbers = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            numbers.add(String.valueOf(i));
        }
        return numbers;
    }

    /**
     * Check if sender is a player
     */
    public static boolean isPlayer(CommandSourceStack source) {
        return source.getSender() instanceof Player;
    }

    /**
     * Get player from command source (returns null if not a player)
     */
    public static Player getPlayer(CommandSourceStack source) {
        return isPlayer(source) ? (Player) source.getSender() : null;
    }
}