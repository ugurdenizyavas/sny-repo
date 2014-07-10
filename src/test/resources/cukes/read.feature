# language: en
Feature: Read Data
  Files should be read from repository with the given urn

  Scenario: Read a product
    Given I write urn:cucumber:a:b:c for content content with current date
    When I read urn:cucumber:a:b:c
    Then Response is OK and response body is content

  Scenario: Cannot read due to invalid urn syntax
    Given I write urn:cucumber:a:b:c for content content with current date
    When I read XYZ
    Then Response is Rejected and response body is {"status":400,"message":"rejected"}

  Scenario: No file found
    Given I write urn:cucumber:a:b:c for content content with current date
    When I read urn:cucumber:a:b:ANOTHER_FILE
    Then Response is Not Found and response body is {"status":404,"message":"File in path /cucumber/a/b/another_file not found"}
	
  Read a deleted product
	Given I write urn:cucumber:a:b:c for content content with current date
	And I delete file urn:cucumber:a:b:c 
	And when I read urn:cucumber:a:b:c
	Then Response is Not Found and response body is {"status":404,"message":"File in path /cucumber/a/b/another_file not found"}
