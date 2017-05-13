package retropie.romfilter

import groovy.transform.ToString

/**
 * Entry for one game from gamelist.xml.
 */
@ToString(includeNames = true)
class GamelistEntry {
    String system         // not in gamelist.xml, effectively what folder the gamelist.xml file was in
    String scrapeId       // attrib
    String scrapeSource   // attrib
    String path
    String name
    String desc
    String image
    String thumbnail
    String developer
    String publisher
    String genre
    int players
    String region
    String romtype
    String releasedate
    double rating
    int playcount
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
