package retropie.romfilter

import groovy.transform.ToString

/**
 * Entry for one game from gamelist.xml.
 */
@ToString(includeNames = true)
class GamelistEntry {
    int id          // attrib
    String source   // attrib
    String path
    String name
    String desc
    String image
    String developer
    String publisher
    String genre
    int players
    String region
    String romtype
    String releasedate

    boolean hasImage() {
        return image
    }
}
