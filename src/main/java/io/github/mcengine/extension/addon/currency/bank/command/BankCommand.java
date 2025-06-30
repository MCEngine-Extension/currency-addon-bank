package io.github.mcengine.extension.addon.currency.bank.command;

import io.github.mcengine.common.currency.MCEngineCurrencyCommon;
import io.github.mcengine.extension.addon.currency.bank.database.BankDB;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;

/**
 * Handles the /bank command and its subcommands for depositing and withdrawing currency.
 * <p>
 * Supported usage:
 * <ul>
 *     <li>/bank deposit &lt;coinType&gt; &lt;amount&gt;</li>
 *     <li>/bank withdraw &lt;coinType&gt; &lt;amount&gt;</li>
 * </ul>
 */
public class BankCommand implements CommandExecutor {

    /**
     * Executes the /bank command. Supports deposit and withdraw operations.
     *
     * @param sender  The command sender (must be a player).
     * @param command The command object.
     * @param label   The command label used.
     * @param args    Command arguments.
     * @return {@code true} if the command executed successfully; otherwise {@code false}.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /bank <deposit|withdraw> <coinType> <amount>");
            return true;
        }

        String action = args[0].toLowerCase();
        String coinType = args[1].toLowerCase();
        double amount;

        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cAmount must be a valid number.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage("§cAmount must be greater than zero.");
            return true;
        }

        Connection conn = MCEngineCurrencyCommon.getApi().getDBConnection();

        switch (action) {
            case "deposit" -> {
                BankDB.deposit(conn, player, coinType, amount);
                return true;
            }
            case "withdraw" -> {
                BankDB.withdraw(conn, player, coinType, amount);
                return true;
            }
            default -> {
                player.sendMessage("§cUnknown bank command. Use deposit or withdraw.");
                return true;
            }
        }
    }
}
