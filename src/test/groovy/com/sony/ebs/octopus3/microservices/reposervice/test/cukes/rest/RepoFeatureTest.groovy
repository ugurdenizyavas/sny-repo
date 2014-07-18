package com.sony.ebs.octopus3.microservices.reposervice.test.cukes.rest

import com.jayway.restassured.http.ContentType
import com.sony.ebs.octopus3.commons.urn.URNImpl
import cucumber.api.groovy.EN
import cucumber.api.groovy.Hooks
import groovyx.net.http.URIBuilder
import org.codehaus.jackson.map.ObjectMapper
import org.springframework.core.io.ClassPathResource
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.groovy.test.TestHttpClient
import ratpack.groovy.test.TestHttpClients

import java.nio.file.Paths

this.metaClass.mixin(Hooks)
this.metaClass.mixin(EN)

System.setProperty 'environment', 'cucumber'
ObjectMapper objectMapper = new ObjectMapper()

class LocalRatpackWorld {

    LocalScriptApplicationUnderTest aut = new LocalScriptApplicationUnderTest()

    @Delegate
    TestHttpClient client = TestHttpClients.testHttpClient(aut)
}

World {
    new LocalRatpackWorld()
}

Before() {
    delete("repository/file/urn:cucumber:a")
}

After() {
    aut.stop()
}

//=======================================
// GIVEN
//=======================================

Given(~'I delete (.*)') { String urn ->
    delete("repository/file/${urn}")
}

//=======================================
// WHEN
//=======================================

When(~'I write (.*) for content (.*) with current date') { String urn, String content ->
    resetRequest()
    request.with {
        contentType(ContentType.TEXT)
        body(content)
    }

    post("repository/file/${urn}")
}

When(~'I write (.*) for content (.*) as if in date (.*)') { String urn, String content, String updateDate ->
    resetRequest()
    request.with {
        contentType(ContentType.TEXT)
        body(content)
    }

    def uri = new URIBuilder("//repository/file/${urn}")
    uri.addQueryParam("updateDate", updateDate == "null" ? null : updateDate)
    post(uri.toString())
}

When(~'I read (.*)') { String urn ->
    resetRequest()
    get("repository/file/${urn}")
}

When(~'I zip (.*)') { String urn ->
    resetRequest()
    get("repository/zip/${urn}")
}

When(~'I copy (.*) to (.*)') { String sourceUrn, String destinationUrn ->
    resetRequest()
    get("repository/copy/source/${sourceUrn}/destination/${destinationUrn}")
}

When(~'I upload (.*) to (.*)') { String sourceUrn, String destination ->
    resetRequest()
    get("repository/upload/source/${sourceUrn}/destination/${destination}")
}

When(~'I use operation in file (.*)') { String filePath ->
    resetRequest()
    request.with {
        contentType(ContentType.TEXT)
        body(new ClassPathResource(filePath).file.text)
    }

    post("repository/ops")
}

When(~'I ask for delta for (.*) for start date (.*) and end date (.*)') { urn, sdate, edate ->
    resetRequest()
    get(
            new URIBuilder("//repository/delta/${urn}").with {
                addQueryParam("sdate", sdate == "null" ? null : sdate)
                addQueryParam("edate", edate == "null" ? null : edate)
            }.toString()
    )
}

When(~'I wait for (.*) seconds') { seconds ->
    Thread.sleep(seconds as Integer)
}

//=======================================
// THEN
//=======================================

Then(~'Response is (.*) and response body is (.*)') { responseCode, String content ->
    switch (responseCode) {
        case "OK":
            assert 200 == response.statusCode
            break
        case "Created":
            assert 201 == response.statusCode
            break
        case "Accepted":
            assert 202 == response.statusCode
            break
        case "Rejected":
            assert 400 == response.statusCode
            break
        case "Not Found":
            assert 404 == response.statusCode
            break
        default: assert false
    }
}

Then(~'Delta response is (.*)') { String responseBody ->
    compareData responseBody, response.body.asString()
}

Then(~'File is at (.*) and its content is (.*)') { String path, String content ->
    resetRequest()
    get("repository/file/${new URNImpl(null, Paths.get(path))}")

}

//=======================================
// HELPER METHODS
//=======================================

compareJsons = { String json1, String json2 ->
    assert objectMapper.readTree(json1).equals(objectMapper.readTree(json2))
}

validateJson = { String content ->
    try {
        assert objectMapper.readTree(content) != null: "Incoming response content seems to be empty. It is " + content
    } catch (Exception e) {
        assert false: "Incoming response is not a json document. It is " + content
    }
}

compareData = { data1, data2 ->
    if (isJson(data2)) {
        validateJson(data2)
        compareJsons(data1, data2)
    } else {
        assert data1 == data2
    }
}

isJson = { String content ->
    try {
        return objectMapper.readTree(content) != null
    } catch (Exception e) {
        return false
    }
}