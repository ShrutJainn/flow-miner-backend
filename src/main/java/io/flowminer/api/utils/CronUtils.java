package io.flowminer.api.utils;//package io.flowminer.api.utils;
//
//import org.quartz.CronExpression;
//
//import java.text.ParseException;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.Date;
//
//public class CronUtils {
//
//    public static LocalDateTime getNextExecutionTime(String cron) throws ParseException {
//        CronExpression cronExpression = new CronExpression(cron);
//        Date nextValidTime = cronExpression.getNextValidTimeAfter(new Date());
//        return LocalDateTime.ofInstant(nextValidTime.toInstant(), ZoneId.systemDefault());
//    }
//}

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.cronutils.model.time.ExecutionTime;

import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.Optional;

public class CronUtils {

    public static LocalDateTime getNextExecutionTime(String cron) {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX); // 5-field
        CronParser parser = new CronParser(cronDefinition);
        Cron parsedCron = parser.parse(cron);
        parsedCron.validate();

        ExecutionTime executionTime = ExecutionTime.forCron(parsedCron);
        Optional<ZonedDateTime> next = executionTime.nextExecution(ZonedDateTime.now());

        return next.map(ZonedDateTime::toLocalDateTime)
                .orElseThrow(() -> new RuntimeException("Could not calculate next execution"));
    }
}
