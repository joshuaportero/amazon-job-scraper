package me.joshuaportero.ajs.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.LevelConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class LevelColors extends LevelConverter {
    @Override
    public String convert(ILoggingEvent event) {
        if (event.getLevel().equals(Level.ERROR)) {
            return "\033[31mERROR\033[0m";
        } else if (event.getLevel().equals(Level.WARN)) {
            return "\033[33mWARN\033[0m ";
        } else if (event.getLevel().equals(Level.INFO)) {
            return "\033[32mINFO\033[0m ";
        } else if (event.getLevel().equals(Level.DEBUG)) {
            return "\033[34mDEBUG\033[0m";
        } else if (event.getLevel().equals(Level.TRACE)) {
            return "\033[36mTRACE\033[0m";
        } else {
            return event.getLevel().toString();
        }
    }
}
