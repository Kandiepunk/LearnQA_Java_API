package tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Link;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserGetTest extends BaseTestCase {

    ApiCoreRequests apiCoreRequests = new ApiCoreRequests();


    @Test
    @DisplayName("Получение ограниченного списка данных без авторизации по пользователю")
    public void testGetUserDataNotAuth() {
        Response responseUserData = apiCoreRequests.makeGetRequest(
                BASE_URL + "/user/2",
                null,
                null);

        System.out.println(responseUserData.asString());

        Assertions.assertJsonHasField(responseUserData, "username");
        Assertions.assertJsonHasNotField(responseUserData, "firstName");
        Assertions.assertJsonHasNotField(responseUserData, "lastName");
        Assertions.assertJsonHasNotField(responseUserData, "email");
    }

    @Test
    @DisplayName("Получение всех данных пользователя с авторизацией по данному пользователю")
    public void testGetUserDetailsAuthAsSameUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = apiCoreRequests.makeGetRequest(
                BASE_URL + "/user/2",
                header,
                cookie);

        System.out.println(responseUserData.asString());

        String[] expectedFields = {"username", "firstName", "lastName", "email"};

        Assertions.assertJsonHasFields(responseUserData, expectedFields);

        //Проверка выше заменяет все проверки ниже, т.к. создали новый Assertion.
        Assertions.assertJsonHasField(responseUserData, "username");
        Assertions.assertJsonHasField(responseUserData, "firstName");
        Assertions.assertJsonHasField(responseUserData, "lastName");
        Assertions.assertJsonHasField(responseUserData, "email");
    }

    @Test
    @DisplayName("Получение ограниченного списка данных пользователя с авторизацией по другому пользователю")
    public void testGetUserDetailsAuthAsOtherUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/login",
                authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = apiCoreRequests.makeGetRequest(
                BASE_URL + "/user/124282",
                header,
                cookie);

        String[] unexpectedFields = {"password", "firstName", "lastName", "email"};

        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);
        Assertions.assertJsonHasField(responseUserData, "username");
    }
}
