package io.github.mcengine.extension.addon.currency.bank.command;

import io.github.mcengine.common.currency.MCEngineCurrencyCommon;
import io.github.mcengine.extension.addon.currency.bank.database.BankDB;
import io.github.mcengine.extension.addon.currency.bank.util.BankCommandUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;

/**
 * Handles the /bank command and its subcommands for depositing, withdrawing, and checking balances.
 * <p>
 * Supported usage:
 * <ul>
 *     <li>/bank deposit &lt;coinType&gt; &lt;amount&gt;</li>
 *     <li>/bank withdraw &lt;coinType&gt; &lt;amount&gt;</li>
 *     <li>/bank balance &lt;coinType&gt;</li>
 * </ul>
 */
public class BankCommand implements CommandExecutor {

    /**
     * Executes the /bank command. Supports deposit, withdraw, and balance query operations.
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

        if (args.length < 2) {
            player.sendMessage("§cUsage: /bank <deposit|withdraw|balance> <coinType> [amount]");
            return true;
        }

        String action = args[0].toLowerCase();
        String coinType = args[1].toLowerCase();
        Connection conn = MCEngineCurrencyCommon.getApi().getDBConnection();

        switch (action) {
            case "deposit" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /bank deposit <coinType> <amount>");
                    return true;
                }

                double amount = BankCommandUtil.parseAmount(args[2], player);
                if (amount <= 0) return true;

                double walletBalance = MCEngineCurrencyCommon.getApi().getCoin(player.getUniqueId(), coinType);
                if (walletBalance < amount) {
                    player.sendMessage("§cYou do not have enough " + coinType + " in your wallet.");
                    return true;
                }

                BankDB.deposit(conn, player, coinType, amount);
            }

            case "withdraw" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /bank withdraw <coinType> <amount>");
                    return true;
                }

                double amount = BankCommandUtil.parseAmount(args[2], player);
                if (amount <= 0) return true;

                double bankBalance = BankDB.getBankBalance(conn, player, coinType);
                if (bankBalance < amount) {
                    player.sendMessage("§cYou do not have enough " + coinType + " in your bank.");
                    return true;
                }

                BankDB.withdraw(conn, player, coinType, amount);
            }

            case "balance" -> {
                double bankBalance = BankDB.getBankBalance(conn, player, coinType);
                player.sendMessage("§aYour bank balance for §e" + coinType + "§a is: §e" + bankBalance);
            }

            default -> player.sendMessage("§cUnknown bank subcommand. Use deposit, withdraw, or balance.");
        }

        return true;
    }
}
