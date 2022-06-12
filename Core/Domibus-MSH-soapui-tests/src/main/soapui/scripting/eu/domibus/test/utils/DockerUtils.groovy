package eu.domibus.test.utils

import org.apache.log4j.Logger

/**
 * This is utility class managing the docker containers for the test environment. If the script is run from inside the container
 * The container must bind /var/run/docker.sock to host as example
 *  volumes:
 *       # bind docker socks to internal docker
 *       - type: bind
 *         source: /var/run/docker.sock
 *         target: /var/run/docker.sock
 *         read_only: true
 * Be careful with this, because container can now manage the host docker!! Use this only for testing.
 *
 */
class DockerUtils {
    def static LOG = Logger.getLogger(LogUtils.SOAPUI_LOGGER_NAME)
    // fake time filepath accessible from SOAPUI container (https://github.com/wolfcw/libfaketime)
    static final String FAKETIME_FILEPATH = "/tmp/c2/faketime/faketimerc"
    // backup and restore database script paths from database container!
    static final String DB_BACKUP_SCRIPT_FILEPATH = "/usr/local/bin/backup.sh"
    static final String DB_BACKUP_CMD_RESTORE = "restore"
    static final String DB_BACKUP_CMD_BACKUP = "backup"
    static final String DB_BACKUP_CMD_EXISTS = "exists"

    /**
     *  Set system date for "not clustered" C2/C3 side
     * @param side c2 or c3 point.
     * @param dateShift - shift dates as -15d See the notation from here: https://github.com/wolfcw/libfaketime
     * @param log - soapui log4j object
     * @param context - soapui context
     */
    static boolean setSystemDate(String side, String dateShift, context) {
        String containerName = PropertyUtils.getSTDomibusContainerName(side, context)
        String domibusUrl = PropertyUtils.urlToDomibus(side, context)

        stopContainer(containerName)
        def filename = FAKETIME_FILEPATH
        def file = new File(filename)
        file.newWriter().withWriter { w ->
            w << dateShift
        }
        startContainer(containerName)
        return CurlUtils.waitForUrlAccessible(domibusUrl)
    }

    /**
     * Restart container for C2/C3 side
     * @param side - access point "side" example: "c2 or c3". The value is use with allDomainsProperties upper(${side})Default as example C2Default
     * @param log soapui log4j object
     * @param context
     * @return
     */
    static boolean restartContainer(String side, context) {
        String containerName = PropertyUtils.getSTDomibusContainerName(side, context)
        String domibusUrl = PropertyUtils.urlToDomibus(side, context)
        executeDockerCommand("restart", containerName)
        return CurlUtils.waitForUrlAccessible(domibusUrl)
    }

    /**
     * @param side - access point "side" example: "c2 or c3". The value is use with allDomainsProperties upper(${side})Default as example C2Default
     * @param containerName - container name
     * @param log soapui log4j object
     * @return
     */
    static def stopContainer(String containerName) {
        return executeDockerCommand("stop", containerName)
    }

    /**
     * Start container for C2/C3 side. The command executed shell docker command to start container
     * @param containerName - container name
     * @param log soapui log4j object
     * @return
     */
    static def startContainer(String containerName) {
        return executeDockerCommand("start", containerName)
    }

    /**
     * Generic docker command. Method execute docker "dockerCommand" for container
     * @param dockerCommand - docker command as: start, stop, restart...
     * @param containerName - container name
     * @param log - soapui log4j object
     * @return
     */
    static def executeDockerCommand(String dockerCommand, String containerName) {
        def commandString = ["docker", dockerCommand, containerName]
        LOG.info("executeDockerCommand: [$commandString]!")
        return ShellUtils.runCommandInShell(commandString)
    }

    /**
     * Execute script via "docker exec" to export/backup database
     * @param side - access point "side" example: "c2 or c3". The value is use with allDomainsProperties upper(${side})Default as example C2Default
     * @param backupName - backup name set on database container.
     * @param log - soapui log4j object
     * @param context soapui context object
     * @return
     */
    static def backupMysqlDB(String side, String backupName, context) {
        String containerName = PropertyUtils.getSTDatabaseContainerName(side, context)
        LOG.info("  backupMysqlDB  [][]  for container: " + containerName + " and backup name: " + backupName)
        def commandString = ["docker", "exec", containerName, DB_BACKUP_SCRIPT_FILEPATH, DB_BACKUP_CMD_BACKUP, backupName]
        return ShellUtils.runCommandInShell(commandString)
    }

    /**
     * Execute script via "docker exec" to restore database from backup
     * @param side - access point "side" example: "c2 or c3". The value is use with allDomainsProperties upper(${side})Default as example C2Default
     * @param backupName - backup name to restore database.
     * @param log - soapui log4j object
     * @param context soapui context object
     * @return
     */
    static def restoreMysqlDB(String side, String backupName, context) {
        String containerName = PropertyUtils.getSTDatabaseContainerName(side, context)
        LOG.info("  restoreMysqlDB  [][]  for container: " + containerName + " and backup name: " + backupName)
        def commandString = ["docker", "exec", containerName, DB_BACKUP_SCRIPT_FILEPATH, DB_BACKUP_CMD_RESTORE, backupName]
        return ShellUtils.runCommandInShell(commandString)
    }
    /**
     * Execute script via "docker exec" to restore database from backup
     * @param side - access point "side" example: "c2 or c3". The value is use with allDomainsProperties upper(${side})Default as example C2Default
     * @param backupName - backup name to restore database.
     * @param log - soapui log4j object
     * @param context soapui context object
     * @return
     */
    static def backupExistsMysqlDB(String side, String backupName, context) {
        String containerName = PropertyUtils.getSTDatabaseContainerName(side, context)
        LOG.info("  backupExistsMysqlDB  [][]  for container: " + containerName + " and backup name: " + backupName)
        def commandString = ["docker", "exec", containerName, DB_BACKUP_SCRIPT_FILEPATH, DB_BACKUP_CMD_EXISTS, backupName]
        // of exists backup script returns 0 else -1
        return ShellUtils.runCommandInShell(commandString)[2] == 0
    }
}