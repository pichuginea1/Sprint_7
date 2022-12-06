import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public class CreateCourierTests {

	private String login;
	private String password;
	private String firstName;

	@Before
	public void setUp() {
		RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";

		login = "CreateCourier" + System.currentTimeMillis();
		password = "CreatePassword" + System.currentTimeMillis();
		firstName = "CreateName" + System.currentTimeMillis();
	}

	@Test
	@DisplayName("Courier creation")
	@Description("Basic test for positive courier creation")
	public void checkCourierCreation() {
		Response response = createCourier(login, password, firstName);
		Assert.assertEquals(201, response.statusCode());
		Assert.assertEquals("true", response.jsonPath().getString("ok"));
	}

	@Test
	@DisplayName("Can not create two the same couriers")
	@Description("Negative test for two the same couriers creation")
	public void checkCanNotCreateTwoTheSameCouriers() {
		Response firstCourier = createCourier(login, password, firstName);
		Assert.assertEquals(201, firstCourier.statusCode());
		Assert.assertEquals("true", firstCourier.jsonPath().getString("ok"));

		Response secondCourier = createCourier(login, password, firstName);
		Assert.assertEquals(409, secondCourier.statusCode());
		Assert.assertEquals("Этот логин уже используется. Попробуйте другой.", secondCourier.jsonPath().getString("message"));
	}

	@Test
	@DisplayName("Create courier with mandatory fields")
	@Description("Courier will be created if firstName is null")
	public void checkCourierCreatedWithMandatoryFields() {
		Response firstCourier = createCourier(login, password, null);
		Assert.assertEquals(201, firstCourier.statusCode());
		Assert.assertEquals("true", firstCourier.jsonPath().getString("ok"));
	}

	@Test
	@DisplayName("Request body without a field")
	@Description("Error must be returned if request body has no password field")
	public void checkErrorIfBodyHasNoMandatoryField() {
		String requestBody = "{ \"login\" : \"" + login + "\", \"firstName\":\"" + firstName + "\"}";

		Response response = given()
				.header("Content-type", "application/json")
				.and()
				.body(requestBody)
				.when()
				.post("/api/v1/courier")
				.then()
				.extract().response();

		Assert.assertEquals(400, response.statusCode());
		Assert.assertEquals("Недостаточно данных для создания учетной записи", response.jsonPath().getString("message"));
	}

	@Test
	@DisplayName("Create courier with existing login")
	@Description("Error must be returned if try to create a courier with existing login")
	public void checkErrorForCourierCreationWithExistingLogin() {
		Response firstCourier = createCourier(login, password, firstName);
		Assert.assertEquals(201, firstCourier.statusCode());
		Assert.assertEquals("true", firstCourier.jsonPath().getString("ok"));

		Response secondCourier = createCourier(login, password + "1", firstName + "1");
		Assert.assertEquals(409, secondCourier.statusCode());
		Assert.assertEquals("Этот логин уже используется. Попробуйте другой.", secondCourier.jsonPath().getString("message"));
	}

	@Step("Create courier. Send POST request to /api/v1/courier")
	public Response createCourier(String login, String password, String firstName) {
		String requestBody = "{ \"login\" : \"" + login + "\", \"password\":\"" + password + "\", \"firstName\":\"" + firstName + "\"}";

		return given()
				.header("Content-type", "application/json")
				.and()
				.body(requestBody)
				.when()
				.post("/api/v1/courier")
				.then()
				.extract().response();
	}

	@Step("Login courier. Send POST request to /api/v1/courier/login")
	public Response loginCourier(){
		String loginRequestBody = "{ \"login\" : \"" + login + "\", \"password\":\"" + password + "\"}";

		return given()
				.header("Content-type", "application/json")
				.and()
				.body(loginRequestBody)
				.when()
				.post("/api/v1/courier/login")
				.then()
				.extract().response();
	}

	@After
	@Step("Delete courier. Send DELETE request to /api/v1/courier/:id")
	public void deleteCourier() {
		given()
				.header("Content-type", "application/json")
				.when()
				.delete("/api/v1/courier/" + loginCourier().jsonPath().getString("id"))
				.then()
				.extract().response();
	}

}
