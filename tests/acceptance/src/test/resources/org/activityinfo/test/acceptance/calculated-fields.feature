@web
Feature: Calculated fields

  Background:
    Given I have created a database "RRMP"
    And I have added partner "NRC" to "RRMP"
    And I have created a form named "NFI Distribution"

  @AI-991
  Scenario: Calculating Percentages.
    Given I have created a quantity field "a" in "NFI Distribution" with code "a"
    And I have created a quantity field "b" in "NFI Distribution" with code "b"
    And I have created a calculated field "c" in "NFI Distribution" with expression "{a}/{b}"
    When I submit a "NFI Distribution" form with:
      | field              | value           |
      | a                  | 1               |
      | b                  | 2               |
    Then the submission's detail shows:
      | field              | value           |
      | a                  | 1               |
      | b                  | 2               |
      | c                  | 0.5             |
    When I update the submission with:
      | field              | value           |
      | a                  | 1               |
      | b                  | 0               |
    Then the submission's detail shows:
      | field              | value           |
      | a                  | 1               |
      | c                  | ∞              |
    When I update the submission with:
      | field              | value           |
      | a                  | 0               |
      | b                  | 0               |
    Then the submission's detail shows:
      | field              | value           |
      | c                  | NaN             |

  @AI-1041
  Scenario: Pivot calculated indicator.
    Given I have created a quantity field "total" in "NFI Distribution" with code "total"
    And I have created a quantity field "withHelmet" in "NFI Distribution" with code "helmet"
    And I have created a calculated field "percent" in "NFI Distribution" with expression "({helmet}/{total})*100" with aggregation "Average"
    And I submit a "NFI Distribution" form with:
      | field      | value  |
      | total      | 300    |
      | withHelmet | 150    |
    And I submit a "NFI Distribution" form with:
      | field      | value  |
      | total      | 500    |
      | withHelmet | 50     |
    And I submit a "NFI Distribution" form with:
      | field      | value  |
      | total      | 100    |
      | withHelmet | 90     |
    Then aggregating the indicator "percent" by Indicator should yield:
      |                  | Value |
      | percent          | 50    |
