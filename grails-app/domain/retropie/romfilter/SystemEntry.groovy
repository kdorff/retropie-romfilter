package retropie.romfilter

import groovy.transform.ToString

@ToString(includeNames = true)
class SystemEntry {
    String name

    static constraints = {
        name nullable: false, blank: false, index: 'systementry_name'
    }
}
