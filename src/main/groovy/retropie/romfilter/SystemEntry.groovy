package retropie.romfilter

import groovy.transform.ToString
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField

@ToString(includeNames = true)
class SystemEntry {
    String name

    SystemEntry() {
    }

    SystemEntry(Document document) {
        name = document.name
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
