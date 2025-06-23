package tests;

import io.restassured.response.Response;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRegisterTest extends BaseTestCase {

    ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    @DisplayName("Невозможность создания нового пользователя с существующей почтой")
    @Order(1)
    public void testCreateUserWithExistingEmail() {
        String email = "vinkotov@example.com";


        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/",
                userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
    }

    @Test
    @DisplayName("Успешное создания нового пользователя")
    @Order(2)
    public void testCreateUserSuccessfully() {
        String email = DataGenerator.getRandomEmail();
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/",
                userData);

        System.out.println(responseCreateAuth.asString());

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");
    }

    @Test
    @DisplayName("Невозможность создания нового пользователя с почтой без @")
    @Order(3)
    public void testCreateWithIncorrectEmail() {
        String email = "maxkoexample.com";
        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/",
                userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Invalid email format");
    }

    @ParameterizedTest(name = "Создание пользователя без {argumentsWithNames}")
    @ValueSource(strings = {"username", "password", "firstName", "lastName", "email"})
    @DisplayName("Невозможность создания нового пользователя без обязательных передаваемых параметров")
    @Order(4)
    public void testCreateWithoutOnceField(String field) {
        Map<String, String> userData = new HashMap<>();
        userData.put(field, null);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/",
                userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The following required params are missed: " + field);
    }

    @Test
    @DisplayName("Невозможность создания нового пользователя с именем длиной более 250 символов")
    @Order(5)
    public void testCreateWithVeryLongName() {
        String firstName = DataGenerator.getRandomString(255);
        Map<String, String> userData = new HashMap<>();
        userData.put("firstName", firstName);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(
                BASE_URL + "/user/",
                userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'firstName' field is too long");
    }
}
