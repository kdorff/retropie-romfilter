package retropie.romfilter.feed.datatables

import java.util.regex.Pattern

/**
 * Datatables request column. Column numbering is 0-based.
 */
class RequestColumn {
    int columnNumber
    String name
    boolean searchable
    boolean orderable
    String search
    boolean searchIsRegex

    RequestColumn() {
        columnNumber = 0
        name = ''
        searchable = true
        orderable = false
        search = ''
        searchIsRegex = false
    }

    Pattern columnNumberPattern = ~/^columns\[(\d+)\]\[data\]$/
    Pattern namePattern = ~/^columns\[(\d+)\]\[name\]$/
    Pattern searchablePattern = ~/^columns\[(\d+)\]\[searchable\]/
    Pattern orderablePattern = ~/^columns\[(\d+)\]\[orderable\]$/
    Pattern searchPattern = ~/^columns\[(\d+)\]\[search\]\[value\]$/
    Pattern searchIsRegexPattern = ~/^columns\[(\d+)\]\[search\]\[regex\]$/

    static List<RequestColumn> parseColumns(Map<String, String> params) {
        Map<Integer, RequestColumn> results = [:]
        params.each { paramKey, paramVal ->
            paramKey.find(columnNumberPattern) { whole, foundColNum  ->
                assert foundColNum == paramVal
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
        return results.values().sort() { RequestColumn a, RequestColumn b -> a.columnNumber <=> b.columnNumber }
    }
}
