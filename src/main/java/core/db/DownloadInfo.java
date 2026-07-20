package core.db;

import core.util.HODateTime;

import java.sql.Timestamp;

/**
 * Information of the hattrick downloads
 */
public class DownloadInfo {

    /**
     * Identifier of the download
     */
    private final int hrfId;
    /**
     * Date of the download
     */
    private final HODateTime date;
    /**
     * Date of the next hattrick's daily update
     */
    private final HODateTime update;

    /**
     * Create a new download info object
     * Database timestamps are converted to HODateTime objects
     * @param hrfId int
     * @param date Timestamp
     * @param update Timestamp
     */
    public DownloadInfo(int hrfId, Timestamp date, Timestamp update) {
        this.hrfId = hrfId;
        this.date = HODateTime.fromDbTimestamp(date);
        this.update = HODateTime.fromDbTimestamp(update);
    }

    /**
     * Get the hrf id
     * @return int
     */
    public int getHrfId() {
        return hrfId;
    }

    /**
     * Get the download date
     * @return HODateTime
     */
    public HODateTime getDate() {
        return date;
    }

    /**
     * Get the next daily update
     * Another download might make sense then
     * @return HODateTime
     */
    public HODateTime getUpdate() {
        return update;
    }
}
