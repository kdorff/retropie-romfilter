package retropie.romfilter

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        name listSystems: "/systems" {
            controller = 'system'
            action = 'listSystems'
        }
        name listRomsForSystem: "/system/$system" {
            controller = 'system'
            action = 'listRomsForSystem'
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
