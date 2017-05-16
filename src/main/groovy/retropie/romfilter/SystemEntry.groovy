package retropie.romfilter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField

@ToString(includeNames = true)
@EqualsAndHashCode
class SystemEntry {
    String name

    /**
     * The document that was used to create this entry.
     * Null if the document wasn't reconstituted from the indexer.
     */
    Document document

    SystemEntry() {
    }

    SystemEntry(Document document) {
        this()
        name = document.name
        this.document = document
    }

    /**
     * Create a lucene document from this object.
     *
     * @return
     */
    Document makeDocument() {
        Document doc = new Document();
        doc.add(new TextField("name", name, Field.Store.YES))
        return doc
    }
}
