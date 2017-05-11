package retropie.romfilter

import grails.util.TypeConvertingMap
import groovy.json.JsonSlurper

class ResourceService {
    /**
     * Load a JSON resource into a TypeConvertingMap.
     *
     * @param resourcePath the path to the resource
     * @return map for the json
     */
    TypeConvertingMap jsonResourceToMap(String resourcePath) {
        return deepTypeConvertingMap(
            (Map) new JsonSlurper().parseText(loadResource(resourcePath)))
    }

    /**
     * Load a file from a resource into a String.
     *
     * @param resourcePath the path to the resource
     * @return the resource as a String
     */
    String loadResource(String resourcePath) {
        String text = this.getClass().getResource(resourcePath)?.text
        return text
    }

    /**
     * Covert a map and all of it's sub-maps to TypeConvertingMap.
     *
     * @param map a map
     * @return map as a TypeConvertingMap
     */
    TypeConvertingMap deepTypeConvertingMap(Map map) {
        if (map == null) {
            return null
        }
        TypeConvertingMap tcMap = new TypeConvertingMap()
        map.keySet().each { k ->
            Object v = map[k]
            if (v instanceof Map) {
                // Convert the sub-map to be a TypeConvertingMap
                tcMap[k] = deepTypeConvertingMap(v)
            }
            else {
                tcMap[k] = v
            }
        }
        return tcMap
    }
}

