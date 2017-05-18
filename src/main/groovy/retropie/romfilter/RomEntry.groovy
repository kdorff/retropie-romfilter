package retropie.romfilter

import grails.util.Holders
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.log4j.Logger
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.IntPoint
import org.apache.lucene.document.LongPoint
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.StringField

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['system', 'path', 'size'])
class RomEntry {
    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * The system this rom is for (example 'atari2600').
     */
    String system

    /**
     * Filename within the system's rom directory (no additional path).
     */
    String path

    /**
     * Size of the rom (on disk).
     */
    long size

    /**
     * Hash for associated GamelistEntry (or null if no entry).
     */
    Integer gamelistEntryHash

    /**
     * The document that was used to create this entry.
     * Null if the document wasn't reconstituted from the indexer.
     */
    Document document

    /**
     * Number to uniquely identify this RomEntry.
     */
    int getHash() {
        return hashCode()
    }

    /**
     * Store the associate GamelistEntry if we've already looked it up.
     */
    GamelistEntry gamelistEntry

    /**
     * Fetch the associated GamelistEntry.
     */
    GamelistEntry getGamelistEntry() {
        if (gamelistEntry == null) {
            IndexerDataService indexerDataService = Holders.getApplicationContext()?.getBean('indexerDataService')
            if (gamelistEntryHash) {
                gamelistEntry = indexerDataService.gamelistEntryForSystemAndHash(system, gamelistEntryHash)
            }
            else {
                gamelistEntry = indexerDataService.gamelistEntryForSystemAndPath(system, path)
            }
        }
        return gamelistEntry
    }

    /**
     * Default constructor.
     */
    RomEntry() {
    }

    /**
     * Restore from Index constructor.
     */
    RomEntry(Document document) {
        this()
        system = document.system
        path = document.path
        size = document.size.toLong() ?: 0
        gamelistEntryHash = document.gamelistEntryHash?.toInteger() ?: null   // Will return null of no GamelistEntry for this rom
        this.document = document
    }

    /**
     * Create a lucene document from this object.
     *
     * @return
     */
    Document makeDocument() {
        Document doc = new Document();
        doc.add(new StringField("system", system, Field.Store.YES))
        doc.add(new StringField("path", path, Field.Store.YES))

        doc.add(new LongPoint("size", size))
        doc.add(new StoredField("size", size))

        GamelistEntry gamelistEntry = getGamelistEntry()
        gamelistEntryHash = gamelistEntry?.hash
        if (gamelistEntryHash != null) {
            doc.add(new IntPoint("gamelistEntryHash", gamelistEntryHash))
            doc.add(new StoredField("gamelistEntryHash", gamelistEntryHash))
        }

        doc.add(new IntPoint("hash", hash))
        doc.add(new StoredField("hash", hash))
        log.info("RomEntry.doc = ${doc}")
        return doc
    }
}
