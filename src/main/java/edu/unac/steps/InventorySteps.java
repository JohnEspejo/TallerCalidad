package edu.unac.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

public class InventorySteps {
    private ChromeDriver driver;

    @Before
    public void setUp(){
        System.setProperty("webdriver.chrome.driver",
                System.getProperty("user.dir") +
                        "/src/main/java/edu/unac/drivers/chromedriver.exe");//ADD YOUR DRIVER
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        chromeOptions.setBinary("C:\\Users\\helmi\\Downloads\\chrome-win64\\chrome.exe");
        driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();
        driver.get("D:/universidad/Semestre7/Calidad De Software/InventoryManagementApplication/Frontend/index.html");
    }

    @After
    public void tearDown(){
        driver.quit();
    }

    @Given("a device is registered with name Projector, type Multimedia, and location Room101")
    public void a_device_is_registered_with_name_projector_type_multimedia_and_location_room() {
        WebElement nameInput = driver.findElement(By.id("deviceName"));
        nameInput.sendKeys("Projector");

        WebElement typeInput = driver.findElement(By.id("deviceType"));
        typeInput.sendKeys("Multimedia");

        WebElement locationInput = driver.findElement(By.id("deviceLocation"));
        locationInput.sendKeys("Room101");

        WebElement submitButton = driver.findElement(By.id("addDeviceBtn"));
        submitButton.click();

    }

    @Given("the device is currently loaned to user Alice")
    public void the_device_is_currently_loaned_to_user_alice() throws InterruptedException {
        WebElement borrowerInput = driver.findElement(By.id("loanBorrowedBy"));
        borrowerInput.sendKeys("Alice");
        WebElement loanButton = driver.findElement(By.id("addLoanBtn"));
        loanButton.click();
        Thread.sleep(5000);
    }

    @When("the user attempts to delete the device")
    public void the_user_attempts_to_delete_the_device() throws InterruptedException {
        WebElement deleteButton = driver.findElement(By.xpath("//tr[td[contains(text(),'Projector')]]//button[contains(@onclick,'deleteDevice')]"));
        deleteButton.click();
        Thread.sleep(1000);
    }

    @Then("the device should not be deleted")
    public void the_device_should_not_be_deleted() {
        WebElement deviceTableRow = driver.findElement(By.id("devicesTableBody"));
        String tableContent = deviceTableRow.getText();
        boolean isDevicePresent = tableContent.contains("Projector");
        Assert.assertTrue(isDevicePresent, "Device should still be present in the table.");
    }

    @Then("an error message Failed to delete device should be displayed")
    public void an_error_message_failed_to_delete_device_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deviceMessage")));
        String messageText = errorMessage.getText();
        Assert.assertEquals(messageText, "Failed to delete device");
    }
}
