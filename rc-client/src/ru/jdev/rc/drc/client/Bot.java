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
    private byte[] code;

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

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bot bot = (Bot) o;

        if (botName != null ? !botName.equals(bot.botName) : bot.botName != null) return false;
        if (botVersion != null ? !botVersion.equals(bot.botVersion) : bot.botVersion != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = botName != null ? botName.hashCode() : 0;
        result = 31 * result + (botVersion != null ? botVersion.hashCode() : 0);
        return result;
    }
}
