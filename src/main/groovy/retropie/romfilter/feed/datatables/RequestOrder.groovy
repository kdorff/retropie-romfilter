package retropie.romfilter.feed.datatables

import groovy.transform.ToString

import java.util.regex.Pattern

/**
 * Datatables request column. Column numbering is 0-based.
 */
@ToString(includeNames = true)
class RequestOrder {

    /**
     * Direction of a sort.
     */
    enum Direction {
        asc,
        desc,
    }

    /**
     * Pattern to match column number string.
     * example: order[0][column]:0
     */
    final static Pattern orderColumnPattern = ~/^order\[(\d+)\]\[column\]$/

    /**
     * Pattern to match column number string.
     * example: order[0][dir]:asc
     */
    final static Pattern orderDirectionPattern = ~/^order\[(\d+)\]\[dir\]$/

    /**
     * Specifies the order of the RequestOrder objects in a list.
     */
    int columnOrderNumber

    /**
     * Column name to sort on.
     */
    String columnName

    /**
     * Sort direction.
     */
    Direction direction

    /**
     * Base constructor.
     */
    RequestOrder() {
        columnOrderNumber = 0
        columnName = ""
        direction = Direction.asc
    }

    /**
     * Method to parse params into a list of RequestOrder for a datatable data feed request.
     *
     * @param params params from datatables server-side request.
     * @return list of RequestColumn
     */
    static List<RequestOrder> parseOrders(Map<String, String> params) {
        Map<Integer, RequestOrder> results = [:]
        params.each { paramKey, paramVal ->
            paramKey.find(orderColumnPattern) { whole, foundI  ->
                int i = foundI.toInteger()
                RequestOrder order = results.containsKey(i) ? results[i] : (results[i] = new RequestOrder())
                order.columnName = paramVal
            }
            paramKey.find(orderDirectionPattern) { whole, String foundI  ->
                int i = foundI.toInteger()
                RequestOrder order = results.containsKey(i) ? results[i] : (results[i] = new RequestOrder())
                order.direction = Direction.valueOf(paramVal)
            }
        }
        return results.values().sort() { RequestOrder a, RequestOrder b -> a.columnOrderNumber <=> b.columnOrderNumber }
    }
}
