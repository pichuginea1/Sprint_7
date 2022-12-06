import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderTests {

	@Before
	public void setUp() {
		RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
	}

	private String firstName;
	private String lastName;
	private String address;
	private String metroStation;
	private String phone;
	private int rentTime;
	private String deliveryDate;
	private String comment;
	private List<String> color;
	private Response createOrderResponse;

	public CreateOrderTests(String firstName, String lastName, String address, String metroStation, String phone, int rentTime, String deliveryDate, String comment, List color) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.metroStation = metroStation;
		this.phone = phone;
		this.rentTime = rentTime;
		this.deliveryDate = deliveryDate;
		this.comment = comment;
		this.color = color;
	}

	@Parameterized.Parameters(name = "firstName = {0}, lastName = {1}, address = {2}, metroStation = {3}, phone = {4}, rentTime = {5}, " +
			"deliveryDate = {6}, comment = {7}, color = {8}")
	public static Object[][] getData() {
		return new Object[][]{
				{"Василий", "Иванов", "Типанова 25-65", "Черкизовская", "+79111355555", 2, "01.12.2022", "Comment" + System.currentTimeMillis(), Arrays.asList("BLACK", "GREY")},
				{"Иван", "Васильев", "Ленина 38-78", "Бульвар Рокоссовского", "+79219211229", 12, "11.12.2022", "Comment" + System.currentTimeMillis(), Arrays.asList("GREY")},
				{"Пётр", "Петров", "Петрова 1-10", "Юго-Западная", "+79991399999", 3, "02.12.2022", "Comment" + System.currentTimeMillis(), null},
		};
	}

	@Test
	@DisplayName("Create order")
	@Description("Positive parametrized cases for order creation")
	public void checkOrderCreation() {
		Order orderBody = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
		createOrderResponse = given()
				.header("Content-type", "application/json")
				.and()
				.body(orderBody)
				.when()
				.post("/api/v1/orders")
				.then()
				.extract().response();

		createOrderResponse.then().assertThat().body("track", notNullValue())
				.and()
				.statusCode(201);
	}

	@After
	@Step("Cancel order. Send PUT request to /api/v1/orders/cancel")
	public void cancelOrder() {
		given()
				.header("Content-type", "application/json")
				.when()
				.put("/api/v1/orders/cancel?track=" + createOrderResponse.jsonPath().getString("track"))
				.then().assertThat().body("ok", equalTo(true));
	}
}
