package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.Competitor;

/**
 * User: jdev
 * Date: 19.08.11
 */
public class Bot {

    private final String botName;
    private final String botVersion;

    private String alias;
    private Competitor competitor;

    public Bot(String botName, String botVersion) {
        this.botName = botName;
        this.botVersion = botVersion;
    }

    public String getBotName() {
        return botName;
    }

    public String getBotVersion() {
        return botVersion;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public Competitor getCompetitor() {
        return competitor;
    }

    public void setCompetitor(Competitor competitor) {
        this.competitor = competitor;
    }
}
