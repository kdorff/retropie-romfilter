package retropie.romfilter

import org.apache.log4j.Logger
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.Fields
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.MultiFields
import org.apache.lucene.index.Terms
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.Sort
import org.apache.lucene.search.SortField
import org.apache.lucene.search.SortedNumericSortField
import org.apache.lucene.search.SortedSetSortField
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.highlight.Highlighter
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException
import org.apache.lucene.search.highlight.QueryScorer
import org.apache.lucene.search.highlight.SimpleHTMLFormatter
import org.apache.lucene.search.highlight.SimpleSpanFragmenter
import org.apache.lucene.util.BytesRef
import retropie.romfilter.feed.GamesDataFeed
import retropie.romfilter.feed.datatables.DatatablesRequest
import retropie.romfilter.feed.datatables.RequestOrder
import retropie.romfilter.indexed.Game
import retropie.romfilter.parser.RomfilterQueryParser

import java.security.MessageDigest

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
     * Return the count of documents that match a query.
     *
     * @param query
     */
    int getCountForQuery(Query query) {
        IndexSearcher indexSearcher = new IndexSearcher(gamesIndexReader)
        int count = indexSearcher.count(query)
        return count
    }

    Query createQueryForString(String queryStr) {
        RomfilterQueryParser queryParser = new RomfilterQueryParser(queryAnalyzer)
        return queryParser.parse(queryStr)
    }

    /**
     * Return the count of documents for a specific system.
     *
     * @param query
     */
    int getCountForSystem(String system) {
        String queryStr = /+system:"${QueryParser.escape(system)}"/
        RomfilterQueryParser queryParser = new RomfilterQueryParser(queryAnalyzer)
        Query query = queryParser.parse(queryStr)
        return getCountForQuery(query)
    }


    /**
     * The list of terms defined for a specific field.
     * @return
     */
    List<String> listValuesForField(String field) {
        // https://stackoverflow.com/questions/8910008/how-can-i-get-the-list-of-unique-terms-from-a-specific-field-in-lucene
        List<String> allTerms = []
        Fields fields = MultiFields.getFields(gamesIndexReader)
        Terms terms = fields.terms(field)
        if (terms) {
            TermsEnum iterator = terms.iterator()
            BytesRef byteRef = null
            while ((byteRef = iterator.next()) != null) {
                allTerms << new String(byteRef.bytes, byteRef.offset, byteRef.length);
            }
        }
        return allTerms
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
    Game getGameForHash(String hash) {
        return getGameForQuery(/+hash:"${QueryParser.escape(hash)}"/)
    }

    /**
     * Get List[RomEntry] for datatables requeyst.
     *
     * @param datatablesRequest
     * @return
     */
    GamesDataFeed getGameDataFeedForRequest(DatatablesRequest datatablesRequest, boolean highlight = true) {
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
            results = indexSearcher.search(query, gamesIndexReader.maxDoc(), sort, true, false)
        } else {
            results = indexSearcher.search(query, gamesIndexReader.maxDoc())
        }
        ScoreDoc[] scoreDocs = results.scoreDocs

        GamesDataFeed gamesDataFeed = new GamesDataFeed([
            recordsTotal   : getGamesCount(),
            recordsFiltered: results.totalHits,
        ])
        SimpleHTMLFormatter htmlFormatter = null
        Highlighter highlighter = null
        QueryScorer queryScorer = null
        if (highlight) {
            htmlFormatter = new SimpleHTMLFormatter("<span class='MatchedText'>", "</span>");
            queryScorer = new QueryScorer(query)
            highlighter = new Highlighter(htmlFormatter, queryScorer)
        }

        for (int i = datatablesRequest.start; i < results.totalHits; i++) {
            if (i > (datatablesRequest.start + datatablesRequest.length) - 1) {
                break;
            }
            Document document = indexSearcher.doc(scoreDocs[i].doc)
            Game game = new Game(document)
            if (highlight) {
                highlightGame(highlighter, queryScorer, game)
            }
            gamesDataFeed.games << game
        }
        log.info("Found ${gamesDataFeed.games.size()}")
        return gamesDataFeed
    }

    /**
     * Build a sort for the datatables request.
     *
     * @param datatablesRequest
     * @return
     */
    Sort buildSort(DatatablesRequest datatablesRequest) {
        Sort sort = null
        if (datatablesRequest.orders) {
            List<SortField> sortFields = datatablesRequest.orders.collect { RequestOrder requestOrder ->
                Game.GameColumn gameColumn = Game.GameColumn.numberToGameColumn(requestOrder.columnNumber)
                if (gameColumn) {
                    if (gameColumn.sortFieldType == SortField.Type.STRING) {
                        SortField sortField = new SortedSetSortField(
                            gameColumn.orderField,
                            requestOrder.direction == RequestOrder.Direction.desc)

                        // Put blanks on the bottom. Always. This may not be popular.
                        sortField.setMissingValue(
                            requestOrder.direction == RequestOrder.Direction.asc ?
                                SortField.STRING_LAST : SortField.STRING_FIRST
                        )

                        return sortField
                    }
                    else if (gameColumn.sortFieldType in [SortField.Type.INT, SortField.Type.LONG, SortField.Type.DOUBLE, SortField.Type.FLOAT, SortField.Type.SCORE, SortField.Type.DOC]) {
                        SortField sortField = new SortedNumericSortField(
                            gameColumn.orderField,
                            gameColumn.sortFieldType,
                            requestOrder.direction == RequestOrder.Direction.desc)

                        return sortField
                    }
                    else {
                        log.error("Unsupported gameColumn.sortFieldType ${gameColumn.sortFieldType}")
                        return null
                    }
                }
                else {
                    return null
                }
            }.findAll { it != null }
            if (sortFields) {
                sort = new Sort(sortFields as SortField[])
            }
        }
        if (sort) {
            log.info("Returning sort: ${sort}")
        }
        return sort
    }

    /**
     * Highlight search terms in all string fields.
     * Currently the the way I have things set up this doesn't support
     * numbers (or URLs to images).
     *
     * @param highlighter
     * @param queryScorer
     * @param game
     */
    void highlightGame(Highlighter highlighter, QueryScorer queryScorer,  Game game) {
        game.system = highlightField(highlighter, queryScorer, 'system', game.system)
        game.path = highlightField(highlighter, queryScorer, 'path', game.path)
        game.name = highlightField(highlighter, queryScorer, 'name', game.name)
        game.desc = highlightField(highlighter, queryScorer, 'desc', clipText(game.desc, 500))
        game.developer = highlightField(highlighter, queryScorer, 'developer', game.developer)
        game.publisher = highlightField(highlighter, queryScorer, 'publisher', game.publisher)
        game.genre = highlightField(highlighter, queryScorer, 'genre', game.genre)
        game.region = highlightField(highlighter, queryScorer, 'region', game.region)
        game.romtype = highlightField(highlighter, queryScorer, 'romtype', game.romtype)
        game.scrapeId = highlightField(highlighter, queryScorer, 'scrapeId', game.scrapeId)
        game.scrapeSource = highlightField(highlighter, queryScorer, 'scrapeSource', game.scrapeSource)
    }

    /**
     * Clip long text. If clipped, append "..." to the clipped text.
     *
     * @param value
     * @return
     */
    String clipText(String value, int maxLength) {
        int size = value ?.size() ?: 0
        if (size < maxLength) {
            return  value
        }
        else {
            return value.substring(0, maxLength) + '...'
        }
    }

    /**
     * Return the text of a single field highlighted OR the original field if nothing
     * was highlighted.
     *
     * @param highlighter
     * @param queryScorer
     * @param field
     * @param value
     * @return
     * @throws IOException
     * @throws InvalidTokenOffsetsException
     */
    String highlightField(Highlighter highlighter, QueryScorer queryScorer, String field, String value) throws IOException, InvalidTokenOffsetsException {
        highlighter.setTextFragmenter(new SimpleSpanFragmenter(queryScorer, Integer.MAX_VALUE))
        highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE)
        String highlighted = highlighter.getBestFragment(queryAnalyzer, field, value)
        return  highlighted ?: value
    }

    /* ----------------------------------------------------------------------------
     * Index creation methods.
     */

    /**
     * Save a Game to the index.
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
     * Delete a Game document
     *
     * @param game
     * @return
     */
    void deleteGame(Game game) {
        String queryStr = /+hash:"${QueryParser.escape(game.hash)}"/
        RomfilterQueryParser queryParser = new RomfilterQueryParser(queryAnalyzer)
        try {
            Query query = queryParser.parse(queryStr)
            gamesIndexWriter.deleteDocuments(query)
        }
        catch (Exception e) {
            log.error("Error deleting Game document for hash ${game.hash}", e)
        }
    }

    /**
     * Delete all documents that match the specified query.
     * @param query
     */
    void deleteAllForQuery(Query query) {
        try {
            gamesIndexWriter.deleteDocuments(query)
            log.trace("Deleted Game documents for query ${query.toString()}")
        }
        catch (Exception e) {
            log.error("Error deleting Game documents for query ${query.toString()}", e)
        }
    }

    /**
     * Delete all documents that match the specified query.
     * @param query
     */
    void deleteAll() {
        deleteAllForQuery(new MatchAllDocsQuery())
    }

    /**
     * Delete all documents for a specific system.
     * TODO: This can probably be done with a Term instead of a query
     * TODO: which will be even better.
     *
     * @param system
     */
    void deleteAllForSystem(String system) {
        String queryStr = /+system:"${QueryParser.escape(system)}"/
        RomfilterQueryParser queryParser = new RomfilterQueryParser(queryAnalyzer)
        try {
            log.info("deleteing all Game documents for system ${system}")
            Query query = queryParser.parse(queryStr)
            deleteAllForQuery(query)
            log.info("All Game documents deleted for system ${system}")
        }
        catch (Exception e) {
            log.error("Error parsing system delete query for system ${system}", e)
        }
    }

    /**
     * Genereate hash for game.
     *
     * @param game
     * @return
     */
    String generateHash(Game game) {
        try {
            String md5 = "${game.system}|${game.path}|${game.size}"
            MessageDigest md = MessageDigest.getInstance("MD5")
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3))
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Problem creating MD5 for ${game}, returning UUID", e)
            return UUID.toString()
        }
    }
}
