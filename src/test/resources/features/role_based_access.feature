@rbac @ui
Feature: Role-based access control
  Users must only see and reach the modules their role allows.
  Admin rows act as positive controls, proving the ESS "hidden/denied" checks are not vacuous.
  ESS accounts are created on demand through the API and removed afterwards.

  @regression @severity=critical
  Scenario Outline: <role> sees the "<menu>" menu as <visibility>
    Given I am logged in as <role>
    Then the "<menu>" menu should be <visibility>

    @smoke
    Examples: Admin (positive control)
      | role  | menu  | visibility |
      | Admin | Admin | visible    |
      | Admin | PIM   | visible    |

    @smoke @destructive
    Examples: ESS user
      | role | menu    | visibility |
      | ESS  | Admin   | hidden     |
      | ESS  | PIM     | hidden     |
      | ESS  | My Info | visible    |

  @regression @severity=critical
  Scenario Outline: <role> opening the System Users URL directly is <access>
    Given I am logged in as <role>
    When I open the System Users page directly by URL
    Then access to the System Users page should be <access>

    Examples: Admin (positive control)
      | role  | access  |
      | Admin | granted |

    @destructive
    Examples: ESS user
      | role | access |
      | ESS  | denied |
