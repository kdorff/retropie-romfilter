package retropie.romfilter

import grails.util.Holders
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongPoint
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.TextField

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
     * Optional. Associated gamelistEntry (rom has been scraped).
     */
    GamelistEntry getGamelistEntry() {
        return Holders.getApplicationContext()?.getBean('indexerService')?.
            gamelistEntryForSystemAndFilename(system, filename)
    }

    RomEntry() {
    }

    RomEntry(Document document) {
        system = document.system
        filename = document.filename
        size = document.size.toLong() ?: 0
        hasGamelistEntry = document.hasGamelistEntry == 'true' ?: false
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
