Feature: Password Reset via Email

  Scenario: [POSITIVE] Successful password reset and login with registered email
    Given a registered user with email "user@example.com" exists
    When the user requests a password reset for "user@example.com"
    Then a success message is displayed on the screen
    And a reset email is sent to "user@example.com" within 60 seconds
    When the user opens the reset link from the email
    And the user enters a new password "SecureP@ss123" and submits
    Then the password is updated successfully
    And the user is automatically logged in and redirected to the dashboard
    And a password change confirmation email is sent to "user@example.com"

  Scenario: [POSITIVE] Requesting reset for unregistered email displays generic message
    Given a user with email "unregistered@example.com" does not exist in the system
    When the user requests a password reset for "unregistered@example.com"
    Then a generic message "If your email is registered, you will receive a reset link shortly." is displayed
    And no password reset email is sent to "unregistered@example.com"

  Scenario: [POSITIVE] Password reset using exact minimum password complexity requirements
    Given a user has requested a password reset and received a valid reset token
    When the user accesses the password reset page with the token
    And the user enters a new password "A1!bcdef" and submits
    Then the password is updated successfully
    And the user is automatically logged in

  Scenario: [NEGATIVE] Password reset fails when new password does not meet complexity requirements
    Given a user has requested a password reset and received a valid reset token
    When the user accesses the password reset page with the token
    And the user enters a new password "weakpass" and submits
    Then an error message "Password must be at least 8 characters, contain 1 uppercase, 1 number, and 1 special character" is displayed
    And the user is not logged in

  Scenario: [NEGATIVE] Attempting password reset with an expired token
    Given a registered user with email "user@example.com" requested a password reset
    And 16 minutes have passed since the reset link was generated
    When the user clicks the password reset link
    Then an error message "This password reset link has expired" is displayed
    And the user cannot reset the password

  Scenario: [NEGATIVE] Reusing a password reset link that has already been used
    Given a registered user with email "user@example.com" has successfully reset their password using their reset link
    When the user attempts to access the same reset link a second time
    Then an error message "This reset link is invalid or has already been used" is displayed

  Scenario: [BOUNDARY] Password reset with a password of exactly eight characters
    Given a user has requested a password reset and received a valid reset token
    When the user accesses the password reset page with the token
    And the user enters a new password of exactly eight characters "Ab1!c2#d" and submits
    Then the password is updated successfully
    And the user is automatically logged in

  Scenario: [EDGE] Requesting a new reset link invalidates the previous active reset link
    Given a registered user with email "user@example.com" requested a password reset and received token "TOKEN_A"
    When the user requests a second password reset and receives token "TOKEN_B" within 5 minutes
    And the user attempts to access the password reset page using "TOKEN_A"
    Then an error message "This reset link is invalid or has been superseded" is displayed
    When the user accesses the password reset page using "TOKEN_B"
    Then the user is allowed to proceed with the password reset