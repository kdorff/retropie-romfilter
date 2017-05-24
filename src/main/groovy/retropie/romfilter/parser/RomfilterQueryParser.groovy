package retropie.romfilter.parser

import org.apache.log4j.Logger
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.DoublePoint
import org.apache.lucene.document.FloatPoint
import org.apache.lucene.document.IntPoint
import org.apache.lucene.document.LongPoint
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.search.Query
import org.apache.lucene.search.SortField
import retropie.romfilter.indexed.Game

/**
 * Handles ranges in my fields.
 */
class RomfilterQueryParser extends QueryParser {
    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

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
        //log.info("Perhaps creating range query ${field} ${part1} ${part2} ${inclusive}");

        SortField.Type sortfieldType = Game.GameColumn.fieldToGameColumn(field)?.sortFieldType
        //log.info("dataClass=${dataClass}")
        Query result
        try {
            if (sortfieldType == SortField.Type.INT) {
                //log.info("it is an int query")
                int lower = Integer.parseInt(part1)
                int upper = Integer.parseInt(part2)
                if (upper == lower) {
                    result = IntPoint.newExactQuery(field, lower)
                } else {
                    result = IntPoint.newRangeQuery(field, lower, upper)
                }
            } else if (sortfieldType == SortField.Type.LONG) {
                //log.info("it is an long query")
                long lower = Long.parseLong(part1)
                long upper = Long.parseLong(part2)
                if (upper == lower) {
                    result = LongPoint.newExactQuery(field, lower)
                } else {
                    result = LongPoint.newRangeQuery(field, lower, upper)
                }
            } else if (sortfieldType == SortField.Type.DOUBLE) {
                //log.info("it is an double query")
                double lower = Double.parseDouble(part1)
                double upper = Double.parseDouble(part2)
                if (upper == lower) {
                    result = DoublePoint.newExactQuery(field, lower)
                } else {
                    result = DoublePoint.newRangeQuery(field, lower, upper)
                }
            } else if (sortfieldType == SortField.Type.FLOATz) {
                //log.info("it is an double query")
                float lower = Float.parseFloat(part1)
                float upper = Float.parseFloat(part2)
                if (upper == lower) {
                    result = FloatPoint.newExactQuery(field, lower)
                } else {
                    result = FloatPoint.newRangeQuery(field, lower, upper)
                }
            } else {
                // Build a normal term range query as a fallback
                result = super.getRangeQuery(field, part1, part2, inclusive, endInclusive);
            }
            return result
        }
        catch (NumberFormatException e) {
            log.error("Bad query field:${field} part1:${part1} part2:${part2} inclusive:${inclusive}")
            return super.getRangeQuery(field, part1, part2, inclusive, endInclusive)
        }
    }
}