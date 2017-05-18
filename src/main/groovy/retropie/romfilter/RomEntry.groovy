package retropie.romfilter

import grails.util.Holders
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.IntField
import org.apache.lucene.document.LongField
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.StringField

@ToString(includeNames = true)
@EqualsAndHashCode
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
     * If the rom has a gamelistEntry.
     */
    boolean hasGamelistEntry

    /**
     * The document that was used to create this entry.
     * Null if the document wasn't reconstituted from the indexer.
     */
    Document document

    /**
     * Store the associate GamelistEntry if we've already looked it up.
     */
    transient GamelistEntry gamelistEntry

    /**
     * Number to uniquely identify this RomEntry.
     */
    int getHash() {
        return this.hashCode()
    }

    /**
     * Fetch the associated GamelistEntry.
     */
    GamelistEntry getGamelistEntry() {
        if (gamelistEntry == null) {
            gamelistEntry = Holders.getApplicationContext()?.getBean('indexerDataService')?.
                gamelistEntryForSystemAndPath(system, filename)
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
        filename = document.filename
        size = document.size.toLong() ?: 0
        hasGamelistEntry = document.hasGamelistEntry == 'true' ?: false
        hash = document.hash.toInteger() ?: 0
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
        doc.add(new StringField("filename", filename, Field.Store.YES))

        doc.add(new LongField("size", size, Field.Store.YES))

        doc.add(new StoredField("size", size))
        if (gamelistEntry) doc.add(new IntField("gamelistHash", gamelistEntry.hash, Field.Store.YES))
        doc.add(new IntField("hash", hash, Field.Store.YES))
        //doc.add(new StoredField("hash", hash))
        return doc
    }
}
