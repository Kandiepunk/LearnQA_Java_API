package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserEditTest extends BaseTestCase {

    ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    @DisplayName("Успешное изменение имени пользователя")
    public void testEditJustCreatedTest() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/", userData);

        responseCreateAuth.prettyPrint();

        String userId = getStringFromJson(responseCreateAuth, "id");

        //LOGIN
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

        //EDIT
        String newName = "Change Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);
//        editData = DataGenerator.getRegistrationData(editData);

        Response responseEditName = apiCoreRequests.makePutRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie,
                editData);


        System.out.println("EDIT = " + responseEditName.asString());

        //GET
        Response responseUserData = apiCoreRequests.makeGetRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie
        );

        System.out.println("GET = " + responseUserData.asString());
        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    @DisplayName("Невозможность изменения данных пользователя без авторизации")
    public void testEditUserDataNotAuth() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/", userData);

        String userId = getStringFromJson(responseCreateAuth, "id");

        //EDIT
        String newName = "Change Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditName = apiCoreRequests.makePutRequest(
                BASE_URL + "/user/" + userId,
                null,
                null,
                editData);

        Assertions.assertResponseCodeEquals(responseEditName, 400);
        Assertions.assertJsonByName(responseEditName, "error", "Auth token not supplied");
    }

    @Test
    @DisplayName("Невозможность изменения данных пользователя авторизованным из другого пользователя")
    public void testEditUserDataAuthAsOtherUser() {
        //GENERATE FIRST USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/", userData);

        String userId = getStringFromJson(responseCreateAuth, "id");

        //GENERATE AND LOGIN SECOND USER
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

        //EDIT
        String newName = "Change Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditName = apiCoreRequests.makePutRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie,
                editData);

        System.out.println("FIRST USER EDIT = " + responseEditName.asString());


        Assertions.assertResponseCodeEquals(responseEditName, 400);
        Assertions.assertJsonByName(responseEditName, "error", "This user can only edit their own data.");
    }

    @Test
    @DisplayName("Невозможность изменения email пользователя на email без символа @")
    public void testEditIncorrectEmail() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/", userData);

        responseCreateAuth.prettyPrint();

        String userId = getStringFromJson(responseCreateAuth, "id");

        //LOGIN
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

        //EDIT
        String newEmail = "maxexample.com";
        Map<String, String> editData = new HashMap<>();
        editData.put("email", newEmail);

        Response responseEditName = apiCoreRequests.makePutRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie,
                editData);


        System.out.println("EDIT = " + responseEditName.asString());

        Assertions.assertResponseCodeEquals(responseEditName, 400);
        Assertions.assertJsonByName(responseEditName, "error", "Invalid email format");

        //GET
        Response responseUserData = apiCoreRequests.makeGetRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie
        );

        System.out.println("GET = " + responseUserData.asString());
        Assertions.assertJsonByName(responseUserData, "email", userData.get("email"));
    }

    @Test
    @DisplayName("Невозможность изменения имени пользователя на имя в один символ")
    public void testEditVeeryShortFirstName() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/", userData);

        responseCreateAuth.prettyPrint();

        String userId = getStringFromJson(responseCreateAuth, "id");

        //LOGIN
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

        //EDIT
        String newFirstName = "X";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newFirstName);

        Response responseEditName = apiCoreRequests.makePutRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie,
                editData);


        System.out.println("EDIT = " + responseEditName.asString());

        Assertions.assertResponseCodeEquals(responseEditName, 400);
        Assertions.assertJsonByName(responseEditName, "error", "The value for field `firstName` is too short");

        //GET
        Response responseUserData = apiCoreRequests.makeGetRequest(
                BASE_URL + "/user/" + userId,
                header,
                cookie
        );

        System.out.println("GET = " + responseUserData.asString());
        Assertions.assertJsonByName(responseUserData, "firstName", userData.get("firstName"));
    }
}
