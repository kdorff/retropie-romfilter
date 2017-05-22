package retropie.romfilter.indexed

import grails.util.Holders
import groovy.transform.ToString
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongPoint
import org.apache.lucene.document.NumericDocValuesField
import org.apache.lucene.document.SortedDocValuesField
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.IntPoint
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.search.SortField
import org.apache.lucene.util.BytesRef

/**
 * Entry for one game from gamelist.xml.
 */
@ToString(includeNames = true, excludes = [])
class Game {

    /**
     * Column details.
     * *Point types here are not orderable. I need to index a bit more data for them to be Orderable.
     * See
     * https://lists.gt.net/lucene/java-user/315384
     * http://stackoverflow.com/questions/38358087/how-to-sort-intpont-or-longpoint-field-in-lucene-6
     */
    enum GameColumn {
        SYSTEM([
            number: 0,
            field: 'system',
            initiallyVisible: true,
        ]),
        PATH([
            number: 1,
            field: 'path',
            orderable: false,
            searchable: false,
        ]),
        SIZE([
            number: 2,
            field: 'size',
            sortFieldType: SortField.Type.LONG,
        ]),
        NAME([
            number: 3,
            field: 'name',
            initiallyVisible: true,
        ]),
        NAME_PATH_COMPARISON([
            number:4,
            field:'namePathComparison',
            orderable: false,
            searchable: false,
            friendlyName: 'Name/Path Comparison',
            initiallyVisible: true
        ]),
        DESCRIPTION([
            number:5,
            field:'desc',
            orderable: false,
            friendlyName: 'Description',
            initiallyVisible:  true,
        ]),
        IMAGE([
            number:  6,
            field:  'image',
            orderable: false,
            searchable:  false,
            initiallyVisible:  true,
        ]),
        THUMBNAIL([
            number: 7,
            field: 'thumbnail',
            orderable: false,
            searchable:  false,
        ]),
        DEVELOPER([
            number: 8,
            field: 'developer',
            initiallyVisible:  true,
        ]),
        PUBLISHER([
            number: 9,
            field: 'publisher',
            initiallyVisible: true,
        ]),
        GENRE([
            number: 10,
            field: 'genre',
        ]),
        PLAYERS([
            number: 11,
            field: 'players',
            sortFieldType: SortField.Type.INT,
        ]),
        REGION([
            number: 12,
            field: 'region',
        ]),
        ROM_TYPE([
            number: 13,
            field: 'romtype',
            friendlyName: 'ROM Type',
        ]),
        RELEASE_DATE([
            number: 14,
            field: 'releasedate',
            friendlyName: 'Release Date',
            sortFieldType: SortField.Type.LONG,
        ]),
        RATING([
            number: 15,
            field: 'rating',
            sortFieldType: SortField.Type.INT,
            initiallyVisible: true,
        ]),
        PLAY_COUNT([
            number: 16,
            field: 'playcount',
            friendlyName:  'Play Count',
            sortFieldType: SortField.Type.INT,
        ]),
        LAST_PLAYED([
            number: 17,
            field: 'lastplayed',
            friendlyName: 'Last Played',
            sortFieldType: SortField.Type.LONG,
        ]),
        SCRAPE_ID([
            number: 18,
            field: 'scrapeId',
            friendlyName: 'Scrape ID',
        ]),
        SCRAPE_SOURCE([
            number: 19,
            field: 'scrapeSource',
            friendlyName: 'Scrape Source',
        ]),
        HASH([
            number: 20,
            field: 'hash',
        ])

        int number
        String field
        String orderField
        boolean searchable
        boolean orderable
        String friendlyName
        String initiallyVisible
        SortField.Type sortFieldType

        /**
         * Construct a GameColumn
         *
         * @param number
         * @param field
         * @param orderField
         * @param searchable
         * @param friendlyName
         * @param initiallyVisible
         */
        GameColumn(Map config) {
            // Cannot store this list in static. Hmm, where to put it?
            List<String> validConfigKeys = [
                'number', 'field', 'orderField', 'searchable', 'orderable',
                'friendlyName', 'initiallyVisible', 'sortFieldType'
            ]

            // Make sure all the config keys are valid for this configuration
            // This will throw an exception if the enum's config Map is mis-configured (hopefully).
            Set<String> invalidKeys = config.keySet().findAll { String configKey ->
                return (!(configKey in validConfigKeys))
            }
            if (invalidKeys) {
                throw new IllegalArgumentException("Invalid keys ${invalidKeys} during configuration of GameColumn")
            }
            if (config.number == null|| !config.field) {
                throw new IllegalArgumentException(
                    "Invalid value for required field, one of GameColumn.number or GameColumn.field ")
            }

            this.number = config.number
            this.field = config.field
            this.orderable = config.orderable == null ? true : config.orderable
            this.orderField = config.orderField ?: "${this.field}Sorted"
            this.searchable = config.searchable == null ? true : config.orderable
            this.friendlyName = config.friendlyName ?: this.field.capitalize()
            this.initiallyVisible = config.initiallyVisible == null ? false : config.initiallyVisible
            this.sortFieldType = config.sortFieldType ?: SortField.Type.STRING

            println "Enum ${this} number=${number} field=${field} orderable=${orderable} " +
                "orderField=${orderField} searchable=${searchable} friendlyName=${friendlyName} " +
                "initiallyVisible=${initiallyVisible} sortFieldType=${sortFieldType}"
        }

        /**
         * Find the GameColumn for a given column number
         * TODO Cache all values into a map the first time? But norrmally
         * TODO just looking up one or two different ones so hmm.
         *
         * @param i column number to find
         * @return
         */
        static GameColumn numberToGameColumn(int i) {
            return values().find { GameColumn gameColumn ->
                return gameColumn.number == i
            }
        }
    }

