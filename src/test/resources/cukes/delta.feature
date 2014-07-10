# language: en
Feature: Delta service
  Files should be read from repository with the given urn

  Scenario: Find content with 2 dates given
    Given I write urn:cucumber:a:b:c1 for content content as if in date 1980-01-01T00:00:00.000Z
    And I write urn:cucumber:a:b:c2 for content content as if in date 1980-01-01T01:00:00.000Z
    And I write urn:cucumber:a:b:c3 for content content as if in date 1980-01-01T02:00:00.000Z
    When I ask for delta for urn:cucumber:a:b for start date 1980-01-01T00:30:00.000Z and end date 1980-01-01T01:30:00.000Z
    Then Delta response is [ "urn:cucumber:a:b:c2" ]
    When I ask for delta for urn:cucumber:a:b for start date 2000-01-01T00:00:00.000Z and end date 2000-01-02T00:00:00.000Z
    Then Delta response is [ ]
    When I ask for delta for urn:cucumber:a:b for start date 1970-01-01T00:00:00.000Z and end date 1970-01-02T00:00:00.000Z
    Then Delta response is [ ]

  Scenario: Find content with 1 date given
    Given I write urn:cucumber:a:b:c1 for content content as if in date 1980-01-01T00:00:00.000Z
    And I write urn:cucumber:a:b:c2 for content content as if in date 1980-01-01T01:00:00.000Z
    And I write urn:cucumber:a:b:c3 for content content as if in date 1980-01-01T02:00:00.000Z
    When I ask for delta for urn:cucumber:a:b for start date null and end date 1980-01-01T01:30:00.000Z
    Then Delta response is [ "urn:cucumber:a:b:c1", "urn:cucumber:a:b:c2" ]
    When I ask for delta for urn:cucumber:a:b for start date 1980-01-01T00:30:00.000Z and end date null
    Then Delta response is [ "urn:cucumber:a:b:c2", "urn:cucumber:a:b:c3" ]
    When I ask for delta for urn:cucumber:a:b for start date 2000-01-01T00:00:00.000Z and end date null
    Then Delta response is [ ]

  Scenario: Find content with no dates given
    Given I write urn:cucumber:a:b:c1 for content content as if in date 1980-01-01T00:00:00.000Z
    And I write urn:cucumber:a:b:c2 for content content as if in date 1980-01-01T01:00:00.000Z
    And I write urn:cucumber:a:b:c3 for content content as if in date 1980-01-01T02:00:00.000Z
    When I ask for delta for urn:cucumber:a:b for start date null and end date null
    Then Delta response is [ "urn:cucumber:a:b:c1", "urn:cucumber:a:b:c2", "urn:cucumber:a:b:c3" ]

  Scenario: Repository is empty
    When I ask for delta for urn:cucumber:a:b for start date null and end date null
    Then Delta response is [ ]

  Scenario: Ask delta with invalid parameters
    When I ask for delta for X for start date null and end date null
    Then Delta response is {"status":400,"message":"rejected"}
    When I ask for delta for urn:cucumber:a:b for start date X and end date null
    Then Delta response is {"status":400,"message":"rejected"}
    When I ask for delta for urn:cucumber:a:b for start date null and end date X
    Then Delta response is {"status":400,"message":"rejected"}

  Scenario: Update file and get from delta
    Given I write urn:cucumber:a:b:c4 for content content as if in date 1980-01-01T00:00:00.000Z
    And I write urn:cucumber:a:b:c4 for content content as if in date 2014-07-01T01:00:00.000Z
    When I ask for delta for urn:cucumber:a:b for start date 1980-01-01T01:00:00.000Z and end date 1980-01-02T01:00:00.000Z
    Then Delta response is [ ]
    When I ask for delta for urn:cucumber:a:b for start date 2014-06-01T01:00:00.000Z and end date null
    Then Delta response is [ "urn:cucumber:a:b:c4" ]

   

   
   
