package retropie.romfilter.feed

import retropie.romfilter.indexed.RomEntry

/**
 * Created by kevi9037 on 5/18/17.
 */
class RomsDataFeed {
    int draw
    int recordsTotal
    int recordsFiltered
    String error
    List<RomEntry> roms

    RomsDataFeed() {
    }
}
