package retropie.romfilter.feed.datatables

import groovy.transform.ToString

import java.util.regex.Pattern

/**
 * Request for data from DataTables.
 */
@ToString(includeNames = true)
class DatatablesRequest {

    /**
     * A nonce. Echo the same number back in the response.
     */
    int draw

    /**
     * The starting record number.
     * I assume this is the start within the filtered setup.
     */
    int start

    /**
     * Length to return. If -1 it means the whole set which is less than ideal.
     */
    int length

    /**
     * The global search string.
     */
    String search

    /**
     * if the global search is regex.
     */
    boolean searchIsRegex

    /**
     * The column data, such as column number and per-column filtering,
     */
    List<RequestColumn> columns

    /**
     * How the results should be ordered.
     */
    List<RequestOrder> orders

    /**
     * Base constructor.
     */
    DatatablesRequest() {
        draw = 0
        start = 0
        length = 10
        search = ""
        searchIsRegex = false
        columns = []
        orders = []
    }

    DatatablesRequest(Map<String, String> params) {
        this()
        params.each { paramKey, paramVal ->
            switch (paramKey) {
                case "draw":
                    draw = paramVal.toInteger()
                    break
                case "start":
                    start = paramVal.toInteger()
                    break
                case "length":
                    length = paramVal.toInteger()
                    break
                case "search[value]":
                    search = paramVal
                    break
                case "search[regex]":
                    searchIsRegex = paramVal.toBoolean()
                    break
            }
        }
        columns = RequestColumn.parseColumns(params)
        orders = RequestOrder.parseOrders(params)
    }
}
