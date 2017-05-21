package retropie.romfilter.feed.datatables

import groovy.transform.ToString
import org.apache.log4j.Logger

import java.util.regex.Pattern

/**
 * Datatables request column. Column numbering is 0-based.
 */
@ToString(includeNames = true)
class RequestColumn {
    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * Pattern to match column number string.
     * columns[0][data]:0
     */
    final static Pattern columnNumberPattern = ~/^columns\[(\d+)\]\[data\]$/

    /**
     * Pattern to match column name string.
     * columns[0][name]:
     */
    final static Pattern namePattern = ~/^columns\[(\d+)\]\[name\]$/

    /**
     * Pattern to match column searchable string.
     * columns[0][searchable]:true
     */
    final static Pattern searchablePattern = ~/^columns\[(\d+)\]\[searchable\]/

    /**
     * Pattern to match column orderable string.
     * columns[0][orderable]:true
     */
    final static Pattern orderablePattern = ~/^columns\[(\d+)\]\[orderable\]$/

    /**
     * Pattern to match column search string.
     * columns[0][search][value]:
     */
    final static Pattern searchPattern = ~/^columns\[(\d+)\]\[search\]\[value\]$/

    /**
     * Pattern to match column search.regex string.
     * columns[0][search][regex]:false
     */
    final static Pattern searchIsRegexPattern = ~/^columns\[(\d+)\]\[search\]\[regex\]$/

    /**
     * Specifies the order of the RequestColumn objects in a list.
     * Use this to sort a List[RequestColumn].
     */
    int columnOrderNumber

    /**
     * Column number for this column.
     * The first column if data is 0.
     */
    int columnNumber

    /**
     * Name for this column.
     */
    String name

    /**
     * If the column is searchable.
     */
    boolean searchable

    /**
     * If the column is orderable.
     */
    boolean orderable

    /**
     * The search string to apply to this column.
     */
    String search

    /**
     * If the search string should be considered regex.
     */
    boolean searchIsRegex

    /**
     * Base constructor.
     */
    RequestColumn() {
        columnOrderNumber = 0
        columnNumber = 0
        name = ''
        searchable = true
        orderable = false
        search = ''
    }

    /**
     * Method to parse params into a list of RequestColumns for a datatable data feed request.
     *
     * @param params params from datatables server-side request.
     * @return list of RequestColumn
     */
    static List<RequestColumn> parseColumns(Map<String, String> params) {
        Map<Integer, RequestColumn> results = [:]
        params.each { paramKey, paramVal ->
            paramKey.find(columnNumberPattern) { whole, foundColNum  ->
                int col = foundColNum.toInteger()
                RequestColumn column = results.containsKey(col) ? results[col] : (results[col] = new RequestColumn())
                column.columnNumber = paramVal.toInteger()
            }
            paramKey.find(namePattern) { whole, String foundColNum  ->
                int col = foundColNum.toInteger()
                RequestColumn column = results.containsKey(col) ? results[col] : (results[col] = new RequestColumn())
                column.name = paramVal
            }
            paramKey.find(searchablePattern) { whole, String foundColNum ->
                int col = foundColNum.toInteger()
                RequestColumn column = results.containsKey(col) ? results[col] : (results[col] = new RequestColumn())
                column.searchable = paramVal.toBoolean()
            }
            paramKey.find(orderablePattern) { whole, String foundColNum ->
                int col = foundColNum.toInteger()
                RequestColumn column = results.containsKey(col) ? results[col] : (results[col] = new RequestColumn())
                column.orderable = paramVal.toBoolean()
            }
            paramKey.find(searchPattern) { whole, String foundColNum ->
                int col = foundColNum.toInteger()
                RequestColumn column = results.containsKey(col) ? results[col] : (results[col] = new RequestColumn())
                column.search = paramVal
            }
            paramKey.find(searchIsRegexPattern) { whole, String foundColNum ->
                int col = foundColNum.toInteger()
                RequestColumn column = results.containsKey(col) ? results[col] : (results[col] = new RequestColumn())
                column.searchIsRegex = paramVal.toBoolean()
            }
        }
        return results.values().sort() { RequestColumn a, RequestColumn b -> a.columnOrderNumber <=> b.columnOrderNumber }
    }
}
