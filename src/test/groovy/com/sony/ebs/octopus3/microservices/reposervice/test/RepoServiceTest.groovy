package com.sony.ebs.octopus3.microservices.reposervice.test

import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.file.FileUtils
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import com.sun.javaws.exceptions.InvalidArgumentException
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
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
        repoService.write(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"), "content".bytes, null)
        assertFile "/flix_sku/global/en_gb/xel1bu", "content"
    }

    @Test
    void writeRegularFileWithExtension() {
        repoService.write(new URNImpl("urn:flix_sku:global:en_gb:xel1bu.json"), "content".bytes, null)
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
        FileUtils.with {
            writeFile(Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1ba"), "content".bytes, true, true)
            writeFile(Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1bu"), "content".bytes, true, true)
        }

        repoService.zip(new URNImpl("urn:flix_sku:global:en_gb")).with {
            assert getTracked().contains(Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1ba"))
            assert getTracked().contains(Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1bu"))
        }
    }

    @Test(expected = FileNotFoundException.class)
    void shouldGivErrorForZipIfNoFolder() {
        repoService.zip(new URNImpl("urn:flix_sku:global:km_km"))
    }

    @Test
    void shouldCopyFile() {
        FileUtils.writeFile(Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1ba"), "content".bytes, true, true)
        repoService.copy(new URNImpl("urn:flix_sku:global:en_gb:xel1ba"), new URNImpl("urn:flix_sku:global:en_gb:dsch300w"))

        assertFile "/flix_sku/global/en_gb/xel1ba", "content"
        assertFile "/flix_sku/global/en_gb/dsch300w/xel1ba", "content"
    }

    @Test(expected = FileNotFoundException.class)
    void shouldGiveErrorIfFileNotExists() {
        FileUtils.writeFile(Paths.get("$TEST_FOLDER_PATH/flix_sku/global/en_gb/xel1ba"), "content".bytes, true, true)
        repoService.copy(new URNImpl("urn:flix_sku:global:en_gb:nofile"), new URNImpl("urn:flix_sku:global:en_gb:dsch300w"))
    }

    @Test
    void readFileAttributes() {
        repoService.write(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"), "deneme".getBytes(), ISODateUtils.toISODate("1971-01-01T00:00:00.000Z"))
        def fileAttributes = repoService.getFileAttributes(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"))

        assertNotNull fileAttributes.creationTime
        assertFalse fileAttributes.directory
        assertNotNull fileAttributes.lastAccessTime
        assertTrue fileAttributes.regularFile
        assert fileAttributes.lastModifiedTime, "1971-01-01T00:00:00.000Z"
        assert fileAttributes.size, 6
    }

    @Test
    void readFolderAttributes() {
        repoService.write(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"), "deneme".getBytes(), ISODateUtils.toISODate("1971-01-01T00:00:00.000Z"))
        def fileAttributes = repoService.getFileAttributes(new URNImpl("urn:flix_sku:global:en_gb"))

        assertNotNull fileAttributes.creationTime
        assertTrue fileAttributes.directory
        assertNotNull fileAttributes.lastAccessTime
        assertFalse fileAttributes.regularFile
        assert fileAttributes.lastModifiedTime, "1971-01-01T00:00:00.000Z"
    }

    @Test(expected = FileNotFoundException.class)
    void readFileAttributesFileNotFound() {
        repoService.write(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"), "deneme".getBytes(), ISODateUtils.toISODate("1971-01-01T00:00:00.000Z"))

        repoService.getFileAttributes(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"))
    }

    @Test
    void renameFile() {
        repoService.write(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"), "deneme".getBytes(), null)

        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"), "othername")

        assertFile "/flix_sku/global/en_gb/othername", "deneme"
    }

    @Test(expected = FileNotFoundException.class)
    void renameFile_FileNotFound() {
        repoService.write(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"), "deneme".getBytes(), null)

        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), "othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName1() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), "\\othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName2() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), "/othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName3() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), "*othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName4() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), "?othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName5() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), ":othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName6() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), "\"othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName7() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), "<othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName8() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), ">othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName9() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), "|othername")
    }

    @Test(expected = InvalidArgumentException.class)
    void renameFile_invalidFileName10() {
        repoService.rename(new URNImpl("urn:flix_sku:global:en_gb:nonexistent"), " othername")
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