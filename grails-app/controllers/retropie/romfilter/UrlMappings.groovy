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
        name romsDataFeed: "/system/$system/feed" {
            controller = 'system'
            action = [POST: 'romsDataFeed']
        }
        name romForSystem: "/system/$system/rom/$hash" {
            controller = 'system'
            action = 'showRomForSystem'
        }
        name deleteRomForSystem: "/system/$system/rom/$hash/delete" {
            controller = 'system'
            action = [DELETE: 'deleteRomForSystem']
        }
        name showRomImageForSystem: "/system/$system/rom/$hash/image" {
            controller = 'system'
            action = 'showRomImageForSystem'
        }

        "/"(view:"/systems")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
