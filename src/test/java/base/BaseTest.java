package base;

import Utilities.AuthUtils;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

public class BaseTest {
    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://staging.prism-sfa-dev.net/";


    }
    protected String salespersonToken;

    public void loginAsSalesperson() {
        salespersonToken = AuthUtils.login("9918401438", "Test@123");
    }
}
