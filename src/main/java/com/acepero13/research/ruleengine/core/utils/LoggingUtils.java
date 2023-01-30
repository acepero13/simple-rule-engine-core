package com.acepero13.research.ruleengine.core.utils;

import org.apache.logging.log4j.Logger;

/**
 * A Description
 *
 * @author Alvaro Cepero
 */
public class LoggingUtils {
    public static <T> void logIterable(Iterable<T> items, Logger logger) {
        for (T item : items) {
            logger.info(item);
        }
    }
}
