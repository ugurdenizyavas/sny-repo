package com.sony.ebs.octopus3.microservices.reposervice.test

import com.sony.ebs.octopus3.microservices.reposervice.business.DeltaService
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test

/**
 * author: TRYavasU
 * date: 25/06/2014
 */
class DeltaServiceTest {

    final def TEST_FOLDER_PATH = "tmp"

    RepoService repoService
    DeltaService deltaService

    @Before
    void before() {
        repoService = new RepoService(basePath: TEST_FOLDER_PATH)
        deltaService = new DeltaService(basePath: TEST_FOLDER_PATH)
        new File(TEST_FOLDER_PATH).delete()

        repoService.write("urn:flix_sku:global:en_gb:xel1bu", "deneme").toFile().setLastModified(new GregorianCalendar(1970, Calendar.JANUARY, 2).timeInMillis)
        repoService.write("urn:flix_sku:global:en_gb:xel1baep", "deneme2").toFile().setLastModified(new GregorianCalendar(1980, Calendar.JANUARY, 2).timeInMillis)
        repoService.write("urn:flix_sku:global:fr_fr:xel1bu", "deneme3").toFile().setLastModified(new GregorianCalendar(1980, Calendar.JANUARY, 2).timeInMillis)
        repoService.write("urn:flix_sku:global:fr_fr:xel1baep", "deneme4").toFile().setLastModified(new GregorianCalendar(2000, Calendar.JANUARY, 1).timeInMillis)
    }

    @Test
    void shouldWork() {
        def contents = deltaService.delta("urn:flix_sku:global:fr_fr", "1/1/1990T00:00Z")
        assert contents.size() == 1
    }

    @Test
    void shouldReturnNoResult() {
        def contents = deltaService.delta("urn:flix_sku:global:en_gb", "1/1/2010T00:00Z")
        assert contents.size() == 0
    }

    @Test
    void shouldReadFromRepoWithoutDeltaDate() {
        def contents = deltaService.delta("urn:flix_sku:global:fr_fr", null)

        assert contents.size() == 2

        assert contents[0] == "urn:flix_sku:global:fr_fr:xel1baep"
        assert contents[1] == "urn:flix_sku:global:fr_fr:xel1bu"
    }


    @AfterClass
    void tearDown() {
        new File(TEST_FOLDER_PATH).delete()
    }

}