package base;

import Utilities.RestUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;

import static Utilities.RestUtils.SALESPERSON_TOKEN;

public class BaseTest {
    @BeforeClass
    public void setUp() {
        if (SALESPERSON_TOKEN == null) {
            RestUtils.login("9918401438", "Test@123");
        }
        if (SALESPERSON_TOKEN == null) {
            throw new RuntimeException("Token was not generated. Check login.");
        }
    }
    @AfterSuite
    public void tearDown() {
        RestUtils.resetToken();
    }
}
