package eu.domibus.test.earchiving
/**
 * Purpose of the class is to implement earchive specific assertions.
 */
class EArchiveAssert {

    static void assertEqualBatches(String exportPath, String expectedBatchId, String actualBatchId) {
        def expectedBatchFolder = assertBatchFolderStructureExists(exportPath, expectedBatchId)
        def actualBatchFolder = assertBatchFolderStructureExists(exportPath, actualBatchId)

        def expectedBatchDataList = []
        def actualBatchDataList = []

        expectedBatchFolder.eachDir { expectedBatchDataList << it.name }
        actualBatchFolder.eachDir { actualBatchDataList << it.name }
        expectedBatchDataList.sort()
        actualBatchDataList.sort()

        assert (expectedBatchDataList == actualBatchDataList), "Error: Content of actual batch (id: [$actualBatchId]) folder [${actualBatchDataList}] does not match expected batch batch (id: [$expectedBatchId]) folder [${expectedBatchDataList}]!"
    }

    /**
     *  Method asserts batch folder structure exists and returns data File object
     * @param exportPath - export batch
     * @param actualBatchId batch id
     * @return subfolder File object
     */
    static def assertBatchFolderStructureExists(String exportPath, String actualBatchId) {
        def path = new File(exportPath)
        assert (path.exists()), "Exported path [$path.absolutePath] not exists!"
        path = assertFolderExists("Assert batch folder.", path, actualBatchId)
        path = assertFolderExists("Assert batch representations folder.", path, EArchiveConstants.FOLDER_REPRESENTATIONS)
        path = assertFolderExists("Assert batch representation1 folder.", path, EArchiveConstants.FOLDER_REPRESENTATION1)
        return assertFolderExists("Assert batch data folder.", path, EArchiveConstants.FOLDER_DATA)
    }

    /**
     *  Method asserts exported batch contains messages
     * @param exportPath - export batch
     * @param actualBatchId batch id
     * @param list of batch exported messages
     */
    static void assertBatchContainsMessages(String exportPath, String actualBatchId, def messageIds) {
        def exportedFileNames = EArchiveUtils.getBatchExportedMessageFolders(exportPath, actualBatchId)
        exportedFileNames.sort();
        messageIds.sort();

        assert (messageIds == exportedFileNames), "Exported messageIDS [$exportedFileNames] does not match expected exported messageIDs[$messageIds]!"

    }

    static def assertBatchJSONExists(String exportPath, String actualBatchId) {
        def file = EArchiveUtils.getBatchJsonFile(exportPath, actualBatchId)
        return assertFileExists("Assert batch.json file!", file);
    }

    static def assertBatchMETSExists(String exportPath, String actualBatchId) {
        def file = EArchiveUtils.getBatchMETSFile(exportPath, actualBatchId)
        return assertFileExists("Assert METS.xml file!", file);
    }
    /**
     * Method asserts that subfolder exists in given parent folder
     * @param parentPath - parent folder
     * @param subfolder - child folder name
     * @return subfolder File object
     */
    static def assertFolderExists(message, File parentPath, String subfolder) {
        def path = new File(subfolder, parentPath);
        assert (path.exists()), message + " Subfolder [${subfolder}] for path [$path.absolutePath] doesn't exist!"
        assert (path.isDirectory()), message + " Subfolder ${subfolder} for path [$path.absolutePath] is not a folder!"
        return path;
    }
    /**
     * Method asserts that subfolder exists in given parent folder
     * @param parentPath - parent folder
     * @param subfolder - child folder name
     * @return subfolder File object
     */
    static def assertFileExists(String message, File path) {
        assert (path.exists()), message + " File [$path.absolutePath] doesn't exist!"
        assert (path.isFile()), message + "File [$path.absolutePath] is not a folder!"
        return path;
    }
}