    /**
     * Always has  value since the file must exist at the time of scanning.
     * The system this rom is for (example 'atari2600').
     * Not in gamelist.xml, discovered during scanning based on the folder name that is being scanned.
     */
    String system

    /**
     * Always has  value since the file must exist at the time of scanning.
     * The path (filename) to the rom within the system's rom folder (no additional path).
     * The additional pathing that exists in gamelist.xml is removed during parsing.
     * Only games that exist (at the time of scanning) will appear in the index.
     */
    String path

    /**
     * Always has  value since the file must exist at the time of scanning.
     * Size of the file. Since we only store games that existed during indexing, we know size will have a value.
     * Not in gamelist.xml, observed form disc.
     */
    long size

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
    int rating

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
     * The id within scrapeSource for this game.
     * This may generally be an int, but using a String for flexibility.
     * This value comes from the attribute "game.@id" In gamelist.txt
     */
    String scrapeId

    /**
     * The source that of data for this game's scrape.
     * This value comes from the attribute "game.@source" In gamelist.txt
     */
    String scrapeSource

    /**
     * Hash.
     */
    String hash

    /**
     * The document that was used to create this entry.
     * Null if the document wasn't reconstituted from the indexer.
     */
    Document document

    /**
     * Comparison of name and path.
     * Synthetic column, not in index so not sortable.
     *
     * @return
     */
    String getNamePathComparison() {
        return Holders.applicationContext.getBean('gameService')?.namePathComparison(name, path)
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
        rating = Math.round((document.rating?.toDouble() ?: 0.0) * 100) as int
        playcount = document.playcount?.toInteger() ?: 0
        lastplayed = document.lastplayed?.toLong() ?: 0
        hash = document.hash
        this.document = document
    }

    /**
     * Create a lucene document from this object.
     *
     * @return
     */
    Document convertToDocument(Document doc) {
        doc.add(new StringField("system", system, Field.Store.YES))
        doc.add(new SortedDocValuesField("systemOrder", new BytesRef(system)))

        if (scrapeId) {
            doc.add(new StringField("scrapeId", scrapeId, Field.Store.YES))
            doc.add(new SortedDocValuesField("scrapeIdOrder", new BytesRef(scrapeId)))
        }

        if (scrapeSource) {
            doc.add(new StringField("scrapeSource", scrapeSource, Field.Store.YES))
            doc.add(new SortedDocValuesField("scrapeSourceOrder", new BytesRef(scrapeSource)))
        }

        if (path) doc.add(new StringField("path", path, Field.Store.YES))

        if (name) {
            doc.add(new TextField("name", name, Field.Store.YES))
            doc.add(new SortedDocValuesField("nameOrder", new BytesRef(name)))
        }

        // No sorting by description. Who cares.
        if (desc) doc.add(new TextField("desc", desc, Field.Store.YES))

        if (image) doc.add(new StringField("image", image, Field.Store.YES))

        if (thumbnail) doc.add(new StringField("thumbnail", thumbnail, Field.Store.YES))

        if (developer) {
            doc.add(new TextField("developer", developer, Field.Store.YES))
            doc.add(new SortedDocValuesField("developerOrder", new BytesRef(developer)))
        }

        if (publisher) {
            doc.add(new TextField("publisher", publisher, Field.Store.YES))
            doc.add(new SortedDocValuesField("publisherOrder", new BytesRef(publisher)))
        }
        if (genre) {
            doc.add(new TextField("genre", genre, Field.Store.YES))
            doc.add(new SortedDocValuesField("genreOrder", new BytesRef(genre)))
        }

        doc.add(new IntPoint("players", players))
        doc.add(new StoredField("players", players))
        doc.add(new NumericDocValuesField("playersOrder", players))

        if (region) {
            doc.add(new TextField("region", region, Field.Store.YES))
            doc.add(new SortedDocValuesField("regionOrder", new BytesRef(region)))
        }

        if (romtype) {
            doc.add(new TextField("romtype", romtype, Field.Store.YES))
            doc.add(new SortedDocValuesField("romtypeOrder", new BytesRef(romtype)))
        }

        if (releasedate) {
            doc.add(new LongPoint("releasedate", releasedate))
            doc.add(new StoredField("releasedate", releasedate))
            doc.add(new NumericDocValuesField("releasedateOrder", releasedate))
        }

        doc.add(new IntPoint("rating", rating))
        doc.add(new StoredField("rating", rating))
        doc.add(new NumericDocValuesField("ratingOrder", rating))

        doc.add(new IntPoint("playcount", playcount))
        doc.add(new StoredField("playcount", playcount))
        doc.add(new NumericDocValuesField("playcountOrder", playcount))

        if (lastplayed) {
            doc.add(new LongPoint("lastplayed", lastplayed))
            doc.add(new StoredField("lastplayed", lastplayed))
            doc.add(new NumericDocValuesField("lastplayedOrder", lastplayed))
        }

        doc.add(new StringField("hash", hash, Field.Store.YES))
        doc.add(new SortedDocValuesField("hash", new BytesRef(hash)))

        doc.add(new LongPoint("size", size))
        doc.add(new StoredField("size", size))
        doc.add(new NumericDocValuesField("sizeOrder", size))

        String all = "${system} ${scrapeId} ${scrapeSource} ${path} ${name} ${desc} ${image} ${thumbnail} ${developer} ${publisher} ${genre} ${players} ${region} ${romtype} ${releasedate} ${rating} ${playcount} ${lastplayed} ${hash}"
        doc.add(new TextField("all", all, Field.Store.NO))

        return doc
    }
}
