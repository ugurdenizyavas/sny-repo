package com.sony.ebs.octopus3.microservices.reposervice.test

import com.sony.ebs.octopus3.commons.date.ISODateUtils
import com.sony.ebs.octopus3.commons.file.FileUtils
import com.sony.ebs.octopus3.commons.urn.URNImpl
import com.sony.ebs.octopus3.microservices.reposervice.business.DeltaService
import com.sony.ebs.octopus3.microservices.reposervice.business.RepoService
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

/**
 * author: TRYavasU
 * date: 25/06/2014
 */
class DeltaServiceTest {

    final def TEST_FOLDER_PATH = System.getProperty("java.io.tmpdir") + "/testPath"

    RepoService repoService
    DeltaService deltaService

    @Before
    void before() {
        repoService = new RepoService(basePath: TEST_FOLDER_PATH)
        deltaService = new DeltaService(basePath: TEST_FOLDER_PATH)
        new File(TEST_FOLDER_PATH).delete()

        repoService.with {
            write(new URNImpl("urn:flix_sku:global:en_gb:xel1bu"), "deneme".getBytes(), ISODateUtils.toISODate("1971-01-01T00:00:00.000Z"))
            write(new URNImpl("urn:flix_sku:global:en_gb:xel1baep"), "deneme2".getBytes(), ISODateUtils.toISODate("1980-01-01T00:00:00.000Z"))
            write(new URNImpl("urn:flix_sku:global:en_gb:xel1ba54"), "deneme3".getBytes(), ISODateUtils.toISODate("1990-01-01T00:00:00.000Z"))
        }
    }

    @Test
    void shouldWorkWithStartAndEndDate() {
        def contents = deltaService.delta(new URNImpl("urn:flix_sku:global:en_gb"), ISODateUtils.toISODate("1975-01-01T00:00:00.000Z"), ISODateUtils.toISODate("1985-01-01T00:00:00.000Z"))
        assert contents.size == 1

        assert contents.contains("urn:flix_sku:global:en_gb:xel1baep")
    }

    @Test
    void shouldWorkWithStartDate() {
        def contents = deltaService.delta(new URNImpl("urn:flix_sku:global:en_gb"), ISODateUtils.toISODate("1975-01-01T00:00:00.000Z"), null)
        assert contents.size == 2

        assert contents.contains("urn:flix_sku:global:en_gb:xel1ba54")
        assert contents.contains("urn:flix_sku:global:en_gb:xel1baep")
    }

    @Test
    void shouldWorkWithEndDate() {
        def contents = deltaService.delta(new URNImpl("urn:flix_sku:global:en_gb"), null, ISODateUtils.toISODate("1985-01-01T00:00:00.000Z"))
        assert contents.size == 2

        assert contents.contains("urn:flix_sku:global:en_gb:xel1baep")
        assert contents.contains("urn:flix_sku:global:en_gb:xel1bu")
    }

    @Test
    void shouldReadFromRepoWithoutStartAndEndDate() {
        def contents = deltaService.delta(new URNImpl("urn:flix_sku:global:en_gb"), null, null)

        assert contents.size == 3

        assert contents.contains("urn:flix_sku:global:en_gb:xel1ba54")
        assert contents.contains("urn:flix_sku:global:en_gb:xel1baep")
        assert contents.contains("urn:flix_sku:global:en_gb:xel1bu")
    }

    @After
    void tearDown() {
        FileUtils.delete(Paths.get(TEST_FOLDER_PATH + "/flix_sku/global/en_gb"), false)
    }

}