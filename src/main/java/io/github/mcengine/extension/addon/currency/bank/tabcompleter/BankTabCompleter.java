package io.github.mcengine.extension.addon.currency.bank.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tab completer for the /bank command.
 * Provides auto-completion for subcommands and coin types.
 */
public class BankTabCompleter implements TabCompleter {

    /**
     * List of supported subcommands for the /bank command.
     * Includes actions players can perform such as depositing or withdrawing.
     */
    private final List<String> subCommands = List.of("deposit", "withdraw", "balance");

    /**
     * List of supported coin types used in the bank system.
     */
    private final List<String> coinTypes = List.of("coin", "copper", "silver", "gold");

    /**
     * Provides tab completion suggestions for the /bank command.
     *
     * @param sender  The sender of the command.
     * @param command The command being executed.
     * @param label   The command alias used.
     * @param args    The arguments provided by the player so far.
     * @return A list of matching suggestions for the current argument position.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // Suggest subcommands
            List<String> suggestions = new ArrayList<>();
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    suggestions.add(sub);
                }
            }
            return suggestions;
        }

        if (args.length == 2 && subCommands.contains(args[0].toLowerCase())) {
            // Suggest coin types for known subcommands
            List<String> suggestions = new ArrayList<>();
            for (String coin : coinTypes) {
                if (coin.startsWith(args[1].toLowerCase())) {
                    suggestions.add(coin);
                }
            }
            return suggestions;
        }

        return Collections.emptyList();
    }
}
