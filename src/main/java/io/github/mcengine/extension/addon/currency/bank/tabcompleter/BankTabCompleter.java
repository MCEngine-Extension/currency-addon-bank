package io.github.mcengine.extension.addon.currency.bank.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tab completer for the /bank command.
 * Provides suggestions for subcommands and coin types.
 */
public class BankTabCompleter implements TabCompleter {

    /**
     * Supported subcommands for /bank.
     */
    private final List<String> subCommands = List.of("deposit", "withdraw");

    /**
     * Supported coin types for the bank system.
     */
    private final List<String> coinTypes = List.of("coin", "copper", "silver", "gold");

    /**
     * Handles tab completion for the /bank command.
     *
     * @param sender  The sender of the command.
     * @param command The command that was executed.
     * @param label   The alias of the command used.
     * @param args    The arguments entered by the user.
     * @return A list of suggestions based on the input.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    suggestions.add(sub);
                }
            }
            return suggestions;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("withdraw"))) {
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
