import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class APIAutomationTest {
    public static final String CURRENCIES_ENDPOINT_BASE =
            "https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest";
    public static final String BRITISH_POUND = "gbp";
    public static final String UNITED_STATES_DOLLAR = "usd";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = CURRENCIES_ENDPOINT_BASE;
        RestAssured.requestSpecification =
                given()
                        .contentType(ContentType.JSON)
                        .urlEncodingEnabled(false);
    }

    @Test
    public void currenciesListTest() {
        Map<String, String> currenciesMap = when()
                .get("/currencies.json")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response().body().jsonPath().getMap("");

        Assert.assertTrue(currenciesMap.size() > 20,
                "Response doesn't have the expected size");
        Assert.assertTrue(currenciesMap.containsKey(BRITISH_POUND),
                String.format("Response doesn't contain %s", BRITISH_POUND));
        Assert.assertTrue(currenciesMap.containsKey(UNITED_STATES_DOLLAR),
                String.format("Response doesn't contain %s", UNITED_STATES_DOLLAR));

        //Check how many elements the API returns, save all abbreviations to an ArrayList.
        List<String> abbreviations = new ArrayList<>(currenciesMap.keySet());
        HashMap<Integer, Object> noOfCurrenciesMap = new HashMap<>();

        //getting currency list for all currencies
        for (String currency : abbreviations) {
            Integer noOfCurrencyPairs = given()
                    .pathParam("currency", currency)
                    .when()
                    .get("/currencies/{currency}.json")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .body()
                    .jsonPath().getMap("'" + currency + "'").size();

            noOfCurrenciesMap.put(noOfCurrencyPairs, currency);
        }

        Assert.assertEquals(noOfCurrenciesMap.size(), 1,
                String.format("Expecting map %s to have only one item", noOfCurrenciesMap));

    }
}
