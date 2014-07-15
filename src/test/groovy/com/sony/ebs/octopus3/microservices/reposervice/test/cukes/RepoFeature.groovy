package com.sony.ebs.octopus3.microservices.reposervice.test.cukes

import com.jayway.restassured.http.ContentType
import cucumber.api.PendingException
import cucumber.api.groovy.EN
import cucumber.api.groovy.Hooks
import groovyx.net.http.URIBuilder
import org.codehaus.jackson.map.JsonMappingException
import org.codehaus.jackson.map.ObjectMapper
import org.springframework.core.io.ClassPathResource
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.groovy.test.TestHttpClient
import ratpack.groovy.test.TestHttpClients

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
    request.contentType(ContentType.TEXT)
    request.body(content)

    post("repository/file/${urn}")
}

When(~'I write (.*) for content (.*) as if in date (.*)') { String urn, String content, String updateDate ->
    resetRequest()
    request.contentType(ContentType.TEXT)
    request.body(content)

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
    request.contentType(ContentType.TEXT)
    request.body(new ClassPathResource(filePath).file.text)

    post("repository/ops")
}

When(~'I ask for delta for (.*) for start date (.*) and end date (.*)') { urn, sdate, edate ->
    resetRequest()
    def uri = new URIBuilder("//repository/delta/${urn}")
    uri.addQueryParam("sdate", sdate == "null" ? null : sdate)
    uri.addQueryParam("edate", edate == "null" ? null : edate)
    get(uri.toString())
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

//=======================================
// HELPER METHODS
//=======================================

compareJsons = { String json1, String json2 ->
    assert objectMapper.readTree(json1).equals(objectMapper.readTree(json2))
}

validateJson = { String content ->
    try {
        def object = objectMapper.readTree(content)
        assert object != null: "Incoming response content seems to be empty. It is " + content
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
        def object = objectMapper.readTree(content)
        return object != null
    } catch (Exception e) {
        return false
    }
}