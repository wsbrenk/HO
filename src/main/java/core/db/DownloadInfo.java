package core.db;

import core.util.HODateTime;

/**
 * Information of the hattrick downloads
 *
 * @param hrfId  Identifier of the download
 * @param date   Date of the download
 * @param update Date of the next hattrick's daily update
 */
public record DownloadInfo(int hrfId, HODateTime date, HODateTime update) {

    /**
     * Create a new download info object
     * Database timestamps are converted to HODateTime objects
     *
     * @param hrfId  int
     * @param date   HODateTime
     * @param update HODateTime
     */
    public DownloadInfo {
    }

    /**
     * Get the hrf id
     *
     * @return int
     */
    @Override
    public int hrfId() {
        return hrfId;
    }

    /**
     * Get the download date
     *
     * @return HODateTime
     */
    @Override
    public HODateTime date() {
        return date;
    }

    /**
     * Get the next daily update
     * Another download might make sense then
     *
     * @return HODateTime
     */
    @Override
    public HODateTime update() {
        return update;
    }
}
