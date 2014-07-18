package com.sony.ebs.octopus3.microservices.reposervice.test.integration

import com.jayway.restassured.http.ContentType
import com.sony.ebs.octopus3.commons.file.FileUtils
import groovyx.net.http.URIBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.groovy.test.TestHttpClient
import ratpack.groovy.test.TestHttpClients

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * author: tryavasu
 * date: 15/07/2014
 */
class RepoServiceIntegrationTest {

    Path TEST_FOLDER_PATH

    LocalScriptApplicationUnderTest aut
    @Delegate
    TestHttpClient client

    @Before
    void before() {
        def props = new Properties()
        new ClassPathResource("integration-test.properties").file.withInputStream {
            stream -> props.load(stream)
        }

        TEST_FOLDER_PATH = Paths.get(props["storage.root"] as String)

        if (Files.exists(TEST_FOLDER_PATH)) {
            FileUtils.delete(TEST_FOLDER_PATH)
        }

        aut = new LocalScriptApplicationUnderTest()
        client = TestHttpClients.testHttpClient(aut)
    }

    @Test
    void shouldWriteFile() {
        request.with {
            body(new ClassPathResource("test/integration/file1.txt").file.text)
            contentType(ContentType.TEXT)
        }
        post(new URIBuilder("//repository/file/urn:a:b:c").toString())

        assert 202 == response.statusCode

        Thread.sleep(10000)

        assertFile("/a/b/c", "content1")
    }

    @Test
    void shouldWriteConcurrentFile() {
        request.with {
            body(new ClassPathResource("test/integration/file1.txt").file.text)
            contentType(ContentType.TEXT)
        }
        post(new URIBuilder("//repository/file/urn:a:b:c").toString())

        assert 202 == response.statusCode

        request.with {
            body(new ClassPathResource("test/integration/file2.txt").file.text)
            contentType(ContentType.TEXT)
        }
        post(new URIBuilder("//repository/file/urn:a:b:c").toString())

        assert 202 == response.statusCode

        Thread.sleep(10000)

        //Assert second file is written
        assertFile("/a/b/c", "content2")
    }

    void assertFile(relativePath, content) {
        def path = Paths.get("$TEST_FOLDER_PATH${File.separator}${relativePath}")
        assertTrue "File does not exists in ${path.text}", Files.exists(path)
        assertEquals "The content of file [${path.text}] is wrong", content, path.readLines().join()
    }

    @After
    void tearDown() {
        FileUtils.delete(TEST_FOLDER_PATH)
        aut.stop()
    }
}
