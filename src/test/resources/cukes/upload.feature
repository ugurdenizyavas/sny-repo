# language: en
Feature: Upload service
  Files should be uploaded from repository to destination via their urns

  Scenario: Upload file with valid parameters
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    And I zip urn:cucumber:a:b
    When I upload urn:cucumber:a:b.zip to S3
    Then Response is Accepted and response body is {"status":202,"message":"accepted"}

  Scenario: Cannot upload  file with invalid urn
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    And I zip urn:cucumber:a:b
    When I upload invalid_urn:cucumber:a:b.zip to S3
    Then Response is Rejected and response body is {"status":400,"message":"rejected"}

  Scenario: Cannot upload file with invalid upload parameter
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    And I zip urn:cucumber:a:b
    When I upload urn:cucumber:a:b.zip to invalid_upload
    Then Response is Rejected and response body is {"status":400,"message":"rejected"}

  Scenario: Cannot upload a non-existing file
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    And I zip urn:cucumber:a:b
    When I upload invalid_urn:cucumber:a:b1.zip to S3
    Then Response is Not Found and response body is {"status":404,"message":"File in path /cucumber/a/b1 not found"}
