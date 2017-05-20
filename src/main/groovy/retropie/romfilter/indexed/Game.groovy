package retropie.romfilter.indexed

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongPoint
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.IntPoint
import org.apache.lucene.document.DoublePoint
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField

/**
 * Entry for one game from gamelist.xml.
 */
@ToString(includeNames = true)
@EqualsAndHashCode(includes = ["system", "path", "size"])
class Game {
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
     * The long value is in the format 20140418  for April 18, 2014.
     */
    long releasedate

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
     * The long value is in the format 20140418  for April 18, 2014.
     */
    long lastplayed

    /**
     * Size of the file.
     * Not in gamelist.xml, observed form disc.
     */
    long size

    /**
     * The document that was used to create this entry.
     * Null if the document wasn't reconstituted from the indexer.
     */
    Document document

    /**
     * Number to uniquely identify this GamelistEntry.
     */
    int getHash() {
        return hashCode()
    }

    /**
     * Default constructor.
     */
    Game() {
    }

    /**
     * Restore from Index constructor.
     */
    Game(Document document) {
        this()
        system = document.system
        scrapeId = document.scrapeId ?: ''
        scrapeSource = document.scrapeSource ?: ''
        path = document.path ?: ''
        name = document.name ?: ''
        desc = document.desc ?: ''
        image = document.image ?: ''
        thumbnail = document.thumbnail ?: ''
        developer = document.developer ?: ''
        publisher = document.publisher ?: ''
        genre = document.genre ?: ''
        players = document.players?.toInteger() ?: 1
        region = document.region ?: ''
        romtype = document.romtype ?: ''
        releasedate = document.releasedate?.toLong() ?: 0
        rating = document.rating?.toDouble() ?: 0.0
        playcount = document.playcount?.toInteger() ?: 0
        lastplayed = document.lastplayed?.toLong() ?: 0
        this.document = document
    }

    /**
     * Create a lucene document from this object.
     *
     * @return
     */
    Document convertToDocument(Document doc) {
        doc.add(new StringField("system", system, Field.Store.YES))
        if (scrapeId) doc.add(new StringField("scrapeId", scrapeId, Field.Store.YES))
        if (scrapeSource) doc.add(new StringField("scrapeSource", scrapeSource, Field.Store.YES))
        if (path) doc.add(new StringField("path", path, Field.Store.YES))
        if (name) {
            doc.add(new TextField("name", name, Field.Store.YES))
            doc.add(new StringField("nameOrder", name, Field.Store.YES))
        }
        // No sorting by description. Who cares.
        if (desc) doc.add(new TextField("desc", desc, Field.Store.YES))
        if (image) doc.add(new StringField("image", image, Field.Store.YES))
        if (thumbnail) doc.add(new StringField("thumbnail", thumbnail, Field.Store.YES))
        if (developer) {
            doc.add(new TextField("developer", developer, Field.Store.YES))
            doc.add(new StringField("developerOrder", developer, Field.Store.YES))
        }
        if (publisher) {
            doc.add(new TextField("publisher", publisher, Field.Store.YES))
            doc.add(new StringField("publisherOrder", developer, Field.Store.YES))
        }
        if (genre) {
            doc.add(new TextField("genre", genre, Field.Store.YES))
            doc.add(new StringField("genreOrder", genre, Field.Store.YES))
        }
        doc.add(new IntPoint("players", players))
        doc.add(new StoredField("players", players))
        if (region) {
            doc.add(new TextField("region", region, Field.Store.YES))
            doc.add(new StringField("regionOrder", region, Field.Store.YES))
        }
        if (romtype) {
            doc.add(new TextField("romtype", romtype, Field.Store.YES))
            doc.add(new StringField("romtypeOrder", romtype, Field.Store.YES))
        }

        if (releasedate) {
            doc.add(new LongPoint("releasedate", releasedate))
            doc.add(new StoredField("releasedate", releasedate))
        }

        doc.add(new DoublePoint("rating", rating))
        doc.add(new StoredField("rating", rating))
        doc.add(new IntPoint("playcount", playcount))
        doc.add(new StoredField("playcount", playcount))

        if (lastplayed) {
            doc.add(new LongPoint("lastplayed", lastplayed))
            doc.add(new StoredField("lastplayed", lastplayed))
        }
        doc.add(new IntPoint("hash", hash))
        doc.add(new StoredField("hash", hash))
        doc.add(new LongPoint("size", size))
        doc.add(new StoredField("size", size))

        String all = "${system} ${scrapeId} ${scrapeSource} ${path} ${name} ${desc} ${image} ${thumbnail} ${developer} ${publisher} ${genre} ${players} ${region} ${romtype} ${releasedate} ${rating} ${playcount} ${lastplayed} ${this.hash}"
        doc.add(new TextField("all", all, Field.Store.NO))

        return doc
    }

    boolean hasThumbnail() {
        return thumbnail != null && thumbnail != ""
    }

    boolean hasImage() {
        return image != null && image != ""
    }
}
