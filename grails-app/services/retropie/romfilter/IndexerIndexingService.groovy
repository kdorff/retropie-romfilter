package retropie.romfilter

import org.apache.log4j.Logger
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.Query

class IndexerIndexingService {
    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * The IndexWriter for GamelistEntry documents (auto-injected).
     * This is used to create new documents.
     */
    IndexWriter gamesIndexWriter

    /**
     * The IndexWriter for SystemEntry documents (auto-injected).
     * This is used to create new documents.
     */
    IndexWriter systemsIndexWriter

    /**
     * The IndexWriter for RomEntry documents (auto-injected).
     * This is used to create new documents.
     */
    IndexWriter romsIndexWriter

    /**
     * IndexerDataService (auto-injected).
     */
    IndexerDataService indexerDataService

    /**
     * Index query analyzer (auto-injected).
     */
    Analyzer queryAnalyzer

    /**
     * Save a SystemEntry to the index.
     * @param systemEntry
     */
    void saveSystemEntry(SystemEntry systemEntry) {
        Document doc = systemEntry.makeDocument()
        systemsIndexWriter.addDocument(doc)
        log.info("Saved SystemEntry ${systemEntry.name} to index")
    }

    /**
     * Save a GamelistEntry to the index.
     * @param systemEntry
     */
    void saveGamelistEntry(GamelistEntry gamelistEntry) {
        Document doc = gamelistEntry.makeDocument()
        gamesIndexWriter.addDocument(doc)
        log.info("Saved GamelistEntry ${gamelistEntry.system} | ${gamelistEntry.name} | ${gamelistEntry.hash} to index")
    }

    /**
     * Save a RomEntry to the index.
     * @param romEntry
     */
    void saveRomEntry(RomEntry romEntry) {
        Document doc = romEntry.makeDocument()
        romsIndexWriter.addDocument(doc)
        log.info("Saved RomEntry ${romEntry.system} | ${romEntry.filename} | ${romEntry.hash} to index")
    }

    /**
     * Try to delete one or more RomsEntry from the roms index with a provided query.
     *
     * @param queryStr query the defines the documents to delete
     * @return the sequence number for the delete operation
     */
    void deleteRomEntriesForQuery(String queryStr, Query moreQuery = null) {
        StandardQueryParser queryParser = new StandardQueryParser(queryAnalyzer)
        Query query = queryParser.parse(queryStr, "")
        if (moreQuery) {
            query = new BooleanQuery.Builder().
                add(query, BooleanClause.Occur.MUST).
                add(moreQuery, BooleanClause.Occur.MUST).
                build()
        }
        romsIndexWriter.deleteDocuments(query)
    }

    /**
     * Delete a RomEntry document
     *
     * @param romEntry
     * @return
     */
    void deleteRomEntry(RomEntry romEntry) {
        String hashVal = QueryParser.escape(romEntry.hash.toString())
        String hashRange = "[${hashVal} TO ${hashVal}]"
        deleteRomEntriesForQuery(
            /+system:${indexerDataService.escapeSpaces(QueryParser.escape(romEntry.system))} +hash:${hashRange}/)
    }
}
