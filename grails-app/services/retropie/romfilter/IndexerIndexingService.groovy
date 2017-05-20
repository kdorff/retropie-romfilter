package retropie.romfilter

import org.apache.log4j.Logger
import org.apache.lucene.document.Document
import org.apache.lucene.document.IntPoint
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.Query
import retropie.romfilter.indexed.Game

class IndexerIndexingService {
    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * The IndexWriter for Game documents (auto-injected).
     * This is used to create new documents.
     */
    IndexWriter gamesIndexWriter

    /**
     * Save a Ga,e to the index.
     * @param game
     * @param doc reused for performance
     */
    void saveGame(Game game, Document doc) {
        doc.clear()
        game.convertToDocument(doc)
        gamesIndexWriter.addDocument(doc)
        log.info("Saved Game ${game.system}/${game.path} | hash=${game.hash} to index")
    }

    /**
     * Delete a GameEntry document
     *
     * @param gameEntry
     * @return
     */
    long deleteGame(Game gameEntry) {
        Query query = IntPoint.newExactQuery('hash', gameEntry.hash)
        gamesIndexWriter.deleteDocuments(query)
    }
}
