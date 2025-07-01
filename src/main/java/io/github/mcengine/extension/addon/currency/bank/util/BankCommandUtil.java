package io.github.mcengine.extension.addon.currency.bank.util;

import io.github.mcengine.api.core.extension.addon.MCEngineAddOnLogger;

import org.bukkit.entity.Player;

/**
 * Utility class for common bank command operations.
 * Provides shared logic for parsing and validating user input.
 */
public class BankCommandUtil {

    public static void check(MCEngineAddOnLogger logger) {
        logger.info("Class: BankCommandUtil is loadded.");
    }

    /**
     * Parses and validates a numeric string input for amount commands.
     *
     * @param arg    The amount argument as a string.
     * @param player The player to notify if input is invalid.
     * @return A valid parsed amount, or -1 if invalid.
     */
    public static double parseAmount(String arg, Player player) {
        try {
            double amount = Double.parseDouble(arg);
            if (amount <= 0) {
                player.sendMessage("§cAmount must be greater than zero.");
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            player.sendMessage("§cAmount must be a valid number.");
            return -1;
        }
    }
}
