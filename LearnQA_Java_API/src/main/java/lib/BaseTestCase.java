package lib;

import io.qameta.allure.Step;
import io.restassured.http.Headers;
import io.restassured.response.Response;

import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaseTestCase {

    protected final String BASE_URL = "https://playground.learnqa.ru/api";

    @Step("Получаем заголовок из ответа по переданному значению")
    protected String getHeader(Response Response, String name) {
        Headers headers = Response.getHeaders();

        assertTrue(headers.hasHeaderWithName(name), "Response doesn't have header with name " + name);
        return headers.getValue(name);
    }

    @Step("Получаем куку из ответа по переданному значению")
    protected String getCookie(Response Response, String name) {
        Map<String,String> cookies = Response.getCookies();

        assertTrue(cookies.containsKey(name), "Response doesn't have cookie with name " + name);
        return cookies.get(name);
    }

    @Step("Получаем числовое значение из ответа по переданному ключу")
    protected int getIntFromJson(Response Response, String name) {
        Response.then().assertThat().body("$", hasKey(name));
        return Response.jsonPath().getInt(name);
    }

    @Step("Получаем стоковое значение из ответа по переданному ключу")
    protected String getStringFromJson(Response Response, String name) {
        Response.then().assertThat().body("$", hasKey(name));
        return Response.jsonPath().getString(name);
    }
}
