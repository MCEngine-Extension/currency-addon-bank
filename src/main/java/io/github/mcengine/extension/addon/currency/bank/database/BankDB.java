package io.github.mcengine.extension.addon.currency.bank.database;

import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;
import io.github.mcengine.common.currency.MCEngineCurrencyCommon;
import org.bukkit.OfflinePlayer;

import java.sql.*;

/**
 * Utility class for initializing and interacting with the MCEngine Bank database system.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Create required bank and history tables</li>
 *     <li>Deposit and withdraw currency for both online and offline players</li>
 *     <li>Fetch bank balances per coin type</li>
 * </ul>
 */
public class BankDB {

    /**
     * Creates the required database tables for the bank system if they do not already exist.
     * <p>
     * Tables:
     * <ul>
     *     <li><b>currency_bank</b> — Stores player balances per coin type and interest metadata.</li>
     *     <li><b>currency_bank_history</b> — Logs deposits and withdrawals with coin and change type.</li>
     * </ul>
     *
     * @param conn   The SQL {@link Connection} used for executing table creation statements.
     * @param logger The logger used to report success or failure during execution.
     */
    public static void createDBTable(Connection conn, MCEngineAddOnLogger logger) {
        String sql1 = "CREATE TABLE IF NOT EXISTS currency_bank (" +
                "bank_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid VARCHAR(36) NOT NULL, " +
                "coin_type TEXT CHECK(coin_type IN ('coin', 'copper', 'silver', 'gold')) NOT NULL, " +
                "balance DOUBLE DEFAULT 0.0, " +
                "interest_rate DOUBLE DEFAULT 0.0, " +
                "last_interest_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(uuid, coin_type)" +
                ");";

        String sql2 = "CREATE TABLE IF NOT EXISTS currency_bank_history (" +
                "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid VARCHAR(36), " +
                "change_amount DOUBLE, " +
                "change_type TEXT CHECK(change_type IN ('deposit', 'withdraw')) NOT NULL, " +
                "coin_type TEXT CHECK(coin_type IN ('coin', 'copper', 'silver', 'gold')) NOT NULL, " +
                "note TEXT, " +
                "created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql1);
            statement.executeUpdate(sql2);
            logger.info("Bank and bank history tables created or already exist.");
        } catch (SQLException e) {
            logger.warning("Failed to create bank tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deposits a specified amount of currency to a player's bank account for the given coin type.
     *
     * @param conn     The database connection.
     * @param player   The player (online or offline).
     * @param coinType The type of coin to deposit (e.g., "coin", "silver").
     * @param amount   The amount to deposit.
     */
    public static void deposit(Connection conn, OfflinePlayer player, String coinType, double amount) {
        String uuid = player.getUniqueId().toString();
        MCEngineCurrencyCommon.getApi().minusCoin(player.getUniqueId(), coinType, amount);

        try {
            boolean exists;
            String checkSql = "SELECT 1 FROM currency_bank WHERE uuid = ? AND coin_type = ? LIMIT 1;";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setString(1, uuid);
                stmt.setString(2, coinType);
                try (ResultSet rs = stmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE currency_bank SET balance = balance + ? WHERE uuid = ? AND coin_type = ?;")) {
                    update.setDouble(1, amount);
                    update.setString(2, uuid);
                    update.setString(3, coinType);
                    update.executeUpdate();
                }
            } else {
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO currency_bank (uuid, coin_type, balance) VALUES (?, ?, ?);")) {
                    insert.setString(1, uuid);
                    insert.setString(2, coinType);
                    insert.setDouble(3, amount);
                    insert.executeUpdate();
                }
            }

            try (PreparedStatement log = conn.prepareStatement(
                    "INSERT INTO currency_bank_history (uuid, change_amount, change_type, coin_type, note) " +
                            "VALUES (?, ?, 'deposit', ?, 'System/Interest/Deposit');")) {
                log.setString(1, uuid);
                log.setDouble(2, amount);
                log.setString(3, coinType);
                log.executeUpdate();
            }

            if (player.isOnline()) {
                player.getPlayer().sendMessage("§aDeposited " + amount + " " + coinType + " into your bank.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (player.isOnline()) {
                player.getPlayer().sendMessage("§cError occurred while depositing funds.");
            }
        }
    }

    /**
     * Withdraws a specified amount of currency from a player's bank account to their wallet.
     *
     * @param conn     The database connection.
     * @param player   The player (must be online).
     * @param coinType The type of coin to withdraw.
     * @param amount   The amount to withdraw.
     */
    public static void withdraw(Connection conn, OfflinePlayer player, String coinType, double amount) {
        if (!player.isOnline() || player.getPlayer() == null) return;

        String uuid = player.getUniqueId().toString();

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT balance FROM currency_bank WHERE uuid = ? AND coin_type = ?;")) {
            stmt.setString(1, uuid);
            stmt.setString(2, coinType);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    player.getPlayer().sendMessage("§cYou do not have a bank account.");
                    return;
                }

                double balance = rs.getDouble("balance");
                if (balance < amount) {
                    player.getPlayer().sendMessage("§cYou do not have enough funds in your bank.");
                    return;
                }

                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE currency_bank SET balance = balance - ? WHERE uuid = ? AND coin_type = ?;")) {
                    update.setDouble(1, amount);
                    update.setString(2, uuid);
                    update.setString(3, coinType);
                    update.executeUpdate();
                }

                try (PreparedStatement log = conn.prepareStatement(
                        "INSERT INTO currency_bank_history (uuid, change_amount, change_type, coin_type, note) " +
                                "VALUES (?, ?, 'withdraw', ?, 'Player withdrawal');")) {
                    log.setString(1, uuid);
                    log.setDouble(2, amount);
                    log.setString(3, coinType);
                    log.executeUpdate();
                }

                MCEngineCurrencyCommon.getApi().addCoin(player.getUniqueId(), coinType, amount);
                player.getPlayer().sendMessage("§aWithdrew " + amount + " " + coinType + " from your bank.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.getPlayer().sendMessage("§cError occurred while withdrawing funds.");
        }
    }

    /**
     * Retrieves the current bank balance for the given player and coin type.
     *
     * @param conn     The SQL connection.
     * @param player   The player (online or offline).
     * @param coinType The coin type being queried.
     * @return The balance as a double, or 0.0 if not found or on error.
     */
    public static double getBankBalance(Connection conn, OfflinePlayer player, String coinType) {
        String uuid = player.getUniqueId().toString();
        String query = "SELECT balance FROM currency_bank WHERE uuid = ? AND coin_type = ?;";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid);
            stmt.setString(2, coinType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (Exception e) {
            if (player.isOnline()) {
                player.getPlayer().sendMessage("§cFailed to fetch bank balance.");
            }
            e.printStackTrace();
        }

        return 0.0;
    }
}
