package retropie.romfilter

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        name listGames: "/games" {
            controller = 'games'
            action = 'index'
        }
        name gameDataFeed: "/games/feed" {
            controller = 'system'
            action = [POST: 'gamesDataFeed']
        }
        name showGame: "/games/$hash" {
            controller = 'games'
            action = 'showGame'
        }
        name showGameImage: "/games/$hash/image" {
            controller = 'games'
            action = 'showGameImage'
        }
        name deleteGame: "/roms/$hash/delete" {
            controller = 'games'
            action = [DELETE: 'deleteGame']
        }

        "/"(view:"/systems")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
