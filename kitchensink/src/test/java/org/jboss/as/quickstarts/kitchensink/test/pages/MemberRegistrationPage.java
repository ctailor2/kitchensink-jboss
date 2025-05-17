package org.jboss.as.quickstarts.kitchensink.test.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MemberRegistrationPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "reg:name")
    private WebElement nameInput;

    @FindBy(id = "reg:email")
    private WebElement emailInput;

    @FindBy(id = "reg:phoneNumber")
    private WebElement phoneNumberInput;

    @FindBy(id = "reg:register")
    private WebElement registerButton;

    @FindBy(css = ".messages")
    private WebElement messages;

    @FindBy(css = ".simpletablestyle")
    private WebElement membersTable;

    public MemberRegistrationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 10);
        PageFactory.initElements(driver, this);
    }

    public void navigateTo(String baseUrl) {
        driver.get(baseUrl);
    }

    public void registerMember(String name, String email, String phoneNumber) {
        nameInput.clear();
        nameInput.sendKeys(name);

        emailInput.clear();
        emailInput.sendKeys(email);

        phoneNumberInput.clear();
        phoneNumberInput.sendKeys(phoneNumber);

        registerButton.click();
    }

    public String getSuccessMessage() {
        wait.until(ExpectedConditions.visibilityOf(messages));
        return messages.getText();
    }

    public boolean isMemberTableVisible() {
        return membersTable.isDisplayed();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}