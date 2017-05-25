package retropie.romfilter

import org.apache.log4j.Logger

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ScanAllSystemsJob {
    def description = 'Submit scan jobs for all systems'
    def concurrent = false
    def sessionRequired = false

    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * ConfigService (auto-injected).
     */
    ConfigService configService

    /**
     * Startup or on demand. Scan all gamelist.xml files, up to one per system.
     */
    void execute(context) {
        long start = System.currentTimeMillis()
        log.info("Submitting scan jobs for all systems")
        Path gamelistsPath = Paths.get(configService.gamelistsPath)
        foldersContainedWithin(gamelistsPath).each { Path gamelistFolderPath ->
            ScanSystemJob.triggerNow([
                system: gamelistFolderPath.fileName,
            ])
        }
        log.info("Submitting system scan jobs complete. Took ${System.currentTimeMillis() - start}ms")
    }

    /**
     * Return all of the FOLDERS in the specified folder (1 level deep).
     *
     * @param folder folder to search
     * @return list of Path for folders
     */
    List<Path> foldersContainedWithin(Path folder) {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(folder)
            List<Path> paths = stream.findAll { Path path ->
                return Files.isDirectory(path) && !Files.isHidden(path)
            }
            return paths
        } catch (IOException e) {
            log.error("Exception scanning systems folder", e)
            return []
        }
    }
}
