package retropie.romfilter

import groovy.transform.ToString

/**
 * Entry for one game from gamelist.xml.
 */
@ToString(includeNames = true)
class GamelistEntry {
    /**
     * The system this rom is for (example 'atari2600').
     */
    String system         // not in gamelist.xml, effectively what folder the gamelist.xml file was in

    /**
     * The id within scrapeSource for this game.
     * This may generally be an int, but using a String for flexibility.
     * This value comes from the attribute "game.@id" In gamelist.txt
     */
    String scrapeId       // attrib

    /**
     * The source that of data for this game's scrape.
     * This value comes from the attribute "game.@source" In gamelist.txt
     */
    String scrapeSource   // attrib

    /**
     * The path (filename) to the rom within the system's rom folder (no additional path).
     * The additional pathing that exists in gamelist.xml is removed during parsing.
     */
    String path

    /**
     * Name of the rom.
     */
    String name

    /**
     * Description of the rom.
     */
    String desc

    /**
     * The full local filesystem path to the image.
     * This path is transformed to a local path during parsing.
     */
    String image

    /**
     * The full local filesystem path to the thumbnail.
     * This path is transformed to a local path during parsing.
     */
    String thumbnail

    /**
     * Developer of the rom.
     */
    String developer

    /**
     * Publisher of the rom.
     */
    String publisher

    /**
     * Genre of the rom.
     */
    String genre

    /**
     * Number of players supported by the rom.
     * If no value is present in gamelist.xml for this entry, this will default to 1.
     */
    int players

    /**
     * Region of the rom.
     */
    String region

    /**
     * Type of rom.
     */
    String romtype

    /**
     * The date the rom was released.
     */
    String releasedate

    /**
     * The rating of the rom..
     * If no value is present in gamelist.xml for this entry, this will default to 0.
     */
    double rating

    /**
     * The number of times the rom has been played.
     * If no value is present in gamelist.xml for this entry, this will default to 0.
     */
    int playcount

    /**
     * The date the rom was last played.
     */
    String lastplayed

    boolean hasThumbnail() {
        return thumbnail != null && thumbnail != ""
    }

    boolean hasImage() {
        return image != null && image != ""
    }

    /**
     * Constraints.
     */
    static constraints = {
        // Required
        id index: 'system_id_index'
        system nullable: false, blank: false, index: 'system_index,system_id_index'
        path nullable: false, blank: false
        name nullable: false, blank: false
        // Optional
        scrapeId nullable: true, blank: true
        scrapeSource nullable: true, blank: true
        desc nullable: true, blank: true
        image nullable: true, blank: true
        thumbnail nullable: true, blank: true
        developer nullable: true, blank: true
        publisher nullable: true, blank: true
        genre nullable: true, blank: true
        region nullable: true, blank: true
        romtype nullable: true, blank: true
        releasedate nullable: true, blank: true
        lastplayed nullable: true, blank: true
    }

    static mapping = {
        desc type: 'text'
    }
}
