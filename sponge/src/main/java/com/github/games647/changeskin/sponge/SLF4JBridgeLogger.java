package com.github.games647.changeskin.sponge;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SLF4JBridgeLogger extends Logger {

    private final org.slf4j.Logger logger;

    public SLF4JBridgeLogger(org.slf4j.Logger logger) {
        super(logger.getName(), null);

        this.logger = logger;
    }

    @Override
    public void log(LogRecord record) {
        Throwable exception = record.getThrown();
        Level level = record.getLevel();
        String message = record.getMessage();

        if (level == Level.SEVERE) {
            logger.error(message, exception);
        } else if (level == Level.WARNING) {
            logger.warn(message, exception);
        } else if (level == Level.INFO) {
            logger.info(message, exception);
        } else if (level == Level.CONFIG) {
            logger.debug(message, exception);
        } else {
            logger.trace(message, exception);
        }
    }
}
