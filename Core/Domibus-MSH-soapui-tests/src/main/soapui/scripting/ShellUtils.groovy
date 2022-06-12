import org.apache.log4j.Logger


class ShellUtils {
    def static LOG = Logger.getLogger(SoapUIConstants.SOAPUI_LOGGER_NAME)

    static def runCommandInShell(inputCommand) {
        LOG.debug("Calling \"runCommandInShell\".")
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def errorCode=-1;
        LOG.debug("runCommandInShell  [][]  Run curl command: " + inputCommand)
        if (inputCommand) {
            def proc = inputCommand.execute()
            if (proc != null) {
                proc.waitForProcessOutput(outputCatcher, errorCatcher)
                errorCode = proc.exitValue()
                LOG.info("runCommandInShell  [][]  Command: [$inputCommand]  finnished with exit code [$errorCode]")
            }
        }
        LOG.debug("runCommandInShell  [][]  outputCatcher: " + outputCatcher.toString())
        LOG.debug("runCommandInShell  [][]  errorCatcher: " + errorCatcher.toString())
        return ([outputCatcher.toString(), errorCatcher.toString(),errorCode])
    }
}
