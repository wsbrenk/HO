package core.db;

import core.util.HODateTime;

import java.sql.Timestamp;

public class DownloadInfo {

    private final int hrfId;
    private final HODateTime date;
    private final HODateTime update;

    public DownloadInfo(int hrfId, Timestamp date, Timestamp update) {
        this.hrfId = hrfId;
        this.date = HODateTime.fromDbTimestamp(date);
        this.update = HODateTime.fromDbTimestamp(update);
    }

    public int getHrfId() {
        return hrfId;
    }

    public HODateTime getDate() {
        return date;
    }

    public HODateTime getUpdate() {
        return update;
    }
}
