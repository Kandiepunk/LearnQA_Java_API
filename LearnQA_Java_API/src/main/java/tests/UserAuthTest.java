package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import lib.ApiCoreRequests.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Authorization cases")
@Feature("Authorization")
public class UserAuthTest extends BaseTestCase {

    String cookie;
    String header;
    int userIdOnAuth;

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    public void loginUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

//        Response responseGetAuth = RestAssured
//                .given()
//                .body(authData)
//                .post(BASE_URL + "/user/login")
//                .andReturn();

        //код выше заменён этим из-за создания вспомогательных методов в классе lib/ApiCoreRequests.java
        Response responseGetAuth = apiCoreRequests.
                makePostRequest(BASE_URL + "/user/login", authData);

        this.cookie = this.getCookie(responseGetAuth, "auth_sid");
        this.header = this.getHeader(responseGetAuth,"x-csrf-token");
        this.userIdOnAuth = this.getIntFromJson(responseGetAuth,"user_id");
    }

    @Test
    @Description("Тест успешно авторизуется по почте и паролю")
    @DisplayName("Тест положительной авторизации пользователя")
    public void testAuthUser() {
//        Response responseCheckAuth = RestAssured
//                .given()
//                .header("x-csrf-token", this.header)
//                .cookie("auth_sid", this.cookie)
//                .get(BASE_URL + "/user/auth")
//                .andReturn();

        //код выше заменён этим из-за создания вспомогательных методов в классе lib/ApiCoreRequests.java
        Response responseCheckAuth = apiCoreRequests.
                makeGetRequest(BASE_URL + "/user/auth",
                        this.header,
                        this.cookie);

        Assertions.assertJsonByName(responseCheckAuth, "user_id", this.userIdOnAuth);
    }

    @Description("Тест проверяет статус авторизации без переданных куки и токена")
    @DisplayName("Тест не проходящий авторизацию пользователя")
    @ParameterizedTest
    @ValueSource(strings = {"cookie", "headers"})
    public void testNegativeAuthUser(String condition) {

        if (condition.equals("cookie")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithCookie(
                    BASE_URL + "/user/auth",
                    this.cookie);
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else if (condition.equals("headers")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithToken(
                    BASE_URL + "/user/auth",
                    this.header);
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else {
            throw new IllegalArgumentException("Condition value is know: " + condition);
        }

        // Тест ниже полностью заменился из-за создания вспомогательных методов в классе lib/ApiCoreRequests.java
//        RequestSpecification spec = RestAssured.given();
//        spec.baseUri(BASE_URL + "/user/auth");
//
//        if (condition.equals("cookie")) {
//            spec.cookie("auth_sid", this.cookie);
//        } else if (condition.equals("headers")) {
//            spec.header("x-csrf-token", this.header);
//        } else {
//            throw new IllegalArgumentException("Condition value is know: " + condition);
//        }
//
//        Response responseForCheck = spec.get().andReturn();
//        Assertions.assertJsonByName(responseForCheck, "user_id", 0);
    }
}
