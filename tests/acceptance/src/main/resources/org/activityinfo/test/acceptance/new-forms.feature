@web
Feature: New form

  Background:
    Given I have created a database "Patient Registration"
    And I have added partner "NRC" to "Patient Registration"
    And I have created a form "Patient Visits" using the new layout
    And I have created a form named "Medical Activities" with the submissions:
      | Number of patients | Donor   | Diseases treated this month    | Comments         | Partner |
      | quantity           | enum    | enum                           | text             | enum    |
      | 230                | USAID   | cholera, malaria, tuberculosis | no comment       | NRC     |
      | 11                 | ECHO    | cholera                        | cholera epidemic | NRC     |

  @AI-1108
  @web
  Scenario: Partners field is represented by suggest box
    When I have added 21 partners
    Then form "Patient Visits" in database "Patient Registration" has "Partner" field represented by "suggestbox"

  @AI-1213
  @web
  Scenario: Partners field is represented by combo box
    When I have added 11 partners
    Then form "Patient Visits" in database "Patient Registration" has "Partner" field represented by "dropdown"
    When edit entry in new table with field name "Partner Name" and value "Default" in the database "Patient Registration" in the form "Medical Activities" with:
      | field        | value    | controlType |
      | Partner Name | partner5 | dropdown    |

  @AI-1124
  @web
  Scenario: Add entry without project in database
    Given I have submitted to "Medical Activities" form table in "Patient Registration" database:
      | Number of patients | Donor   | Diseases treated this month    | Comments         | Partner | Start Date | End Date   |
      | quantity           | enum    | enum                           | text             | enum    | date       | date       |
      | 13                 | USAID   | cholera                        | no comment       | NRC     | 2014-01-02 | 2014-01-04 |
    Then table has rows with hidden built-in columns:
      | Number of patients | Donor   | Diseases treated this month    |
      | quantity           | enum    | enum                           |
      | 230                | USAID   | cholera, malaria, tuberculosis |
      | 11                 | ECHO    | cholera                        |
      | 13                 | USAID   | cholera                        |

  @AI-1200
  Scenario: Project built-in field is not shown if there is not projects in the database
    When I open a new form submission for "Patient Visits" then following fields are invisible:
      | Project |

  @AI-1033
  @AI-1009
  @web
  Scenario: Built-in fields are always shown
    When I open the form designer for "Patient Visits" in database "Patient Registration"
    Then following fields should be visible in form designer:
      | Partner    |
      | Start Date |
      | End Date   |
      | Comments   |
    Then following fields are not deletable in form designer:
      | Partner    |
      | Start Date |
      | End Date   |
      | Comments   |
    Then "relevance, visible, required" field properties are disabled in form designer for:
      | Partner    |
      | Start Date |
      | End Date   |
      | Comments   |
    When I open a new form submission for "Patient Visits" then following fields are visible:
      | Partner    |
      | Start Date |
      | End Date   |
      | Comments   |

  @AI-1009
  Scenario: Partner field
    When I open the form designer for "Patient Visits" in database "Patient Registration"
    Then reorder "Partner" designer field to position 2
    Then "Partner" designer field is mandatory
    Then change designer field "Partner" with:
      | field       | value         |
      | label       | Partner2      |
      | description | Partner2 desc |
    Then following fields should be visible in form designer:
      | Partner2  |
#    When I open a new form submission for "Patient Visits" then field values are:
#      | field     | value   | controlType |
#      | Partner2  | Default | radio       |

#  @web @odk
#  Scenario: Submitting a form with implicit user group
#    And I have added a quantity field "Age"
#    When I open data entry for "Patient Visits"
#    Then the the field "User Group" should not be present
#    When I submit the form with:
#    | field | value |
#    | Age   | 18    |
#    And I export "Patient Visits"
#    Then the exported spreadsheet should contain:
#    | user group | age |
#    | Default    | 18  |
#
#  @web @odk
#  Scenario: Submitting a form with explicit user group
#    And I have added partner "Southern Office"
#    And I have added partner "Northern Office"
#    And I have removed the default user group
#    When I open data entry for "Patient Visits"
#    Then the the field "User Group" should be present with the choices:
#      | Southern Office |
#      | Northern Office |
#    When I submit the form with:
#      | field   | value             |
#      | user group | Southern Office   |
#      | Age     | 18                |
#    And I export "Patient Visits"
#    Then the exported spreadsheet should contain:
#      | user group        | age |
#      | Southern Office   | 18  |
#
#  @web
#  Scenario: Importing a form with implicit user group
#    Given I have created a database "Patient Registration"
#    And I have added a quantity field "Age"
#    When I import a spreadsheet containing:
#      | age |
#      | 18  |
#    And I export "Patient Visits"
#    Then the exported spreadsheet should contain:
#      | user group | age |
#      | Default    | 18  |
    
    