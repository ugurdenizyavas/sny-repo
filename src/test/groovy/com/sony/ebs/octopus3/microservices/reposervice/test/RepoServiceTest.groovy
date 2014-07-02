package com.sony.ebs.octopus3.microservices.reposervice.test

import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import org.apache.commons.lang.SystemUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class RepoServiceTest {

    final def static TEST_FOLDER_PATH = "tmp"

    RepoService repoService

    @Before
    void before() {
        repoService = new RepoService(basePath: TEST_FOLDER_PATH)
    }

    @Test
    void writeRegularFile() {
        def urn = new URNImpl("urn:flix_sku:global:en_gb:xel1bu")
        repoService.write(urn, "content")
        assertFile "/flix_sku/global/en_gb/xel1bu", "content"
    }

    @Test
    void writeRegularFileWithExtension() {
        def urn = new URNImpl("urn:flix_sku:global:en_gb:xel1bu.json")
        repoService.write(urn, "content")
        assertFile "/flix_sku/global/en_gb/xel1bu.json", "content"
    }

    @Test
    void shouldReadRepoWithUrn() {
        createFile "/flix_sku/global/en_gb/xel1bu", "content"
        assertEquals "content", repoService.read(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"))
    }

    @Test(expected = FileNotFoundException.class)
    void shouldGivErrorIfNoSku() {
        repoService.read(new URNImpl("urn:flix_sku:global:fr_fr:kdl200aq"))
    }

    @After
    void tearDown() {
        new File(TEST_FOLDER_PATH).deleteDir()
    }

    //====================================
    // HELPER METHODS
    //====================================

    static {
        String.metaClass.path { ->
            if (SystemUtils.IS_OS_WINDOWS) {
                def sb = new StringBuffer()
                delegate.each { sb << (it == "/" ? "\\" : "/") }
                sb.toString()
            }
            else
                delegate
        }
    }

    static void assertFile(relativePath, content) {
        def file = new File("$TEST_FOLDER_PATH${File.separator}${relativePath.path()}")
        assertTrue "File does not exists in ${file.absolutePath}", file.exists()
        assertEquals "The content of file [${file.text}] is wrong", content, file.text
    }

    static void createFile(relativePath, content) {
        def file = new File("$TEST_FOLDER_PATH${File.separator}${relativePath.path()}")
        file.parentFile.mkdirs()
        file << content
    }

}