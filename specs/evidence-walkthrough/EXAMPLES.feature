# evidence-walkthrough — specifications by example
# Scenario tags @w<N> map items to delivery waves (see WAVES.md / BACKLOG.md).

Feature: evidence-walkthrough

  @w1 @happy-path
  Scenario: Regenerates every screen capture
    Given the PyGestComm dev server is running and the client is compiled
    And tools/Screenshots.java is compiled against target/classes
    When the documented java command runs with valid credentials
    Then one PNG per screen is written to the output directory
    And the run reports zero controller exceptions

  @w1 @repo-hygiene
  Scenario: No image binaries are tracked
    Given the repository at any commit
    When `git ls-files` is filtered for *.png under docs/ and specs/
    Then the result is empty

  @w1 @documentation
  Scenario: Walkthrough maps captures to screens
    Given specs/evidence-walkthrough/
    When a reviewer reads SPEC.md and VALIDATION.md
    Then each capture is tied to a screen and the API calls it exercises
