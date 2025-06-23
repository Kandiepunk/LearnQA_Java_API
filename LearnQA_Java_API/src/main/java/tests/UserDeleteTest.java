package tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserDeleteTest extends BaseTestCase {

    ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    @DisplayName("Тест на невозможность удалить пользователя с Id = 2")
    public void testDeleteUserWithId2() {
        Allure.step("Логин в пользователя vinkotov@example.com");
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/login",
                authData
        );

        System.out.println("LOGIN = " + responseGetAuth.asString());

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Allure.step("Удаление пользователя vinkotov@example.com");
        Response responseDelete = apiCoreRequests.makeDeleteRequest(
                BASE_URL + "/user/2",
                header,
                cookie);

        System.out.println("DELETE = " + responseDelete.asString());

        Assertions.assertResponseCodeEquals(responseDelete, 400);
        Assertions.assertJsonByName(
                responseDelete,
                "error",
                "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
    }

    @Test
    @DisplayName("Тест на успешное удаление пользователя")
    public void testDeleteNewUser() {
        Allure.step("Создание дефолтного пользователя");
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/", userData);

        responseCreateAuth.prettyPrint();

        String userId = getStringFromJson(responseCreateAuth, "id");

        Allure.step("Логин в дефолтного пользователя");
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/login",
                authData
        );

        System.out.println("LOGIN = " + responseGetAuth.asString());

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Allure.step("Удаление дефолтного пользователя");
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie);


        System.out.println("DELETE = " + responseDeleteUser.asString());

        Assertions.assertJsonByName(responseDeleteUser, "success", "!");

        Allure.step("Получение данных дефолтного пользователя");
        Response responseUserData = apiCoreRequests.makeGetRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie
        );

        System.out.println("GET = " + responseUserData.asString());

        Assertions.assertResponseTextEquals(responseUserData, "User not found");
    }

    @Test
    @DisplayName("Тест на невозможность удаления пользователя авторизованным из другого пользователя")
    public void testDeleteUserAuthAsOtherUser() {
        Allure.step("Создание первого пользователя");
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/", userData);

        String userId = getStringFromJson(responseCreateAuth, "id");

        Allure.step("Создание и логин во второго пользователя");
        Map<String, String> secondUserData = DataGenerator.getRegistrationData();
        Response responseCreateAuthSecond = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/", secondUserData);

        Map<String, String> authData = new HashMap<>();
        authData.put("email", secondUserData.get("email"));
        authData.put("password", secondUserData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/login",
                authData
        );

        System.out.println("SECOND USER LOGIN = " + responseGetAuth.asString());

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Allure.step("Удаление первого пользователя");
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie);

        System.out.println("FIRST USER DELETE = " + responseDeleteUser.asString());


        Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
        Assertions.assertJsonByName(responseDeleteUser, "error", "This user can only delete their own account.");
    }
}
