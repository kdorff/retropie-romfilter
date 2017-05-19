package retropie.romfilter.feed.datatables

/**
 * Request for data from DataTables.
 */
class Request {
    int draw
    List<RequestColumn> columns
    List<RequestOrder> orders
    int start
    int length
    String search
    String searchIsRegex

//    order[0][column]:0
//    order[0][dir]:asc
//    start:0
//    length:-1
//    search[value]:
//    search[regex]:false

    Request() {
    }

    Request(Map<String, String> params) {

    }
}
