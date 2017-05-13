package retropie.romfilter

import groovy.transform.ToString

/**
 * Entry for one game from gamelist.xml.
 */
@ToString(includeNames = true)
class GamelistEntry {
    String system   // not in gamelist.xml, what system this rom is for
    String id       // attrib
    String source   // attrib
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
}
