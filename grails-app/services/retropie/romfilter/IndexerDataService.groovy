package retropie.romfilter

import org.apache.log4j.Logger
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.Query
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
     * Index query analyzer (auto-injected).
     */
    Analyzer queryAnalyzer

    /**
     * Get the IndexReader for SystemEntry documents.
     * @return
     */
    IndexReader getSystemsIndexReader() {
        return systemsIndexWriter.getReader()
    }

    /**
     * Get the IndexReader for GamelistEntry documents.
     * @return
     */
    IndexReader getGamesIndexReader() {
        return gamesIndexWriter.getReader()
    }

    /**
     * Get the IndexReader for RomEntry documents.
     * @return
     */
    IndexReader getRomsIndexReader() {
        return romsIndexWriter.getReader()
    }

    /**
     * The number of SystemEntry documents.
     * @return
     */
    int getSystemEntryCount() {
        return systemsIndexReader.getDocCount('system')
    }

    /**
     * The number of GamelistEntry documents.
     * @return
     */
    int getGamelistEntryCount() {
        return gamesIndexReader.getDocCount('system')
    }

    /**
     * The number of RomEntry documents.
     * @return
     */
    int getRomEntryCount() {
        return romsIndexReader.getDocCount('system')
    }

    /**
     * Return List[RomEntry] for a specific system.
     * @param system
     * @return
     */
    List<RomEntry> romEntriesForSystem(String system) {
        return romEntriesForQuery(/+system:"${QueryParser.escape(system)}"/)
    }

    /**
     * How many roms for a specific system.
     * TODO: there is probably a more optimal way to do this.
     *
     * @param system
     * @return
     */
    int getRomEntryCountForSystem(String system) {
        return romEntriesForSystem(system).size()
    }

    /**
     * Return a GamelistEntry for a given system and path, if it exists.
     * Should querying for this item, for some odd reason, return more than one,
     * this will return the first one. NOTE: they query should always just return one.
     *
     * @param system
     * @param path
     * @return
     */
    GamelistEntry gamelistEntryForQuery(String queryStr, Query moreQuery = null) {
        List<GamelistEntry> matches = gamelistEntriesForQuery(queryStr, moreQuery)
        if (matches) {
            if (matches.size() > 1) {
                log.error("gamelistEntryForQuery(${queryStr}) should have returned 1 but returend ${matches.size()}")
            }
            return matches[0]
        } else {
            return null
        }
    }

    /**
     * Retrieve all SytemEntry.
     * @return
     */
    List<SystemEntry> getAllSystems(){
        println "Retrieving all systems"
        Query query = new MatchAllDocsQuery()

        IndexSearcher indexSearcher = new IndexSearcher(systemsIndexReader)
        return indexSearcher.search(query, systemsIndexReader.maxDoc()).scoreDocs.collect {
            Document document = indexSearcher.doc(it.doc)
            return new SystemEntry(document)
        }
    }

    /**
     * Get gamelist entries for query.
     *
     * @param queryStr
     * @param moreQuery
     * @return
     */
    List<GamelistEntry> gamelistEntriesForQuery(String queryStr, Query moreQuery = null) {
        RomfilterQueryParser queryParser = new RomfilterQueryParser(queryAnalyzer)
        Query query = queryParser.parse(queryStr)
        if (moreQuery) {
            query = new BooleanQuery.Builder().
                add(query, BooleanClause.Occur.MUST).
                add(moreQuery, BooleanClause.Occur.MUST).
                build()
        }
        log.info("Performing query for GamelistEntry list: ${query}")
        IndexSearcher indexSearcher = new IndexSearcher(gamesIndexReader)
        return indexSearcher.search(query, gamesIndexReader.maxDoc()).scoreDocs.collect {
            Document document = indexSearcher.doc(it.doc)
            return new GamelistEntry(document)
        }
    }

    /**
     * Get List[RomEntry] for query.
     *
     * @param queryStr
     * @param moreQuery
     * @return
     */
    List<RomEntry> romEntriesForQuery(String queryStr, Query moreQuery = null) {
        RomfilterQueryParser queryParser = new RomfilterQueryParser(queryAnalyzer)
        Query query = queryParser.parse(queryStr)
        if (moreQuery) {
            query = new BooleanQuery.Builder().
                add(query, BooleanClause.Occur.MUST).
                add(moreQuery, BooleanClause.Occur.MUST).
                build()
        }
        log.info("Performing query for RomEntry list: ${query}")
        IndexSearcher indexSearcher = new IndexSearcher(romsIndexReader)
        List<RomEntry> roms = indexSearcher.search(query, romsIndexReader.maxDoc()).scoreDocs.collect {
            Document document = indexSearcher.doc(it.doc)
            return new RomEntry(document)
        }
        println "Found ${roms}"
        return roms
    }

    /**
     * Obtain a GamelistEntry for a system and path.
     *
     * @param system
     * @param hash
     * @return
     */
    GamelistEntry gamelistEntryForSystemAndPath(String system, String path) {
        GamelistEntry gamelistEntry = gamelistEntryForQuery(
            /+system:"${QueryParser.escape(system)}" +path:"${QueryParser.escape(path)}"/)
        if (gamelistEntry) {
            if (gamelistEntry.path.toString() != path || gamelistEntry.system != system) {
                log.error("Wanted system/path ${system}/${path} but was given ${gamelistEntry.system}/${gamelistEntry.path}")
            }
        }
        return gamelistEntry
    }

    /**
     * Obtain a GamelistEntry for a system and hash.
     *
     * @param system
     * @param hash
     * @return
     */
    GamelistEntry gamelistEntryForSystemAndHash(String system, int hash) {
        String hashVal = QueryParser.escape(hash.toString())
        String hashRange = "[${hashVal} TO ${hashVal}]"
        GamelistEntry entry = gamelistEntryForQuery(
            /+system:"${QueryParser.escape(system)}" +hash:${hashRange}/)
        return entry
    }


    /**
     * Obtain a RomEntry for a specific system and hash.
     *
     * @param system
     * @param hash
     * @return
     */
    RomEntry romEntryForSystemAndHash(String system, int hash) {
        String hashVal = QueryParser.escape(hash.toString())
        String hashRange = "[${hashVal} TO ${hashVal}]"
        RomEntry entry = romEntryForQuery(
            /+system:"${(QueryParser.escape(system))}" +hash:${hashRange}/)
        return entry
    }

    /**
     * Return a GamelistEntry for a given system and path, if it exists.
     * Should querying for this item, for some odd reason, return more than one,
     * this will return the first one. NOTE: they query should always just return one.
     *
     * @param system
     * @param path
     * @return
     */
    RomEntry romEntryForQuery(String queryStr, Query moreQuery = null) {
        List<RomEntry> matches = romEntriesForQuery(queryStr)
        if (matches) {
            if (matches.size() > 1) {
                log.error("romEntryForQuery(${queryStr}) should have returned 1 but returend ${matches.size()}")
            }
            return matches[0]
        } else {
            return null
        }
    }

    /**
     * Return all SystemEntry.
     *
     * @param system
     * @return
     */
    List<SystemEntry> systemEntries() {
        Query query = new MatchAllDocsQuery()
        IndexSearcher indexSearcher = new IndexSearcher(systemsIndexReader)
        return indexSearcher.search(query, systemsIndexReader.maxDoc()).scoreDocs.collect {
            Document document = indexSearcher.doc(it.doc)
            return new SystemEntry(document)
        }
    }

    /**
     * When creating non-phrase queries one must escape spaces.
     *
     * @param s
     * @return
     */
    String escapeSpaces(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (c == ' ') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}