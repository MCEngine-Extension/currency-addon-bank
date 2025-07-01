package io.github.mcengine.extension.addon.currency.bank.util;

import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class for generating interest configuration files used by the bank system.
 * The configuration includes interest rates, coin types, and cron-style schedules.
 */
public class InterestConfigGenerator {

    /**
     * Generates the interest configuration file if it does not exist.
     * Includes cron-style schedule, interest rates, and coin types.
     *
     * @param plugin The plugin instance to resolve data folder path.
     * @param logger Logger used for error reporting and file creation info.
     */
    public static void createInterestConfigIfAbsent(Plugin plugin, MCEngineAddOnLogger logger) {
        String CONFIG_PATH = "configs/addons/MCEngineBank/example.yml";
        File file = new File(plugin.getDataFolder(), CONFIG_PATH);

        if (file.exists()) {
            logger.info("Interest config already exists at " + file.getPath());
            return;
        }

        File parentDir = file.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            logger.warning("Failed to create config folder: " + parentDir.getPath());
            return;
        }

        String header = "# Interest Configuration\n" +
                "# -----------------------\n" +
                "# This file defines interest payout rules.\n" +
                "#\n" +
                "# Schedule supports cron syntax:\n" +
                "# minute hour day_of_month month day_of_week\n" +
                "# Example: '0 0 * * *' means daily at midnight.\n" +
                "# Website for creating a schedule: https://www.freeformatter.com/cron-expression-generator-quartz.html" +
                "#\n" +
                "# Example Structure:\n" +
                "# interest:\n" +
                "#   1:\n" +
                "#     amount: 100000\n" +
                "#     coin_type: coin\n" +
                "#     interest_rate: 2\n" +
                "# schedule: '0 0 * * *'\n\n";

        String content = header +
                "interest:\n" +
                "  1:\n" +
                "    amount: 100000\n" +
                "    coin_type: coin\n" +
                "    interest_rate: 2\n" +
                "  2:\n" +
                "    amount: 50000\n" +
                "    coin_type: silver\n" +
                "    interest_rate: 1.5\n\n" +
                "schedule: '0 0 * * *'\n";

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            logger.info("Created interest config at: " + file.getPath());
        } catch (IOException e) {
            logger.warning("Failed to write interest config: " + e.getMessage());
        }
    }
}
