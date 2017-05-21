package retropie.romfilter

class GameService {

    String namePathComparison(String name, String path) {
        if (!name || !path) {
            return null
        }
        return "${name}<br/>${path}"
    }

}
