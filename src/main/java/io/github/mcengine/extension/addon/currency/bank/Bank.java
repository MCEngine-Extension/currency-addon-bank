package io.github.mcengine.extension.addon.currency.bank;

import io.github.mcengine.api.currency.extension.addon.IMCEngineCurrencyAddOn;
import io.github.mcengine.api.mcengine.MCEngineApi;
import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;

import org.bukkit.plugin.Plugin;

/**
 * Main class for the MCEngineBank add-on.
 * <p>
 * This add-on integrates with the Currency plugin to provide bank-related features
 * such as interest accrual, deposit handling, and bank data integration.
 * </p>
 *
 * <p>
 * When the add-on is loaded, it performs the following tasks:
 * <ul>
 *     <li>Initializes an add-on-specific logger.</li>
 *     <li>Checks for updates using MCEngine's GitHub integration.</li>
 * </ul>
 * </p>
 */
public class Bank implements IMCEngineCurrencyAddOn {

    /**
     * Called when the add-on is loaded by the MCEngine framework.
     * Initializes the logger and checks for available updates.
     *
     * @param plugin The Bukkit plugin instance that owns this add-on.
     */
    @Override
    public void onLoad(Plugin plugin) {
        MCEngineAddOnLogger logger = new MCEngineAddOnLogger(plugin, "MCEngineBank");

        // Check for plugin updates using GitHub token (if provided in config)
        MCEngineApi.checkUpdate(
            plugin,
            logger.getLogger(),
            "[AddOn] [MCEngineBank] ",
            "github",
            "MCEngine-Extension",
            "currency-addon-bank",
            plugin.getConfig().getString("github.token", "null")
        );
    }
}
