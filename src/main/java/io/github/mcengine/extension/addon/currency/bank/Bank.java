package io.github.mcengine.extension.addon.currency.bank;

import io.github.mcengine.api.currency.extension.addon.IMCEngineCurrencyAddOn;
import io.github.mcengine.api.core.MCEngineApi;
import io.github.mcengine.api.core.extension.addon.MCEngineAddOnLogger;
import io.github.mcengine.common.currency.MCEngineCurrencyCommon;
import io.github.mcengine.extension.addon.currency.bank.command.BankCommand;
import io.github.mcengine.extension.addon.currency.bank.database.BankDB;
import io.github.mcengine.extension.addon.currency.bank.scheduler.BankInterestScheduler;
import io.github.mcengine.extension.addon.currency.bank.tabcompleter.BankTabCompleter;
import io.github.mcengine.extension.addon.currency.bank.util.BankCommandUtil;
import io.github.mcengine.extension.addon.currency.bank.util.InterestConfigGenerator;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

/**
 * Main class for the MCEngineBank add-on.
 * <p>
 * This add-on integrates with the Currency plugin to provide bank-related features
 * such as interest accrual, deposit/withdraw functionality, and database persistence.
 */
public class Bank implements IMCEngineCurrencyAddOn {

    /**
     * Called when the add-on is loaded by the MCEngine framework.
     * Initializes the logger, creates database tables, and registers the /bank command.
     *
     * @param plugin The Bukkit plugin instance that owns this add-on.
     */
    @Override
    public void onLoad(Plugin plugin) {
        MCEngineAddOnLogger logger = new MCEngineAddOnLogger(plugin, "MCEngineBank");

        BankCommandUtil.check(logger);

        InterestConfigGenerator.createInterestConfigIfAbsent(plugin, logger);

        // Create required database tables
        Connection conn = MCEngineCurrencyCommon.getApi().getDBConnection();
        BankDB.createDBTable(conn, logger);

        try {
            // Access command map via reflection
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // Register /bank command with handler and tab completer
            Command bankCommand = new Command("bank") {

                /** Command logic handler */
                private final BankCommand handler = new BankCommand();

                /** Tab completer for /bank */
                private final BankTabCompleter completer = new BankTabCompleter();

                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return handler.onCommand(sender, this, label, args);
                }

                @Override
                public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                    return completer.onTabComplete(sender, this, alias, args);
                }
            };

            bankCommand.setDescription("Manage your virtual bank account.");
            bankCommand.setUsage("/bank <deposit|withdraw> <coinType> <amount>");

            commandMap.register(plugin.getName().toLowerCase(), bankCommand);

            logger.info("Bank command registered.");
        } catch (Exception e) {
            logger.warning("Failed to register bank command: " + e.getMessage());
            e.printStackTrace();
        }

        // Start the cron-based interest scheduler
        new BankInterestScheduler(plugin, logger);

        // Check for plugin updates
        MCEngineApi.checkUpdate(
            plugin,
            logger.getLogger(),
            "github",
            "MCEngine-Extension",
            "currency-addon-bank",
            plugin.getConfig().getString("github.token", "null")
        );
    }
}
