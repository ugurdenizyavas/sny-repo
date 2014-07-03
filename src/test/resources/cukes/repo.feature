# language: en
Feature: Repo Read/Write/Delete and Delta Features
  Files should be written to repo with or without an updateDate
  Files should be got from repo with urn
  Files should be deleted with a urn
  Delta service should query files with start and end date via files' last modification date

  Scenario: Write flow
    Given I delete folder urn: urn:flix_sku:global:en_gb
    When I write urn: urn:flix_sku:global:en_gb:DSCH200W and updateDate: null and content: content
    Then Response is: Accepted

  Scenario: Corrupted write flow
    Given I delete folder urn: urn:flix_sku:global:en_gb
    When I write urn: wrongurn:flix_sku:global:en_gb:DSCH200W and updateDate: null and content: content
    Then Response is: Rejected

  Scenario: Read flow
    Given I delete folder urn: urn:flix_sku:global:en_gb
    And I write urn: urn:flix_sku:global:en_gb:DSCH200W and updateDate: null and content: content
    When I read urn: urn:flix_sku:global:en_gb:DSCH200W
    Then Response is: OK

  Scenario: Corrupted read flow
    Given I delete folder urn: urn:flix_sku:global:en_gb
    And I write urn: urn:flix_sku:global:en_gb:DSCH200W and updateDate: null and content: content
    When I read urn: wrongurn:flix_sku:global:en_gb:DSCH200W
    Then Response is: Rejected

  Scenario: No file found read flow
    Given I delete folder urn: urn:flix_sku:global:en_gb
    And I write urn: urn:flix_sku:global:en_gb:DSCH200W and updateDate: null and content: content
    When I read urn: urn:flix_sku:global:en_gb:NOFILE
    Then Response is: Not Found

  Scenario: Delta Flow
    Given I delete folder urn: urn:flix_sku:global:en_gb
    And I write urn: urn:flix_sku:global:en_gb:DSCH200W and updateDate: 1980-01-01T00:00:00.000Z and content: content
    And I write urn: urn:flix_sku:global:en_gb:DSCH200Y and updateDate: 1980-01-01T01:00:00.000Z and content: content
    And I write urn: urn:flix_sku:global:en_gb:DSCH200Z and updateDate: 1980-01-01T02:00:00.000Z and content: content
    When I ask for delta with urn: urn:flix_sku:global:en_gb and start date: 1980-01-01T00:30:00.000Z and end date: 1980-01-01T01:30:00.000Z
    Then Delta response is: [ "urn:flix_sku:global:en_gb:dsch200y" ]
    When I ask for delta with urn: urn:flix_sku:global:en_gb and start date: null and end date: 1980-01-01T01:30:00.000Z
    Then Delta response is: [ "urn:flix_sku:global:en_gb:dsch200w", "urn:flix_sku:global:en_gb:dsch200y" ]
    When I ask for delta with urn: urn:flix_sku:global:en_gb and start date: 1980-01-01T00:30:00.000Z and end date: null
    Then Delta response is: [ "urn:flix_sku:global:en_gb:dsch200y", "urn:flix_sku:global:en_gb:dsch200z" ]
    When I ask for delta with urn: urn:flix_sku:global:en_gb and start date: null and end date: null
    Then Delta response is: [ "urn:flix_sku:global:en_gb:dsch200w", "urn:flix_sku:global:en_gb:dsch200y", "urn:flix_sku:global:en_gb:dsch200z" ]

