package com.sony.ebs.octopus3.microservices.reposervice.test

import com.sony.ebs.octopus3.commons.file.FileUtils
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class RepoServiceTest {

    final def static TEST_FOLDER_PATH = System.getProperty("java.io.tmpdir") + "/testPath"

    RepoService repoService

    @Before
    void before() {
        new File(TEST_FOLDER_PATH).delete()
        repoService = new RepoService(basePath: TEST_FOLDER_PATH)
    }

    @Test
    void writeRegularFile() {
        def urn = new URNImpl("urn:flix_sku:global:en_gb:xel1bu")
        repoService.write(urn, "content".bytes, null)
        assertFile "/flix_sku/global/en_gb/xel1bu", "content"
    }

    @Test
    void writeRegularFileWithExtension() {
        def urn = new URNImpl("urn:flix_sku:global:en_gb:xel1bu.json")
        repoService.write(urn, "content".bytes, null)
        assertFile "/flix_sku/global/en_gb/xel1bu.json", "content"
    }

    @Test
    void shouldReadRepoWithUrn() {
        FileUtils.writeFile(Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1ba"), "content".bytes, true, true)
        assertEquals "content", repoService.read(new URNImpl("urn:flix_sku:global:en_gb:xel1ba")).readLines().join()
    }

    @Test(expected = FileNotFoundException.class)
    void shouldGivErrorIfNoSku() {
        repoService.read(new URNImpl("urn:flix_sku:global:fr_fr:kdl200aq"))
    }

    @Test
    void shouldZipRepoWithUrn() {
        FileUtils.writeFile(Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1ba"), "content".bytes, true, true)
        FileUtils.writeFile(Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1bu"), "content".bytes, true, true)
        def zipResult = repoService.zip(new URNImpl("urn:flix_sku:global:en_gb"))

        assertEquals Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1ba"), zipResult.getTracked()[0]
        assertEquals Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1bu"), zipResult.getTracked()[1]
    }

    @Test(expected = FileNotFoundException.class)
    void shouldGivErrorForZipIfNoFolder() {
        repoService.zip(new URNImpl("urn:flix_sku:global:km_km"))
    }

    @After
    void tearDown() {
        FileUtils.delete(Paths.get(TEST_FOLDER_PATH + "/flix_sku/global/en_gb"))
    }

    static void assertFile(relativePath, content) {
        def path = Paths.get("$TEST_FOLDER_PATH${File.separator}${relativePath}")
        assertTrue "File does not exists in ${path.text}", Files.exists(path)
        assertEquals "The content of file [${path.text}] is wrong", content, path.readLines().join()
    }

}