package org.jboss.as.quickstarts.kitchensink.test;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class MemberSeleniumIT {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl = "http://localhost:8080/kitchensink";

    @Before
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 10);
        driver.get(baseUrl);
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testSuccessfulMemberRegistration() {
        // Fill in the registration form
        driver.findElement(By.id("reg:name")).sendKeys("John Doe");
        driver.findElement(By.id("reg:email")).sendKeys("john" + System.currentTimeMillis() + "@test.com");
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");

        // Submit the form
        driver.findElement(By.id("reg:register")).click();

        // Wait for success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".messages")));
        assertTrue(successMessage.getText().contains("Registered!"));

        // Verify member appears in the table
        WebElement memberTable = driver.findElement(By.className("simpletablestyle"));
        assertTrue(memberTable.getText().contains("John Doe"));
    }

    @Test
    public void testInvalidNameRegistration() {
        // Test name with numbers (should fail)
        driver.findElement(By.id("reg:name")).sendKeys("John123");
        driver.findElement(By.id("reg:email")).sendKeys("john@test.com");
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");

        // Submit the form
        driver.findElement(By.id("reg:register")).click();

        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".invalid")));
        assertTrue(errorMessage.getText().contains("Must not contain numbers"));
    }

    @Test
    public void testInvalidEmailRegistration() {
        // Test invalid email format
        driver.findElement(By.id("reg:name")).sendKeys("John Doe");
        driver.findElement(By.id("reg:email")).sendKeys("invalid-email");
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");

        // Submit the form
        driver.findElement(By.id("reg:register")).click();

        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".invalid")));
        assertTrue(errorMessage.getText().contains("must be a well-formed email address"));
    }

    @Test
    public void testInvalidPhoneRegistration() {
        // Test phone number too short
        driver.findElement(By.id("reg:name")).sendKeys("John Doe");
        driver.findElement(By.id("reg:email")).sendKeys("john@test.com");
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("123");

        // Submit the form
        driver.findElement(By.id("reg:register")).click();

        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".invalid")));
        assertTrue(errorMessage.getText().contains("size must be between 10 and 12"));
    }

    @Test
    public void testDuplicateEmailRegistration() {
        String email = "duplicate" + System.currentTimeMillis() + "@test.com";

        // Register first member
        driver.findElement(By.id("reg:name")).sendKeys("John Doe");
        driver.findElement(By.id("reg:email")).sendKeys(email);
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");
        driver.findElement(By.id("reg:register")).click();

        // Wait for success message and clear form
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".messages")));

        // Try to register second member with same email
        driver.findElement(By.id("reg:name")).sendKeys("Jane Doe");
        driver.findElement(By.id("reg:email")).sendKeys(email);
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("0987654321");
        driver.findElement(By.id("reg:register")).click();

        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".invalid")));
        assertTrue(errorMessage.getText().contains("Email taken"));
    }

    @Test
    public void testMemberListing() {
        // Register a new member
        String uniqueName = "Test User " + System.currentTimeMillis();
        driver.findElement(By.id("reg:name")).sendKeys(uniqueName);
        driver.findElement(By.id("reg:email")).sendKeys("test" + System.currentTimeMillis() + "@test.com");
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");
        driver.findElement(By.id("reg:register")).click();

        // Wait for success message
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".messages")));

        // Verify member table exists and contains the new member
        WebElement memberTable = driver.findElement(By.className("simpletablestyle"));
        List<WebElement> rows = memberTable.findElements(By.tagName("tr"));
        boolean found = false;
        for (WebElement row : rows) {
            if (row.getText().contains(uniqueName)) {
                found = true;
                break;
            }
        }
        assertTrue("New member should appear in the table", found);
    }

    @Test
    public void testRestLinkGeneration() {
        // Register a new member
        driver.findElement(By.id("reg:name")).sendKeys("REST Test User");
        driver.findElement(By.id("reg:email")).sendKeys("rest" + System.currentTimeMillis() + "@test.com");
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");
        driver.findElement(By.id("reg:register")).click();

        // Wait for success message
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".messages")));

        // Find the REST URL in the table
        WebElement memberTable = driver.findElement(By.className("simpletablestyle"));
        List<WebElement> links = memberTable.findElements(By.tagName("a"));
        boolean foundRestLink = false;
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("/rest/members/")) {
                foundRestLink = true;
                assertTrue(href.matches(".*/rest/members/\\d+"));
                break;
            }
        }
        assertTrue("REST link should be present in the table", foundRestLink);
    }
}