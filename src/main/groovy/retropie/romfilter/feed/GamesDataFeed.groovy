package retropie.romfilter.feed

import retropie.romfilter.indexed.Game

/**
 * Feed of games for datatables.
 */
class GamesDataFeed {
    /**
     * Int value to echo back.
     */
    int draw

    /**
     * Total number of unfiltered records.
     */
    int recordsTotal

    /**
     * Using the current filters, the total number of filtered records.
     */
    int recordsFiltered

    /**
     * Error.
     */
    String error

    /**
     * List of games for the current request.
     */
    List<Game> games

    /**
     * Base constructor.
     */
    GamesDataFeed() {
        games = []
    }
}
