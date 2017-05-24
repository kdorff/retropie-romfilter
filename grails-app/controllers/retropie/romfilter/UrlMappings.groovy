package retropie.romfilter

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: "games", action: "browse")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
