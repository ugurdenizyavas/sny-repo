# language: en
Feature: Write Data
  Files should be written to repository to the given urn with the given content

  Scenario: Write content
    When I write urn:cucumber:a:b:c for content content with current date
    Then Response is Accepted and response body is {"status":202, "message":"accepted"}

  Scenario: Cannot write content due to invalid urn
    When I write X for content content with current date
    Then Response is Rejected and response body is {"status":400,"message":"rejected"}

  Scenario: Write content check repo
    When I write urn:cucumber:a:b:c for content content with current date
    And I wait for 5 seconds
    And I read urn:cucumber:a:b:c
    Then Response is OK and response body is content

  Scenario: Concurrent write check repo
    When I write urn:cucumber:a:b:c for content content1 with current date
    And I write urn:cucumber:a:b:c for content content2 with current date
    And I wait for 5 seconds
    And I read urn:cucumber:a:b:c
    Then Response is OK and response body is content2