package com.sony.ebs.octopus3.microservices.reposervice.test.cukes

import com.jayway.restassured.http.ContentType
import cucumber.api.groovy.EN
import cucumber.api.groovy.Hooks
import groovyx.net.http.URIBuilder
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.groovy.test.TestHttpClient
import ratpack.groovy.test.TestHttpClients

this.metaClass.mixin(Hooks)
this.metaClass.mixin(EN)

System.setProperty 'ENV', 'cucumber'

class LocalRatpackWorld {

    LocalScriptApplicationUnderTest aut = new LocalScriptApplicationUnderTest()

    @Delegate
    TestHttpClient client = TestHttpClients.testHttpClient(aut)
}

World {
    new LocalRatpackWorld()
}

Before() {
}

After() {
    aut.stop()
}

Given(~'I delete folder urn: (.*)') { String urn ->
    resetRequest()
    delete("repository/file/${urn}")
}

When(~'I write urn: (.*) and updateDate: (.*) and content: content') { String urn, String updateDate ->
    resetRequest()
    request.contentType(ContentType.TEXT)
    request.body("content")

    post(updateDate != "null" ? "repository/file/${urn}?updateDate=${updateDate}" : "repository/file/${urn}")
}

When(~'I read urn: (.*)') { String urn ->
    resetRequest()
    get("repository/file/${urn}")
}

When(~'I ask for delta with urn: (.*) and start date: (.*) and end date: (.*)') { String urn, String sdate, String edate ->
    resetRequest()

    def uri = new URIBuilder("http://repository/delta/${urn}")
    if (sdate != "null")
        uri.addQueryParam("sdate", sdate)
    if (edate != "null")
        uri.addQueryParam("edate", edate)

    //Remove "https://"
    get(uri.toString().substring(7))
}

Then(~'Response is: (.*)') { String responseCode ->
    switch (responseCode) {
        case "OK":
            assert 200 == response.statusCode
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

Then(~'Delta response is: (.*)') { String responseBody ->
    assert responseBody == response.body.asString()
}