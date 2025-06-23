import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelloWorldTest {

    @Test
    public void testHelloFrom() {
        System.out.println("Hello from Maxim");
    }

    @Test
    public void testRestAssured() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "Max");


        Response response = RestAssured
                .given()
//                .queryParam("name", "Max")
                .queryParams(params)
                .get("https://playground.learnqa.ru/api/hello")
                .andReturn();
        response.prettyPrint();
    }

    @Test
    public void testRestAssuredJson() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "Max");


        JsonPath response = RestAssured
                .given()
//                .queryParam("name", "Max")
                .queryParams(params)
                .get("https://playground.learnqa.ru/api/hello")
                .jsonPath();

        String name = response.get("answer");
        if (name == null) {
            System.out.println("the key 'answer2' is absent");
        } else {
            System.out.println(name);
        }
    }

    @Test
    public void testRestAssuredAPICHeckType() {
        Response response = RestAssured
                .given()
//                .body("param1=value&param2=value2")
                .body("{\"param1\":\"value\",\"param2\":\"value2\"}")
                // или в теле запрос в JSON формате (сверху) или отдельными queryParam - ами (автоматом приведет в JSON формат) (снизу)
//                .queryParam("param1", "value1")
//                .queryParam("param2", "value2")
                .post("https://playground.learnqa.ru/api/check_type")
                .andReturn();
        response.print();
    }

    @Test
    public void testRestAssuredAPICHeckTypeWithHashMap() {
        Map<String, Object> body = new HashMap<>();
        body.put("param1", "value1");
        body.put("param2", "value2");

        Response response = RestAssured
                .given()
                .body(body)
                .post("https://playground.learnqa.ru/api/check_type")
                .andReturn();
        response.print();
    }

    @Test
    public void testRestAssuredAPICHeckTypeStatusCode() {
        Map<String, String> headers = new HashMap<>();
        headers.put("myHeaders1", "myValue1");
        headers.put("myHeaders2", "myValue2");

        Response response = RestAssured
                .post("https://playground.learnqa.ru/api/check_type")
                .andReturn();

        int statusCode = response.getStatusCode();
        System.out.println(statusCode);


        response = RestAssured
                .post("https://playground.learnqa.ru/api/get_500")
                .andReturn();

        statusCode = response.getStatusCode();
        System.out.println(statusCode);

        response = RestAssured
                .get("https://playground.learnqa.ru/api/some")
                .andReturn();

        statusCode = response.getStatusCode();
        System.out.println(statusCode);

        response = RestAssured
                .given()
                .redirects()
                .follow(true)
                .get("https://playground.learnqa.ru/api/get_303")
                .andReturn();

        statusCode = response.getStatusCode();
        System.out.println(statusCode);

        response = RestAssured
                .given()
                .redirects()
                .follow(true)
                .get("https://playground.learnqa.ru/api/show_all_headers")
                .andReturn();

        response.prettyPrint();

        Headers responseHeaders = response.getHeaders();
        System.out.println(responseHeaders);

        response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .get("https://playground.learnqa.ru/api/get_303")
                .andReturn();

        response.prettyPrint();


        responseHeaders = response.getHeaders();
        System.out.println("Все заголовки от сервера = " + responseHeaders);
        String locationHeader = response.getHeader("Location");
        System.out.println("Ответ от сервера = " + locationHeader);

    }

    @Test
    public void testRestAssuredCookies() {
        Map<String, String> data = new HashMap<>();
        data.put("login", "secret_login");
        data.put("password", "secret_pass");

        Response responseForGet = RestAssured
                .given()
                .body(data)
                .when()
                .post("https://playground.learnqa.ru/api/get_auth_cookie")
                .andReturn();

        System.out.println("\nPretty text:");
        responseForGet.prettyPrint();

        System.out.println("\nHeaders:");
        Headers responseHeaders = responseForGet.getHeaders();
        System.out.println(responseHeaders);

        System.out.println("\nCookies:");
        Map<String, String> responseCookies = responseForGet.getCookies();
        System.out.println(responseCookies);

        //Выводим только значение куки "auth_cookie"
        //Выводим и запоминаем куку
        String responseCookie = responseForGet.getCookie("auth_cookie");
        System.out.println(responseCookie);

        Map<String, String> cookies = new HashMap<>();
        if (responseCookie != null) {
            cookies.put("auth_cookie", responseCookie);
        }


        //Создаем второй запрос с передачей куки
        Response responseForCheck = RestAssured
                .given()
                .body(data)
                .cookies(cookies)
                .when()
                .post("https://playground.learnqa.ru/api/check_auth_cookie")
                .andReturn();

        responseForCheck.print();
    }

    @Test
    public void testFor200() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/map")
                .andReturn();
//        assertTrue(response.statusCode() == 200,"Unexpected status code");
        assertEquals(200, response.statusCode(),"Unexpected status code");
    }

    @Test
    public void testFor404() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/map2")
                .andReturn();
//        assertTrue(response.statusCode() == 200,"Unexpected status code");
        assertEquals(404, response.statusCode(),"Unexpected status code");
    }

    @Test
    public void testHelloMethodWithoutName() {
        JsonPath response = RestAssured
                .get("https://playground.learnqa.ru/api/hello")
                .jsonPath();
        String answer = response.getString("answer");
        assertEquals("Hello, someone", answer, "The answer is not expected");

    }

    @Test
    public void testHelloMethodWithName() {
        String name = "Username";
        JsonPath response = RestAssured
                .given()
                .queryParam("name", name)
                .get("https://playground.learnqa.ru/api/hello")
                .jsonPath();
        String answer = response.getString("answer");
        assertEquals("Hello, " + name, answer, "The answer is not expected");

    }

    @ParameterizedTest
    @ValueSource(strings = {"", "John", "Pete"})
    public void testHelloMethodWithNameParameterized(String name) {
        Map<String, String> queryParams = new HashMap<>();

        if (name.length() > 0) {
            queryParams.put("name", name);
        }

        JsonPath response = RestAssured
                .given()
                .queryParams(queryParams)
                .get("https://playground.learnqa.ru/api/hello")
                .jsonPath();
        String answer = response.getString("answer");
        String expectedName = (name.length() > 0) ? name : "someone";
        assertEquals("Hello, " + expectedName, answer, "The answer is not expected");

    }
}
