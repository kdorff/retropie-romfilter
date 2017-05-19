package retropie.romfilter.queryParser

import org.apache.log4j.Logger
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.DoublePoint
import org.apache.lucene.document.IntPoint
import org.apache.lucene.document.LongPoint
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.search.Query

/**
 * Handles ranges in my fields.
 */
class RomfilterQueryParser extends QueryParser {
    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * Fields and their type.
     */
    final Map<String, Class> rangleableFields = [
        'size': LongPoint,
        'hash': IntPoint,
        'players': IntPoint,
        'playcount': IntPoint,
        'rating': DoublePoint,
        'gamelistEntryHash': IntPoint,
    ]

    RomfilterQueryParser(Analyzer analyzer) {
        super('all', analyzer)
    }

    @Override
    Query createPhraseQuery(String field, String queryText, int phraseSlop) {
        log.info(/++ createFieldQuery(${field}, ${queryText}, ${phraseSlop})/)
        Query query = super.createPhraseQuery(field, queryText, phraseSlop)
        log.info(/++ query=${query.class.name}, ${query}/)
        return query
    }

    @Override
    Query getRangeQuery(String field, String part1, String part2, boolean inclusive, boolean endInclusive) throws ParseException {
        log.info("Perhaps creating range query ${field} ${part1} ${part2} ${inclusive}");

        Class dataClass = rangleableFields[field]
        log.info("dataClass=${dataClass}")
        Query result
        if (dataClass == IntPoint) {
            int lower = Integer.parseInt(part1)
            int upper = Integer.parseInt(part2)
            if (upper == lower) {
                result = IntPoint.newExactQuery(field, lower)
            }
            else {
                result = IntPoint.newRangeQuery(field, lower, upper)
            }
        } else if (dataClass == LongPoint) {
            log.info("it is an int query")
            long lower = Long.parseLong(part1)
            long upper = Long.parseLong(part2)
            if (upper == lower) {
                result = LongPoint.newExactQuery(field, lower)
            }
            else {
                result = LongPoint.newRangeQuery(field, lower, upper)
            }
        }
        else if (dataClass == DoublePoint) {
            log.info("it is an double query")
            double lower = Double.parseDouble(part1)
            double upper = Double.parseDouble(part2)
            if (upper == lower) {
                result = DoublePoint.newExactQuery(field, lower)
            }
            else {
                result = DoublePoint.newRangeQuery(field, lower, upper)
            }
        }
        else {
            // Build a normal term range query as a fallback
            result = super.getRangeQuery(field, part1, part2, inclusive, endInclusive);
        }
        return result
    }
}