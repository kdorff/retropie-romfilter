package retropie.romfilter

import grails.core.GrailsApplication

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ConfigService {

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    List<String> getSystemsToSkip() {
        return grailsApplication.config.retropie.romfilter.skipSystems
    }

    List<String> getValidTypesSystem(String system) {
        return grailsApplication.config.retropie.emulationStation.system[system]?.validRomTypes ?: []
    }

    /**
     * Load a file from a resource into a String.
     *
     * @param resourcePath the path to the resource
     * @return the resource as a String
     */
    String getRomGlobForSystem(String system) {
        List<String> validTypes = getValidTypesSystem(system)
        String glob
        if (validTypes) {
            glob = '*.{' +  validTypes.join(',') + '}'
        }
        else {
            glob = "*"
        }
        return glob
    }

    /**
     * The gamelists path. This references a folder that contains one folder per system. Each of these
     * folders contains a file 'gamelist.xml'.
     *
     * @return
     */
    String getGamelistsPath() {
        return grailsApplication.config.retropie.emulationStation.gamelistsPath
    }

    /**
     * The gamelists path for a specific system. This references a folder that might contain a gamelist.xml
     * for the specified system.
     *
     * @param system
     * @return
     */
    String getGamelistPathForSystem(String system) {
        return "${gamelistsPath}/${system}/gamelist.xml"
    }

    /**
     * The rom path. This references a folder that contains one folder per system. Each of these
     * folders contains roms for that system.
     *
     * @param system
     * @return
     */
    String getRomsPath() {
        return grailsApplication.config.retropie.emulationStation.romsPath
    }

    /**
     * The roms path for a specific system.
     *
     * @param system
     * @return
     */
    String getRomsPathForSystem(String system) {
        return "${romsPath}/${system}"
    }

    /**
     * Return the images prefix. When scanned, this is placed before the system and filename. If
     * the whole image path is "~/.emulationstation/downloads/atari2600/blah.jpg" the prefix is
     * probably "~/.emulationstation/downloads/".
     *
     * @return
     */
    String getImagesPrefix() {
        return grailsApplication.config.retropie.emulationStation.imagesPrefix
    }

    /**
     * Return the images path.
     *
     * @return
     */
    String getImagesPath() {
        return grailsApplication.config.retropie.emulationStation.imagesPath
    }

    /**
     * Return the images path for a given system
     *
     * @param system
     * @return
     */
    String getImagesPathForSystem(String system) {
        return "${imagesPath}/${system}"
    }

    /**
     * Return the trash path.
     *
     * @return
     */
    String getTrashPath() {
        String trashPathStr = grailsApplication.config.retropie.romfilter.trashPath
        Path trashPath = Paths.get(trashPathStr)
        if (!Files.exists(trashPath)) {
            Files.createDirectories(trashPath)
        }
        return trashPathStr
    }

    /**
     * Return the trash path for a given system
     *
     * @param system
     * @return
     */
    String getTrashPathForSystem(String system) {
        String systemTrashPathStr = "${trashPath}/${system}"
        Path systemTrashPath = Paths.get(systemTrashPathStr)
        if (!Files.exists(systemTrashPath)) {
            Files.createDirectories(systemTrashPath)
        }
        return systemTrashPathStr
    }
}
