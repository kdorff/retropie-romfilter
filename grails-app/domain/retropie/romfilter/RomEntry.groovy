package retropie.romfilter

class RomEntry {

    /**
     * The system this rom is for (example 'atari2600').
     */
    String system

    /**
     * Filename within the system's rom directory (no additional path).
     */
    String filename

    /**
     * Size of the rom (on disk).
     */
    long size

    /**
     * Optional. Associated gamelistEntry (rom has been scraped).
     */
    GamelistEntry gamelistEntry

    static constraints = {
        system nullable: false, blank: false, index: 'romentry_system,romentry_system_filename'
        filename nullable: false, blank: false, index: 'romentry_filename'
        gamelistEntry nullable: true
    }
}
