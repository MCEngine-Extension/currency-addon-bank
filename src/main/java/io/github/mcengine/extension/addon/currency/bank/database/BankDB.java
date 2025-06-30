package io.github.mcengine.extension.addon.currency.bank.database;

import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for initializing database tables related to the MCEngine Bank add-on.
 */
public class BankDB {

    /**
     * Creates the required database tables for the bank system if they do not already exist.
     * <p>
     * This includes:
     * <ul>
     *     <li><b>currency_bank</b> — Stores player balances and interest metadata.</li>
     *     <li><b>currency_bank_history</b> — Logs deposits and withdrawals, including coin type and change type.</li>
     * </ul>
     *
     * @param conn   The active SQL {@link Connection} used for executing table creation statements.
     * @param logger The logger used to report success or failure during execution.
     */
    public static void createDBTable(Connection conn, MCEngineAddOnLogger logger) {
        String sql1 = "CREATE TABLE IF NOT EXISTS currency_bank (" +
                "bank_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid VARCHAR(36) NOT NULL, " +
                "balance DOUBLE DEFAULT 0.0, " +
                "interest_rate DOUBLE DEFAULT 0.0, " +
                "last_interest_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
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
}
