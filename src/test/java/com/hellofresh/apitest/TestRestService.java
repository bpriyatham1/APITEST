package com.hellofresh.apitest;

import static io.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestRestService {

	public static final Logger logger = Logger.getLogger(TestRestService.class.getName());

	@BeforeTest
	public void setup() {

		String log4jConfPath = "log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);

		RestAssured.baseURI = System.getProperty("base-url", "http://services.groupkt.com");
	}

	@Test
	public void GetAllCountries() {
		RequestSpecification httpRequest = RestAssured.given();
		Response response = httpRequest.request(Method.GET, "/country/get/all");
		String responseBody = response.getBody().asString();
		System.out.println("Response Body returning all Countries =>  " + responseBody);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 200, "Correct status code returned");
	}

	@Test
	public void validateGetAllCountries() {
		expect().body("RestResponse.result.alpha2_code", hasItems("US", "DE", "GB")).and().statusCode(200).when()
				.get("/country/get/all");
		logger("Successfully validated multiple countries in the response.");
	}

	@DataProvider(parallel = true)
	public Object[][] getCountriesForSearch() {
		Object[][] countryToSearch = { { "United States", "US" }, { "Germany", "DE" }, { "Great Britain", "GB" } };
		return countryToSearch;
	}

	@Test(dataProvider = "getCountriesForSearch")
	public void validateIndividualCountry(String countryName, String countryCode) {
		expect().body("RestResponse.result.alpha2_code", equalTo(countryCode)).and().statusCode(200).given()
				.pathParam("countryCode", countryCode).when().get("/country/get/iso2code/{countryCode}");

	}

	@Test()
	public void validateResponseForInexistentCountry() {
		String countryCode = "notfound";
		expect().body("RestResponse.messages",
				contains(String.format("No matching country found for requested code [%s].", countryCode))).and()
				.statusCode(200).given().pathParam("countryCode", countryCode).when()
				.get(String.format("/country/get/iso2code/{countryCode}", countryCode));

		 logger("Successfully validated API response for inexistent country.");
	}

	@Test()
	public void tryCreateNewCountry() {

		String postPath = "/country/create";

		String postBody = "{\r\n" + "\"name\": \"Test Country\",\r\n" + "\"alpha2_code\": \"TC\",\r\n"
				+ "\"alpha3_code\": \"TCY\"\r\n" + "}";

		RestAssured.given().contentType(ContentType.JSON).body(postBody).post(postPath).then()
				// Assert Status Code 201 to make sure new entry is created whicch should be
				// failed
				.statusCode(201);

		 logger("Successfully validated create country API response.");
	}

	public void logger(String data) {
		logger.info(data);
		Reporter.log(data + "\n");
	}
}