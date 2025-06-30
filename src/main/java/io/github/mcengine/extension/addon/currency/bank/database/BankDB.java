package io.github.mcengine.extension.addon.currency.bank.database;

import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles database interactions for the MCEngine Bank add-on.
 * Creates necessary tables and provides APIs to insert and manage bank records.
 */
public class BankDB {

    /**
     * The active SQL database connection used for bank operations.
     */
    private final Connection conn;

    /**
     * Logger instance used for reporting database actions and errors.
     */
    private final MCEngineAddOnLogger logger;

    /**
     * Constructs the BankDB instance and initializes the bank database tables.
     *
     * @param conn   The SQL {@link Connection} to be used for database operations.
     * @param logger The logger instance for logging bank-related database activity.
     */
    public BankDB(Connection conn, MCEngineAddOnLogger logger) {
        this.conn = conn;
        this.logger = logger;
        createDBTable();
    }

    /**
     * Creates the bank tables if they don't already exist.
     * - `currency_bank`: Main table for player bank balances and interest tracking.
     * - `currency_bank_history`: Archive/history for interest payouts or transactions.
     */
    public void createDBTable() {
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
                "change_type TEXT, " +
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
     * Inserts a bank entry for a player.
     *
     * @param uuid         Player UUID.
     * @param balance      Initial balance.
     * @param interestRate Interest rate to apply.
     */
    public void insertBankEntry(String uuid, double balance, double interestRate) {
        String sql = "INSERT INTO currency_bank (uuid, balance, interest_rate, last_interest_time) " +
                     "VALUES (?, ?, ?, CURRENT_TIMESTAMP);";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setDouble(2, balance);
            stmt.setDouble(3, interestRate);
            stmt.executeUpdate();
            logger.info("Bank entry created for UUID: " + uuid);
        } catch (SQLException e) {
            logger.warning("Failed to insert bank entry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Records a transaction or interest event in the bank history table.
     *
     * @param uuid        Player UUID.
     * @param amount      Amount added or deducted.
     * @param changeType  Type of change (e.g., "interest", "deposit", "withdraw").
     * @param note        Optional note about the change.
     */
    public void insertBankHistory(String uuid, double amount, String changeType, String note) {
        String sql = "INSERT INTO currency_bank_history (uuid, change_amount, change_type, note, created_time) " +
                     "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP);";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setDouble(2, amount);
            stmt.setString(3, changeType);
            stmt.setString(4, note);
            stmt.executeUpdate();
            logger.info("Bank history recorded for UUID: " + uuid + " | Type: " + changeType);
        } catch (SQLException e) {
            logger.warning("Failed to insert bank history: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
