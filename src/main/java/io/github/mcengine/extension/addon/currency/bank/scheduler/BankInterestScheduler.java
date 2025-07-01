package io.github.mcengine.extension.addon.currency.bank.scheduler;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import io.github.mcengine.api.core.extension.addon.MCEngineAddOnLogger;
import io.github.mcengine.common.currency.MCEngineCurrencyCommon;
import io.github.mcengine.extension.addon.currency.bank.database.BankDB;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Handles asynchronous, cron-based scheduled interest payouts for MCEngineBank.
 * Loads configs recursively from disk but keeps them out of memory until execution.
 */
public class BankInterestScheduler {

    /**
     * Path to the directory containing interest configuration files.
     */
    private static final String CONFIG_PATH = "configs/addons/MCEngineBank/";

    /**
     * The cron parser used to interpret schedule expressions.
     */
    private static final CronParser PARSER = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(com.cronutils.model.CronType.UNIX));

    /**
     * The YAML parser for loading configuration files.
     */
    private static final Yaml yaml = new Yaml();

    /**
     * Constructs the scheduler for interest payouts.
     *
     * @param plugin the plugin instance
     * @param logger the logger instance
     */
    public BankInterestScheduler(Plugin plugin, MCEngineAddOnLogger logger) {
        loadAndScheduleAll(plugin, logger);
    }

    /**
     * Loads all interest configuration files and schedules them as asynchronous cron tasks.
     */
    public void loadAndScheduleAll(Plugin plugin, MCEngineAddOnLogger logger) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File baseDir = new File(plugin.getDataFolder(), CONFIG_PATH);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
                logger.info("Created config directory for interest configs.");
                return;
            }

            List<File> configFiles = new ArrayList<>();
            findYamlFiles(baseDir, configFiles);

            for (File file : configFiles) {
                scheduleTaskForFile(plugin, logger, file);
            }
        });
    }

    /**
     * Recursively scans a directory for YAML configuration files.
     *
     * @param dir   the root directory
     * @param found the list to store results
     */
    private void findYamlFiles(File dir, List<File> found) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                findYamlFiles(f, found);
            } else if (f.getName().endsWith(".yml") || f.getName().endsWith(".yaml")) {
                found.add(f);
            }
        }
    }

    /**
     * Schedules a repeating interest task based on cron for a single config file.
     *
     * @param file the YAML configuration file
     */
    private void scheduleTaskForFile(Plugin plugin, MCEngineAddOnLogger logger, File file) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                Map<String, Object> root = yaml.load(fis);
                String cronExpr = (String) root.get("schedule");

                if (cronExpr == null || cronExpr.trim().isEmpty()) {
                    logger.warning("Missing schedule in: " + file.getName());
                    return;
                }

                long delay = getInitialDelayMillis(cronExpr);
                long period = getFixedPeriodMillis(cronExpr);

                Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> runInterestTask(logger, file), delay / 50L, period / 50L);
                logger.info("Scheduled interest for " + file.getName());

            } catch (Exception e) {
                logger.warning("Failed to parse interest file: " + file.getName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Executes a scheduled interest payout by reading the config and applying changes.
     *
     * @param file the YAML config file
     */
    private void runInterestTask(MCEngineAddOnLogger logger, File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            Map<String, Object> config = yaml.load(fis);
            Map<String, Map<String, Object>> interestMap = (Map<String, Map<String, Object>>) config.get("interest");

            Map<String, Map<String, Object>> filtered = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : interestMap.entrySet()) {
                String key = entry.getValue().get("coin_type") + "-" + entry.getValue().get("amount");
                filtered.put(key, entry.getValue());
            }

            try (Connection conn = MCEngineCurrencyCommon.getApi().getDBConnection()) {
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    for (Map<String, Object> values : filtered.values()) {
                        String coinType = (String) values.get("coin_type");
                        double base = ((Number) values.get("amount")).doubleValue();
                        double rate = ((Number) values.get("interest_rate")).doubleValue();

                        double interest = base * (rate / 100.0);
                        BankDB.deposit(conn, player, coinType, interest);
                        logger.info("Applied " + interest + " " + coinType + " interest to " + player.getName());
                    }
                }
            }

        } catch (Exception e) {
            logger.warning("Failed to run interest task for: " + file.getName());
            e.printStackTrace();
        }
    }

    /**
     * Calculates delay until the next cron execution.
     *
     * @param cronExpression the cron expression
     * @return delay in milliseconds
     */
    private long getInitialDelayMillis(String cronExpression) {
        Cron cron = PARSER.parse(cronExpression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        return executionTime.timeToNextExecution(ZonedDateTime.now())
                .orElse(Duration.ofMinutes(1)).toMillis();
    }

    /**
     * Provides an approximate fixed execution interval.
     * Currently hardcoded to 1 day, regardless of cron expression.
     *
     * @param cronExpression the cron string
     * @return interval in milliseconds
     */
    private long getFixedPeriodMillis(String cronExpression) {
        return Duration.ofDays(1).toMillis(); // Adjust as needed
    }
}
