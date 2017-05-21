package retropie.romfilter

import org.apache.log4j.Logger
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.IntPoint
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.Sort
import org.apache.lucene.search.SortField
import org.apache.lucene.search.TopDocs
import retropie.romfilter.feed.GamesDataFeed
import retropie.romfilter.feed.datatables.DatatablesRequest
import retropie.romfilter.feed.datatables.RequestOrder
import retropie.romfilter.indexed.Game
import retropie.romfilter.queryParser.RomfilterQueryParser

class IndexerDataService {
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
     * Index query analyzer (auto-injected).
     */
    Analyzer queryAnalyzer

    /* ----------------------------------------------------------------------------
     * Data access methods
     */

    /**
     * Get the IndexReader for GamelistEntry documents.
     * Don't save this, get it fresh every you need it.
     * @return
     */
    IndexReader getGamesIndexReader() {
        return gamesIndexWriter.getReader()
    }

    /**
     * The number of Game documents.
     * @return
     */
    int getGamesCount() {
        // The field system is ALWAYS populated so we use that to count.
        return gamesIndexReader.getDocCount('system')
    }

    /**
     * Get List[Game] for query string.
     *
     * @param queryStr
     * @return
     */
    List<Game> getGamesForQuery(String queryStr) {
        RomfilterQueryParser queryParser = new RomfilterQueryParser(queryAnalyzer)
        return getGamesForQuery(queryParser.parse(queryStr))
    }

    /**
     * Get List[Game] entries for query.
     *
     * @param queryStr
     * @return
     */
    List<Game> getGamesForQuery(Query query) {
        //log.info("Performing query for GamelistEntry list: ${query}")
        IndexSearcher indexSearcher = new IndexSearcher(gamesIndexReader)
        return indexSearcher.search(query, gamesIndexReader.maxDoc()).scoreDocs.collect {
            Document document = indexSearcher.doc(it.doc)
            return new Game(document)
        }
    }

    /**
     * Return a Game for a query string, if matched.
     * Should querying for this item, for some odd reason, return more than one,
     * this will return the first one. NOTE: they query should always just return one.
     *
     * @param queryStr
     * @return
     */
    Game getGameForQuery(String queryStr) {
        List<Game> matches = getGamesForQuery(queryStr)
        if (matches) {
            if (matches.size() > 1) {
                log.error("getGameForQuery(${queryStr}) should have returned 1 but returend ${matches.size()}")
            }
            return matches[0]
        } else {
            return null
        }
    }

    /**
     * Return a Game for a query, if matched.
     * Should querying for this item, for some odd reason, return more than one,
     * this will return the first one. NOTE: they query should always just return one.
     *
     * @param queryStr
     * @return
     */
    Game getGameForQuery(Query query) {
        List<Game> matches = getGamesForQuery(query)
        if (matches) {
            if (matches.size() > 1) {
                log.error("getGameForQuery(${query}) should have returned 1 but returend ${matches.size()}")
            }
            return matches[0]
        } else {
            return null
        }
    }

    /**
     * Return a Game for a Game's hash value, if matched.
     * Should querying for this item, for some odd reason, return more than one,
     * this will return the first one. NOTE: they query should always just return one.
     *
     * @param hash
     * @return
     */
    Game getGameForHash(int hash) {
        return getGameForQuery(IntPoint.newExactQuery('hash', hash))
    }

    /**
     * Get List[RomEntry] for datatables requeyst.
     *
     * @param datatablesRequest
     * @return
     */
    GamesDataFeed getGameDataFeedForRequest(DatatablesRequest datatablesRequest) {
        RomfilterQueryParser queryParser = new RomfilterQueryParser(queryAnalyzer)
        Query query
        if (datatablesRequest.search) {
            try {
                query = queryParser.parse(datatablesRequest.search)
            }
            catch (ParseException e) {
                query = new MatchAllDocsQuery()
            }
        } else {
            query = new MatchAllDocsQuery()
        }

        log.info("Performing Game query: ${query}")
        IndexSearcher indexSearcher = new IndexSearcher(gamesIndexReader)

        Sort sort = buildSort(datatablesRequest)

        TopDocs results
        if (sort) {
            results = indexSearcher.search(query, gamesIndexReader.maxDoc(), sort)
        } else {
            results = indexSearcher.search(query, gamesIndexReader.maxDoc())
        }
        ScoreDoc[] scoreDocs = results.scoreDocs

        GamesDataFeed gamesDataFeed = new GamesDataFeed([
            recordsTotal   : getGamesCount(),
            recordsFiltered: results.totalHits,
        ])
        for (int i = datatablesRequest.start; i < results.totalHits; i++) {
            if (i > (datatablesRequest.start + datatablesRequest.length) - 1) {
                break;
            }
            Document document = indexSearcher.doc(scoreDocs[i].doc)
            gamesDataFeed.games << new Game(document)
        }
        log.info("Found ${gamesDataFeed.games.size()}")
        return gamesDataFeed
    }

    Sort buildSort(DatatablesRequest datatablesRequest) {
        Sort sort = null
//        if (datatablesRequest.orders) {
//            SortField[] sortFields = datatablesRequest.orders.collect { RequestOrder requestOrder ->
//                // Need to map column # to field and optionally the field's order field.
//                // We should first switch to returning all fields and have an enum in Game
//                // that maps field number to field's order field (which is often just the field
//                // itself). Note that desc isn't sortable.
//                // We should also first define the row to datatables in the initialization datatables javascript.
//            } as SortField[]
//            sort = new Sort(sortFields)
//        }
        return sort
    }

    /* ----------------------------------------------------------------------------
     * Index creation methods.
     */

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
