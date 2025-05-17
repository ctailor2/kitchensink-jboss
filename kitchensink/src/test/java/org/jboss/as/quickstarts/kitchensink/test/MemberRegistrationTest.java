package org.jboss.as.quickstarts.kitchensink.test;

import org.jboss.as.quickstarts.kitchensink.test.pages.MemberRegistrationPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

public class MemberRegistrationTest extends BaseSeleniumTest {

    private MemberRegistrationPage registrationPage;

    @BeforeEach
    public void setupTest() {
        registrationPage = new MemberRegistrationPage(driver);
        registrationPage.navigateTo(BASE_URL);
    }

    @Test
    public void testSuccessfulMemberRegistration() {
        // Register a new member
        registrationPage.registerMember(
            "John Doe",
            "john.doe@example.com",
            "1234567890"
        );

        // Verify success message
        String message = registrationPage.getSuccessMessage();
        assertTrue(message.contains("Registered!"), "Expected success message not found");

        // Verify member table is visible and contains the new member
        assertTrue(registrationPage.isMemberTableVisible(), "Members table should be visible");

        // Verify the member appears in the table
        WebElement table = driver.findElement(By.className("simpletablestyle"));
        String tableText = table.getText();
        assertTrue(tableText.contains("John Doe"), "New member name not found in table");
        assertTrue(tableText.contains("john.doe@example.com"), "New member email not found in table");
        assertTrue(tableText.contains("1234567890"), "New member phone not found in table");
    }

    @Test
    public void testInvalidEmailRegistration() {
        // Try to register with invalid email
        registrationPage.registerMember(
            "Jane Doe",
            "invalid-email",
            "1234567890"
        );

        // Verify error message
        String message = registrationPage.getSuccessMessage();
        assertTrue(message.contains("invalid"), "Expected error message not found");
    }

    @Test
    public void testEmptyNameRegistration() {
        // Try to register with empty name
        registrationPage.registerMember(
            "",
            "jane.doe@example.com",
            "1234567890"
        );

        // Verify error message
        String message = registrationPage.getSuccessMessage();
        assertTrue(message.contains("invalid"), "Expected error message not found");
    }

    @Test
    public void testInvalidPhoneNumberRegistration() {
        // Try to register with invalid phone number
        registrationPage.registerMember(
            "Jane Doe",
            "jane.doe@example.com",
            "abc"  // Invalid phone number
        );

        // Verify error message
        String message = registrationPage.getSuccessMessage();
        assertTrue(message.contains("invalid"), "Expected error message not found");
    }

    @Test
    public void testRestUrlGeneration() {
        // First register a member
        registrationPage.registerMember(
            "REST Test",
            "rest.test@example.com",
            "9876543210"
        );

        // Verify REST URL is generated and visible in the table
        WebElement table = driver.findElement(By.className("simpletablestyle"));
        String tableText = table.getText();
        assertTrue(tableText.contains("/rest/members/"), "REST URL not found in table");
    }
}