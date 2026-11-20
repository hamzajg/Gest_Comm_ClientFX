# industrial-look-feel — specifications by example
# Scenario tags @w<N> map items to delivery waves (see WAVES.md / BACKLOG.md).

Feature: industrial-look-feel

  @w1 @happy-path
  Scenario: Theme tokens load
    Given the application scene is created by StartApp
    Then the scene carries exactly one stylesheet, the industrial sheet
    And the sheet defines the graphite surfaces, steel borders and amber accent once

  @w1 @happy-path
  Scenario: Shell adopts the industrial surface
    Given the main window is open on a 1200x700 scene
    When the user looks at the menu bar, the search row and the tab strip
    Then all three use the dark surface with steel separators
    And the selected tab is marked with the amber accent

  @w1 @happy-path
  Scenario: Tables follow the theme
    Given the Clients tab is open against the running backend
    Then the header row uses uppercase micro-labels on the raised surface
    And rows alternate between the two graphite surfaces
    And the selected row is highlighted in amber rather than system blue
    And the status label reads in muted monospace

  @w1 @happy-path
  Scenario: Drawer is styled expanded and collapsed
    Given a tab with actions is active
    When the drawer is expanded
    Then its caption, title and action buttons use the drawer surface and ghost button style
    When the drawer is collapsed to its rail
    Then the rail keeps the same background and border and shows only the expand toggle

  @w1 @happy-path
  Scenario: Colours live only in the stylesheet
    Given the source tree after this wave
    Then no controller or view sets an `-fx-` colour inline
    And every colour resolves from the stylesheet token block

  @w2 @happy-path
  Scenario: Window respects the minimum size
    Given the window is resized to 1024x640
    Then the menu bar, search row, tab strip and drawer still fit without clipping

  @w2 @happy-path
  Scenario: Forms inherit the theme
    Given the Nouveau client form is open as a tab
    Then its labels use the uppercase micro-label style
    And its fields, combo boxes and buttons match the shell surfaces
