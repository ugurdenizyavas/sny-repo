# language: en
Feature: Upload service
  Files should be uploaded from repository to destination via their urns

  Scenario: Do ops with valid parameters
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    When I use operation in file ops/ops.json
    Then Response is Accepted and response body is {"status":202,"message":"accepted"}

  Scenario: Cannot do ops with no file
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    When I use operation in file ops/NON_EXISTING
    Then Response is Rejected and response body is {"status":400,"message":"rejected"}

  Scenario: Cannot do ops if cannot parse file
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    When I use operation in file ops/invalid_ops.json
    Then Response is Rejected and response body is {"status":400,"message":"rejected"}
