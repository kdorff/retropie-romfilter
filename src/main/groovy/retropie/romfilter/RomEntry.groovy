package retropie.romfilter

import grails.util.Holders
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongPoint
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.TextField

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
     * Hash of RomEntry (currently md5 hash of filename).
     */
    String hash

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
     * I cannot decide if I love or at this. I think it's staying.
     */
    GamelistEntry getGamelistEntry() {
        return Holders.getApplicationContext()?.getBean('indexerDataService')?.
            gamelistEntryForQuery(/system:"${system}" AND path:"${filename}"/)
    }

    RomEntry() {
    }

    RomEntry(Document document) {
        this()
        system = document.system
        filename = document.filename
        size = document.size.toLong() ?: 0
        hasGamelistEntry = document.hasGamelistEntry == 'true' ?: false
        hash = filename
        this.document = document
    }

    /**
     * Create a lucene document from this object.
     *
     * @return
     */
    Document makeDocument() {
        Document doc = new Document();
        doc.add(new TextField("system", system, Field.Store.YES))
        doc.add(new TextField("filename", filename, Field.Store.YES))
        doc.add(new LongPoint("size", size))
        doc.add(new StoredField("size", size))
        doc.add(new TextField("hasGamelistEntry", hasGamelistEntry.toString(), Field.Store.YES))
        return doc
    }
}
