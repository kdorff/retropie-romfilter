package retropie.romfilter

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.apache.log4j.Logger
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.core.io.Resource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.env.PropertiesPropertySource

class Application extends GrailsAutoConfiguration implements EnvironmentAware {

    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    /**
     * Incorporate external config, if it exists.
     *
     * @param environment
     */
    @Override
    void setEnvironment(Environment environment) {
        String configFilename = "${System.getenv().HOME}/.retropie-romfilter.yml"
        File configFile = new File(configFilename)
        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            try {
                Resource resourceConfig = new FileSystemResource(configFilename)
                YamlPropertiesFactoryBean ypfb = new YamlPropertiesFactoryBean()
                ypfb.setResources(resourceConfig)
                ypfb.afterPropertiesSet();
                Properties properties = ypfb.getObject()
                environment.propertySources.addFirst(new PropertiesPropertySource("external-config", properties))
            } catch (Exception e) {
                log.error("Error loading external configuration", e)
            }
        }
        else {
            log.warn("Warning, optional external configuration file missing ${configFilename}")
        }
    }
}