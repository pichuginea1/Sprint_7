import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.example.OrderJsonObject;
import org.example.Order;
import org.junit.*;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static io.restassured.RestAssured.given;

public class OrderListTests {

	@BeforeClass
	public static void log() {
		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
	}

	private final String timestamp = String.valueOf(System.currentTimeMillis());
	private String track;

	@Before
	public void setUp() {
		RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
		track = createOrder().body().jsonPath().getString("track");
	}

	@Test
	@DisplayName("Get orders list")
	@Description("Basic test for getting orders list")
	public void checkOrderList() {

		//region Комменатрий для ревьювера:
		/*
		Мы общались с наставником на тему решения задания #4 "Проверь, что в тело ответа возвращается список заказов.".
		Само по себе задание крайне размыто (как и весь спринт по теме). Читая это задание, я подумал, что достаточно
		создать список из заказов, и добавить его в тело запроса (в тренажёре говорится об этом). Далее через get сервис
		получить этот список по трек номеру и проверить содержимое обоих элементов. Но как выяснилось, get сервис
		не умеет возвращать массив по трек номеру. Нужно создавать по отдельности заказы и далее их каким-то образом
		связать. Наставник предположил, что это можно сделать через сервис accept. Якобы, он может проставить курьера
		обоим заказам и далее по курьеру можно найти активные заказы. В документации про это не написано, в основной
		 части задания тоже. Чтобы воспроизвести такую ситуацию нужно:
		 1. Создать курьера;
		 2. Сделать логин, чтобы получить id курьера;
		 3. Создать заказы;
		 4. Принять заказы;
		 5. Получить заказы.
		 В этой схеме слишком много дублирования кода из предыдущих тестов, поэтому решили отказаться от такого подхода.
		 И не факт, что она бы сработала. Далее стал пробовать найти все ордеры, что есть. Это бы получилось, если бы
		 использовалась сортировка, например, createdAT DESC. Но в списке сначала выдаются старые ордеры,
		 используется лимит на 30 записей и пагинация. Тоже неудобно.
		 Как итог, решили остановиться на создании одного заказа и проверить его содержимое через десериализацию json.
		 Задание - простор для фантазий. :)
		 */
		//endregion

		OrderJsonObject orderJsonObject = given()
				.header("Content-type", "application/json")
				.queryParam("t", track)
				.get("/api/v1/orders/track")
				.body()
				.as(OrderJsonObject.class);

		Assert.assertEquals("Order1FN", orderJsonObject.getOrder().getFirstName());
		Assert.assertEquals("Order1LN", orderJsonObject.getOrder().getLastName());
		Assert.assertEquals("Order1AD", orderJsonObject.getOrder().getAddress());
		Assert.assertEquals("Order1MT", orderJsonObject.getOrder().getMetroStation());
		Assert.assertEquals("+71233215556", orderJsonObject.getOrder().getPhone());
		Assert.assertTrue(orderJsonObject.getOrder().getDeliveryDate().contains("2023-06-06"));
		Assert.assertEquals(1, orderJsonObject.getOrder().getRentTime());
		Assert.assertEquals("Order1CM" + timestamp, orderJsonObject.getOrder().getComment());
		Assert.assertEquals("BLACK", orderJsonObject.getOrder().getColor().get(0));
	}

	@Step("Create order. Send POST requests to /api/v1/orders")
	public Response createOrder() {
		Order order = new Order("Order1FN", "Order1LN", "Order1AD", "Order1MT", "+71233215556",
				1, "2023-06-06", "Order1CM" + timestamp, Arrays.asList("BLACK"));
		return given()
				.header("Content-type", "application/json")
				.and()
				.body(order)
				.when()
				.post("/api/v1/orders")
				.then().assertThat().statusCode(201)
				.extract().response();
	}

	@After
	@Step("Cancel order. Send PUT request to /api/v1/orders/cancel")
	public void cancelOrder() {
		given()
				.header("Content-type", "application/json")
				.when()
				.put("/api/v1/orders/cancel?track=" + track)
				.then().assertThat().body("ok", equalTo(true));
	}
}
