package retropie.romfilter

import grails.core.GrailsApplication

class SystemController {

    GrailsApplication grailsApplication

    def listSystems() {
        println "Listing systems"
        return [
            systems: grailsApplication.config.retropie.romfilter.systems
        ]
    }

    def listRomsForSystem(String system) {
        println "Listing roms for ${system}"
        List<String> gamelist = ['hi.zip', 'there.zip', "woot.zip"]
        Map<String, Map> filenameToDetails = [
            'hi.zip': [
                path: 'hi.zip',
                name: 'hi',
                image: './somepath.jpg',
                desc: 'description of hi',
                hasImg: true,
                genre: 'genre of hi',
                size: 100,
            ],
            'there.zip': [
                path: 'there.zip',
                name: 'there',
                image: '',
                desc: 'description of there',
                hasImg: false,
                genre: 'genre of there',
                size: 100,
            ]
        ]
        return [
            system: system,
            gamelist: gamelist,
            filenameToDetails: filenameToDetails,
        ]
    }
}
