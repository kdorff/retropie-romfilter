package retropie.romfilter

import groovy.transform.ToString
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.IntPoint
import org.apache.lucene.document.DoublePoint
import org.apache.lucene.document.TextField

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

    /**
     * Default constructor.
     */
    GamelistEntry() {
    }

    /**
     * From a Document constructor.
     */
    GamelistEntry(Document document) {
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
        releasedate = document.releasedate ?: ''
        rating = document.rating?.toDouble() ?: 0.0
        playcount = document.playcount?.toInteger() ?: 0
        lastplayed = document.lastplayed ?: ''
    }

    /**
     * Create a lucene document from this object.
     *
     * @return
     */
    Document makeDocument() {
        Document doc = new Document();
        doc.add(new TextField("system", system, Field.Store.YES))
        if (scrapeId) doc.add(new TextField("scrapeId", scrapeId, Field.Store.YES))
        if (scrapeSource) doc.add(new TextField("scrapeSource", scrapeSource, Field.Store.YES))
        if (path) doc.add(new TextField("path", path, Field.Store.YES))
        if (name) doc.add(new TextField("name", name, Field.Store.YES))
        if (desc) doc.add(new TextField("desc", desc, Field.Store.YES))
        if (image) doc.add(new TextField("image", image, Field.Store.YES))
        if (thumbnail) doc.add(new TextField("thumbnail", thumbnail, Field.Store.YES))
        if (developer) doc.add(new TextField("developer", developer, Field.Store.YES))
        if (publisher) doc.add(new TextField("publisher", publisher, Field.Store.YES))
        if (genre) doc.add(new TextField("genre", genre, Field.Store.YES))
        doc.add(new IntPoint("players", players))
        doc.add(new StoredField("players", players))
        if (region) doc.add(new TextField("region", region, Field.Store.YES))
        if (romtype) doc.add(new TextField("romtype", romtype, Field.Store.YES))
        if (releasedate) doc.add(new TextField("releasedate", releasedate, Field.Store.YES))
        doc.add(new DoublePoint("rating", rating))
        doc.add(new StoredField("rating", rating))
        doc.add(new IntPoint("playcount", playcount))
        doc.add(new StoredField("playcount", playcount))
        if (lastplayed) doc.add(new TextField("lastplayed", lastplayed, Field.Store.YES))
        return doc
    }

    boolean hasThumbnail() {
        return thumbnail != null && thumbnail != ""
    }

    boolean hasImage() {
        return image != null && image != ""
    }
}
