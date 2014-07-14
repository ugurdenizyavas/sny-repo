# language: en
Feature: Zip service
  Files should be zipped from repository with the given urn

  Scenario: Zip folder with valid path
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    When I zip urn:cucumber:a:b
    Then Response is Created and response body is {"status":201, "zippedFiles":["urn:cucumber:a:b:c1","urn:cucumber:a:b:c2", "urn:cucumber:a:b:c3], "zipPath":"urn:cucumber:a:b"}

  Scenario: Cannot zip folder with non-existing path
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    When I zip urn:cucumber:a:ANOTHER_FILE
    Then Response is Not Found and response body is {"status":404,"message":"File in path /cucumber/a/b1 not found"}

  Scenario: Cannot zip folder with invalid urn
    Given I write urn:cucumber:a:b:c1 for content content with current date
    And I write urn:cucumber:a:b:c2 for content content with current date
    And I write urn:cucumber:a:b:c3 for content content with current date
    When I zip invalid_urn:cucumber:a:b
    Then Response is Rejected and response body is {"status":400,"message":"rejected"}