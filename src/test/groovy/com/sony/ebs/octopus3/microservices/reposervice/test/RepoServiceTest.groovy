package com.sony.ebs.octopus3.microservices.reposervice.test

import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import org.apache.commons.lang.SystemUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class RepoServiceTest {

    final def static TEST_FOLDER_PATH = "tmp"

    RepoService repoService

    @Before
    void before() {
        new File(TEST_FOLDER_PATH).delete()
        repoService = new RepoService(basePath: TEST_FOLDER_PATH)
    }

    @Test
    void writeRegularFile() {
        def urn = new URNImpl("urn:flix_sku:global:en_gb:xel1bu")
        repoService.write(urn, "content", null)
        assertFile "/flix_sku/global/en_gb/xel1bu", "content"
    }

    @Test
    void writeRegularFileWithExtension() {
        def urn = new URNImpl("urn:flix_sku:global:en_gb:xel1bu.json")
        repoService.write(urn, "content", null)
        assertFile "/flix_sku/global/en_gb/xel1bu.json", "content"
    }

    @Test
    void shouldReadRepoWithUrn() {
        createFile "/flix_sku/global/en_gb/xel1ba", "content"
        assertEquals "content", repoService.read(new URNImpl("urn:flix_sku:global:en_gb:xel1ba")).readLines().join()
    }

    @Test(expected = FileNotFoundException.class)
    void shouldGivErrorIfNoSku() {
        repoService.read(new URNImpl("urn:flix_sku:global:fr_fr:kdl200aq"))
    }

    @After
    void tearDown() {
        def folder = Paths.get(TEST_FOLDER_PATH + "/flix_sku/global/en_gb")
        if (Files.exists(folder))
            RepoService.removeRecursive(folder)
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
            } else
                delegate
        }
    }

    static void assertFile(relativePath, content) {
        def path = Paths.get("$TEST_FOLDER_PATH${File.separator}${relativePath}")
        assertTrue "File does not exists in ${path.text}", Files.exists(path)
        assertEquals "The content of file [${path.text}] is wrong", content, path.readLines().join()
    }

    static void createFile(relativePath, content) {
        def path = Paths.get("$TEST_FOLDER_PATH${File.separator}${relativePath}")
        Files.createDirectories(path.parent)
        def file = Files.createFile(path)
        file << content
    }

}