import groovy.io.FileType

class TruststoreUtils {

    def context = null
    def log = null
    static def TRUSTSTORE_PASSWORD = "test123"

    TruststoreUtils(log, context) {
        this.context = context
        this.log = log
    }

    /**
     * Uploads truststore file on Domibus 'side' server
     * @param side
     * @param baseFilePath
     * @param extFilePath
     * @param context
     * @param log
     * @param domainValue
     * @param outcome
     * @param tsPassword
     * @param authUser
     * @param authPwd
     * @return
     */
    //---------------------------------------------------------------------------------------------------------------------------------
    static def uploadTruststore(String side, String baseFilePath, String extFilePath, context, log, String domainValue = "Default", String outcome = "successfully", String tsPassword = TRUSTSTORE_PASSWORD, String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"uploadTruststore\".", log)
        log.info "  uploadTruststore  [][]  Start upload truststore for Domibus \"" + side + "\"."
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        try{
            LogUtils.debugLog("  uploadTruststore  [][]  Fetch multitenancy mode on domibus $side.", log)
            (authenticationUser, authenticationPwd) = Domibus.retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            String truststoreFile = Domibus.computePathRessources(baseFilePath,extFilePath,context,log)

            def commandString = ["curl", Domibus.urlToDomibus(side, log, context) + "/rest/truststore/save",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + Domibus.returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-F", "password=" + tsPassword,
                                 "-F", "file=@" + truststoreFile,
                                 "-v"]
            def commandResult = Domibus.runCommandInShell(commandString, log)

            assert(commandResult[0].contains(outcome)),"Error:uploadTruststore: Error while trying to upload the truststore to domibus. Returned: " + commandResult[0]
            log.info "  uploadTruststore  [][]  " + commandResult[0] + " Domibus: \"" + side + "\"."
        } finally {
            Domibus.resetAuthTokens(log)
        }
    }

    /**
     * The method creates a new keystore. The name of the keystore will be "gateway_keystore.jks" unless the optional domain name argument is provided
     * in this case the name of the keystore will be "gateway_keystore_DOMAIN.jks"
     * @param context
     * @param log
     * @param workingDirectory
     * @param keystoreAlias
     * @param keystorePassword
     * @param privateKeyPassword
     * @param validityOfKey
     * @param keystoreFileName
     * @return
     */
    static def generateKeyStore(context, log, workingDirectory, keystoreAlias, keystorePassword, privateKeyPassword, validityOfKey = 300, keystoreFileName = "gateway_keystore.jks") {

        assert (keystoreAlias?.trim()), "Please provide the alias of the keystore entry as the 3rd parameter (e.g. 'red_gw', 'blue_gw'}"
        assert (keystorePassword?.trim()), "Please provide keystore password"
        assert (privateKeyPassword?.trim()), "Please provide not empty private key password"

        log.info """Generating keystore using: 
        keystoreAlias = ${keystoreAlias},  
        keystorePassword = ${keystorePassword}, 
        privateKeyPassword = ${privateKeyPassword}, 
        keystoreFileName = ${keystoreFileName}, 
        validityOfKey = ${validityOfKey}"""

        def keystoreFile = workingDirectory + keystoreFileName
        log.info keystoreFile

        def startDate = 0
        def defaultValidity = 1 // 1 days is minimal validity for Key and Certificate Management Tool - keytool
        if (validityOfKey <= 0) {
            startDate = validityOfKey - defaultValidity
            validityOfKey = defaultValidity
        }

        def commandString =  ["keytool", "-genkeypair",
                              "-dname",  "C = BE,O = eDelivery,CN = ${keystoreAlias}",
                              "-alias", "${keystoreAlias}",
                              "-keyalg", "RSA",
                              "-keysize", "2048",
                              "-keypass", "${privateKeyPassword}",
                              "-validity", validityOfKey.toString(),
                              "-storetype", "JKS",
                              "-keystore", "${keystoreFile}",
                              "-storepass", "${keystorePassword}" ,
                              "-v"]
        if (startDate != 0)
            commandString << "-startdate" << startDate.toString() + "d"

        def commandResult = Domibus.runCommandInShell(commandString, log)
        assert!(commandResult[0].contains("error")),"Error: Output of keytool execution, generating key, should not contain an error. Returned message: " +  commandResult[0] + "||||" +  commandResult[1]

        def pemPath = workingDirectory + returnDefaultPemFileName(keystoreFileName, keystoreAlias)
        def pemFile = new File(pemPath)

        assert !(pemFile.exists()), "The certificate file: ${pemPath} shouldn't already exist"

        commandString =  ["keytool", "-exportcert",
                          "-alias", "${keystoreAlias}",
                          "-file", pemPath,
                          "-keystore", "${keystoreFile}",
                          "-storetype", "JKS",
                          "-storepass", "${keystorePassword}",
                          "-rfc", "-v"]

        commandResult = Domibus.runCommandInShell(commandString, log)
        assert!(commandResult[0].contains("error")),"Error: Output of keytool execution, generating *.pem file, should not contain an error. Returned message: " +  commandResult[0] + "||" +  commandResult[1]

        pemFile = new File(pemPath)
        pemFile.setWritable(true)

    }

    /**
     * This shared method is used for creating pem filename
     * @param keystoreFileName
     * @param keystoreAlias
     * @return
     */
    static String returnDefaultPemFileName(String keystoreFileName, String keystoreAlias) {
        return "${keystoreFileName}_${keystoreAlias}.pem"
    }

    /**
     * This method imports an existing public-key certificate into a truststore. If the truststore is missing, it will be created.
     * The name of the truststore chosen as destination will be "gateway_truststore.jks" unless the optional truststoreFileName
     * argument is provided - in this case the name of the truststore used will be exactly as provided truststoreFileName
     * (you need to include extension, example value "gateway_truststore_domain1.jks")
     * @param context
     * @param log
     * @param workingDirectory
     * @param keystoreAlias
     * @param keystorePassword
     * @param privateKeyPassword
     * @param keystoreFileName
     * @param truststoreFileName
     * @return
     */
    static def updateTrustStore(context, log, workingDirectory, keystoreAlias, keystorePassword, privateKeyPassword, keystoreFileName, truststoreFileName = "gateway_truststore.jks") {

        assert (keystoreAlias?.trim()), "Please provide the alias of the keystore entry as the 3rd parameter (e.g. 'red_gw', 'blue_gw'}"
        assert (keystorePassword?.trim()), "Please provide keystore password"
        assert (privateKeyPassword?.trim()), "Please provide not empty private key password"

        log.info """Updating truststore using: 
        keystoreAlias = ${keystoreAlias}, 
        keystorePassword = ${keystorePassword}, 
        privateKeyPassword = ${privateKeyPassword}, 
        truststoreFileName = ${truststoreFileName}, 
        keystoreFileName = ${keystoreFileName}"""

        def truststoreFile = workingDirectory  + truststoreFileName
        def pemFilePath = workingDirectory  + returnDefaultPemFileName(keystoreFileName, keystoreAlias)

        def pemFile = new File(pemFilePath)
        assert (pemFile.exists()), "The certificate ${pemFile} shouldn't already exist"

        def commandString =  ["keytool", "-importcert",
                              "-alias", "${keystoreAlias}",
                              "-file", pemFilePath,
                              "-keypass", "${privateKeyPassword}",
                              "-keystore", truststoreFile,
                              "-storetype", "JKS",
                              "-storepass", "${keystorePassword}",
                              "-noprompt ", "-v"]

        def commandResult = Domibus.runCommandInShell(commandString, log)
        assert!(commandResult[0].contains("error")),"Error: Output of keytool execution, importing *.pem data to truststre, should not contain an error. Returned message: " +  commandResult[0] + "||" +  commandResult[1]

        def trustFile = new File(truststoreFile)
        trustFile.setWritable(true)
    }

}
