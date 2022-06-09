package eu.domibus.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;

public class DomibusLoggerContextListener implements LoggerContextListener {

    @Override
    public boolean isResetResistant() {
        return false;
    }

    @Override
    public void onStart(LoggerContext loggerContext) {
        System.out.println("onStart " + loggerContext);
    }

    @Override
    public void onReset(LoggerContext loggerContext) {
        System.out.println("onReset " + loggerContext);
    }

    @Override
    public void onStop(LoggerContext loggerContext) {
        System.out.println("onStop " + loggerContext);
    }

    @Override
    public void onLevelChange(Logger logger, Level level) {
        System.out.println("onStop " + logger + " / " + level);
    }
}
