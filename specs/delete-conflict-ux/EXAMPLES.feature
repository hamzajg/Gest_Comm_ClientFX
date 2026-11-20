# delete-conflict-ux — specifications by example
# Scenario tags @w<N> map items to delivery waves (see WAVES.md / BACKLOG.md).

Feature: delete-conflict-ux

  @w1 @error-path
  Scenario: A blocked delete explains why in French
    Given PyGestComm answers 409 with detail "client used by 2 devis"
    When the operator confirms Supprimer on that client row
    Then the status line reads "Suppression impossible : client used by 2 devis"
    And no raw HTTP status blob is shown

  @w1 @contract
  Scenario: API errors expose status and backend detail
    Given an error body {"detail": "Only a 'valide' devis can be converted"}
    When ApiClient receives a 400 for that request
    Then ApiException reports status 400 and that detail

  @w1 @ui
  Scenario: Home screen has no placeholder text and no empty filler column
    Given the Home screen is loaded
    Then no node renders the text "Text Here"
    And the client table's columns fill its width
