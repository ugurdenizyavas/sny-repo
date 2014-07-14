# language: en
Feature: Copy service
  Files should be copied in repository from one urn to another

  Scenario: Copy file from one path to another
    Given I write urn:cucumber:a:b:c1 for content content with current date
    When I copy urn:cucumber:a:b:c1 to urn:cucumber:a:b:c2
    Then Response is Accepted and response body is {"status":202,"message":"accepted"}

  Scenario: Cannot copy a non-existing path
    Given I write urn:cucumber:a:b:c1 for content content with current date
    When I copy urn:cucumber:a:b:ANOTHER_FILE to urn:cucumber:a:b:c2
    Then Response is Not Found and response body is {"status":404,"message":"File in path /cucumber/a/b/ANOTHER_FILE not found"}

  Scenario: Cannot copy a invalid path
    Given I write urn:cucumber:a:b:c1 for content content with current date
    When I copy invalid_urn:cucumber:a:b to urn:cucumber:a:b:c2
    Then Response is Rejected and response body is {"status":400,"message":"rejected"}