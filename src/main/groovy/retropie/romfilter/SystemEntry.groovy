package retropie.romfilter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.IntPoint
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.StringField

@ToString(includeNames = true)
@EqualsAndHashCode(includes = ['system'])
class SystemEntry {
    /**
     * The name of the system (such as atari2600).
     */
    String system

    /**
     * The document that was used to create this entry.
     * Null if the document wasn't reconstituted from the indexer.
     */
    Document document

    /**
     * Default constructor.
     */
    SystemEntry() {
    }

    int getHash() {
        return hashCode()
    }

    /**
     * Restore from Index constructor.
     */
    SystemEntry(Document document) {
        this()
        system = document.system
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
        doc.add(new IntPoint("hash", hash))
        doc.add(new StoredField("hash", hash))
        return doc
    }
}
