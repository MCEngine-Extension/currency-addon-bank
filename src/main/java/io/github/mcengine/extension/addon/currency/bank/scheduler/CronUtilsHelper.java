package io.github.mcengine.extension.addon.currency.bank.scheduler;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Duration;
import java.time.ZonedDateTime;

import static com.cronutils.model.CronType.UNIX;

/**
 * Utility class to parse cron expressions and convert to scheduling delay.
 */
public class CronUtilsHelper {

    /** Shared cron parser instance for UNIX-style expressions. */
    private static final CronParser PARSER = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(UNIX));

    /**
     * Converts a cron expression to a delay in milliseconds until the next execution.
     *
     * @param cronExpression Cron string, e.g., "0 0 * * *"
     * @return Delay in milliseconds
     */
    public static long getDelayForCron(String cronExpression) {
        Cron cron = PARSER.parse(cronExpression);
        ExecutionTime execTime = ExecutionTime.forCron(cron);

        return execTime.timeToNextExecution(ZonedDateTime.now())
                .orElse(Duration.ofMinutes(1))
                .toMillis();
    }
}
