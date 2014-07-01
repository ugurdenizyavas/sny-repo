package com.sony.ebs.octopus3.microservices.reposervice.test

import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

class RepoServiceTest {

    final def TEST_FOLDER_PATH = "tmp"
    final def SEPARATOR = File.separator

    RepoService repoService

    @Before
    void before() {
        repoService = new RepoService(basePath: TEST_FOLDER_PATH)
        new File(TEST_FOLDER_PATH).delete()

        repoService.write("urn:flix_sku:global:en_gb:xel1bu", "deneme")
        repoService.write("urn:flix_sku:global:en_gb:xel1baep", "deneme2")
        repoService.write("urn:flix_sku:global:fr_fr:xel1bu", "deneme3")
        repoService.write("urn:flix_sku:global:fr_fr:xel1baep", "deneme4")
    }

    @Test
    void shouldReadRepoWithUrn() {
        assert repoService.read("urn:flix_sku:global:en_gb:xel1bu") == Paths.get("${TEST_FOLDER_PATH}${SEPARATOR}flix_sku${SEPARATOR}global${SEPARATOR}en_gb${SEPARATOR}xel1bu")
        assert repoService.read("urn:flix_sku:global:en_gb:xel1baep") == Paths.get("${TEST_FOLDER_PATH}${SEPARATOR}flix_sku${SEPARATOR}global${SEPARATOR}en_gb${SEPARATOR}xel1baep")
        assert repoService.read("urn:flix_sku:global:fr_fr:xel1bu") == Paths.get("${TEST_FOLDER_PATH}${SEPARATOR}flix_sku${SEPARATOR}global${SEPARATOR}fr_fr${SEPARATOR}xel1bu")
        assert repoService.read("urn:flix_sku:global:fr_fr:xel1baep") == Paths.get("${TEST_FOLDER_PATH}${SEPARATOR}flix_sku${SEPARATOR}global${SEPARATOR}fr_fr${SEPARATOR}xel1baep")
    }

    @Test(expected = FileNotFoundException.class)
    void shouldGivErrorIfNoSku() {
        repoService.read("urn:flix_sku:global:fr_fr:kdl200aq")
    }

    @After
    void tearDown() {
        new File(TEST_FOLDER_PATH).delete()
    }
}