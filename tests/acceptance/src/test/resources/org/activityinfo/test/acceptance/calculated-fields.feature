@web
Feature: Calculated fields

  Background:
    Given I have created a database "RRMP"
    And I have added partner "NRC" to "RRMP"
    And I have added partner "UPS" to "RRMP"
    And I have created a form named "NFI Distribution"

  @AI-991
  Scenario: Calculating Percentages
    Given I have created a quantity field "a" in "NFI Distribution" with code "a"
    And I have created a quantity field "b" in "NFI Distribution" with code "b"
    And I have created a calculated field "c" in "NFI Distribution" with expression "{a}/{b}"
    When I submit a "NFI Distribution" form with:
      | field              | value           |
      | partner            | NRC             |
      | a                  | 1               |
      | b                  | 2               |
      | partner            | NRC             |
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
      | c                  | ∞               |
    When I update the submission with:
      | field              | value           |
      | a                  | 0               |
      | b                  | 0               |
    Then the submission's detail shows:
      | field              | value           |
      | c                  | NaN             |

  @AI-1041
  Scenario: Pivot calculated indicator
    Given I have created a quantity field "total" in "NFI Distribution" with code "total"
    And I have created a quantity field "withHelmet" in "NFI Distribution" with code "helmet"
    And I have created a calculated field "percent" in "NFI Distribution" with expression "({helmet}/{total})*100" with aggregation "Average"
    And I have submitted "NFI Distribution" forms with:
      | partner    | total  | withHelmet |
      | NRC        | 300    | 150        |
      | NRC        | 500    | 50         |
      | NRC        | 100    | 90         |
    Then aggregating the indicator "percent" by Indicator should yield:
      |                  | Value |
      | percent          | 50    |

  @AI-1082
  Scenario: Drill down on calculated indicator
    Given I have created a quantity field "i1" in "NFI Distribution" with code "i1"
    And I have created a quantity field "i2" in "NFI Distribution" with code "i2"
    And I have created a calculated field "plus" in "NFI Distribution" with expression "{i1}+{i2}" with aggregation "Average"
    And I have created a calculated field "percent" in "NFI Distribution" with expression "({i1}/{i2})*100" with aggregation "Sum"
    And I have submitted "NFI Distribution" forms with:
      | partner | i1  | i2  | Start Date | End Date   |
      | NRC     | 300 | 150 | 2014-05-21 | 2014-05-21 |
      | NRC     | 100 | 10  | 2014-07-21 | 2014-07-21 |
      | NRC     | 4   | 20  | 2015-05-21 | 2015-05-21 |
      | NRC     | 5   | 50  | 2015-07-21 | 2015-07-21 |

    Then aggregating the indicators plus and percent by Indicator and Year should yield:
      |                  |  2014 |   2015 |
      | plus             |   280 |   39.5 |
      | percent          | 1,200 |   30   |
    Then drill down on "280" should yield:
      | RRMP | NFI Distribution | NRC |  | 2014-07-21 | plus | 110 |
      | RRMP | NFI Distribution | NRC |  | 2014-05-21 | plus | 450 |

  Scenario: Aggregating calculated indicators ignore missing values and divisions by zero
    # When aggregating calculated fields with values that include 10/0, for
    # example, these values are ignored
    Given I have created a quantity field "Cost" in "NFI Distribution" with code "C"
    And I have created a quantity field "Number of Beneficiaries" in "NFI Distribution" with code "N"
    And I have created a calculated field "Cost/Beneficiary" in "NFI Distribution" with expression "C/N" with aggregation "Average"
    And I have submitted "NFI Distribution" forms with:
      | partner | Cost  | Number of Beneficiaries | Start Date | End Date   |
      | NRC     |       | 150                     | 2016-01-01 | 2016-12-31 |
      | NRC     | 100   | 0                       | 2016-01-01 | 2016-12-31 |
      | NRC     | 4     |                         | 2016-01-01 | 2016-12-31 |
      | UPS     | 100   |                         | 2016-01-01 | 2016-12-31 |
      | UPS     | 500   | 50                      | 2016-01-01 | 2016-12-31 |
      | UPS     | 600   | 0                       | 2016-01-01 | 2016-12-31 |
    Then aggregating the indicators Cost/Beneficiary by Partner and Year should yield:
      |         | 2016  |
      | UPS     | 10    |

  @AI-1082
  Scenario: Combining sum and average calculated indicators
    # When combining indicators that use different aggregation methods,
    # the sum aggregation method should always be used
    Given I have created a quantity field "i1" in "NFI Distribution" with code "i1"
    And I have created a quantity field "i2" in "NFI Distribution" with code "i2"
    And I have created a calculated field "plus" in "NFI Distribution" with expression "{i1}+{i2}" with aggregation "Average"
    And I have created a calculated field "percent" in "NFI Distribution" with expression "({i1}/{i2})*100" with aggregation "Sum"
    And I have submitted "NFI Distribution" forms with:
      | partner | i1  | i2  | Start Date | End Date   |
      | NRC     | 300 | 150 | 2014-05-21 | 2014-05-21 |
      | UPS     | 100 | 10  | 2014-07-21 | 2014-07-21 |
      | NRC     | 10  | 2   | 2014-10-21 | 2014-10-21 |
      | NRC     | 4   | 20  | 2015-05-21 | 2015-05-21 |
      | UPS     | 5   | 50  | 2015-07-21 | 2015-07-21 |
      | NRC     | 7   | 0   | 2016-07-21 | 2016-07-21 |

    Then aggregating the indicators percent by Partner and Year should yield:
      |         | 2014  | 2015 |
      | NRC     | 700   | 20   |
      | UPS     | 1,000 | 10   |
    Then aggregating the indicators plus by Partner and Year should yield:
      |         | 2014  | 2015 | 2016 |
      | NRC     | 231   | 24   | 7    |
      | UPS     | 110   | 55   |      |
    Then aggregating the indicators i1 and plus by Partner and Year should yield:
      |         | 2014 | 2015 | 2016 |
      | NRC     | 772  | 28   | 14   |
      | UPS     | 210  | 60   |      |
    Then aggregating the indicators plus and percent by Partner and Year should yield:
      |         | 2014   | 2015 | 2016 |
      | NRC     | 1,162  | 44   |    7 |
      | UPS     | 1,110  | 65   |      |