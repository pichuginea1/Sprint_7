import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginCourierTests {

	private String login;
	private String password;
	private String firstName;

	@Before
	public void setUp() {
		RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";

		login = "LoginCourier" + System.currentTimeMillis();
		password = "CourierPassword" + System.currentTimeMillis();
		firstName = "LoginName" + System.currentTimeMillis();

		createCourier(login, password, firstName);
	}

	@Test
	@DisplayName("Courier creation")
	@Description("Basic test for positive courier authentication")
	public void checkCourierAuthenticated() {
		Response loginResponse = loginCourier(login, password);

		loginResponse.then().assertThat().body("id", notNullValue())
				.and()
				.statusCode(200);
	}

	@Test
	@DisplayName("Login without mandatory password")
	@Description("Error will be returned if try to login password = null")
	public void checkCourierCanNotLoginWithoutPassword() {
		Response loginResponse = loginCourier(login, null);

		loginResponse.then().assertThat().body("message", equalTo("Учетная запись не найдена"))
				.and()
				.statusCode(404);
	}

	@Test
	@DisplayName("Login with incorrect password")
	@Description("Error will be returned if try to login with incorrect password")
	public void checkCourierCanNotLoginWithIncorrectPassword() {
		Response loginResponse = loginCourier(login, password + "1");

		loginResponse.then().assertThat().body("message", equalTo("Учетная запись не найдена"))
				.and()
				.statusCode(404);
	}

	@Test
	@DisplayName("Courier authentication without login field")
	@Description("Error must be returned if request body has no login field")
	public void checkErrorIfBodyHasNoMandatoryField() {
		String loginRequestBody = "{\"password\":\"" + password + "\"}";
		Response loginResponse = given()
				.header("Content-type", "application/json")
				.and()
				.body(loginRequestBody)
				.when()
				.post("/api/v1/courier/login")
				.then()
				.extract().response();

		loginResponse.then().assertThat().body("message", equalTo("Недостаточно данных для входа"))
				.and()
				.statusCode(400);
	}

	@Test
	@DisplayName("Login with non existing user")
	@Description("Error must be returned if try to authenticate with non existing user")
	public void checkErrorForLoginNonExistingUser() {
		Response loginResponse = loginCourier(login + "1", password);

		loginResponse.then().assertThat().body("message", equalTo("Учетная запись не найдена"))
				.and()
				.statusCode(404);
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
	public Response loginCourier(String login, String password) {
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
	@Step("Send DELETE request to /api/v1/courier/:id")
	public void deleteCourier() {
		//delete courier
		given()
				.header("Content-type", "application/json")
				.when()
				.delete("/api/v1/courier/" + loginCourier(login, password).jsonPath().getString("id"))
				.then()
				.extract().response();
	}
}
