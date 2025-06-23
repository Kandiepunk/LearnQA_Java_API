import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomeworkTests {

    @Test
    public void testEx5() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .andReturn();
        ArrayList<LinkedHashMap> messagesList = response.jsonPath().get("messages");
        System.out.println(messagesList.get(1));
    }

    @Test
    public void testEx6() {
        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn();

        System.out.println(response.getHeader("Location"));
    }

    @Test
    public void testEx7() {
        String url = "https://playground.learnqa.ru/api/long_redirect";
        while (true) {
            Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .get(url)
                .andReturn();

            System.out.println(url);

            int statusCode = response.getStatusCode();
            System.out.println(statusCode);

            String redirectUrl = response.getHeader("Location");
            if (redirectUrl != null & statusCode != 200) {
                url = redirectUrl;
            } else {
                break;
            }
        }
    }

    @Test
    public void testEx8() throws InterruptedException {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .andReturn();

        String token = response.jsonPath().get("token");
        int second = response.jsonPath().get("seconds");

        Response responseForStatus = RestAssured
                .given()
                .queryParam("token", token)
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .andReturn();

        String responseStatus = responseForStatus.jsonPath().get("status");
        if (responseStatus.equals("Job is NOT ready")) {
            Thread.sleep(second * 1000L);
        }

        responseForStatus = RestAssured
                .given()
                .queryParam("token", token)
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .andReturn();

        responseStatus = responseForStatus.jsonPath().get("status");
        String responseResult = responseForStatus.jsonPath().get("result");
        if (responseStatus.equals("Job is ready") & responseResult != null) {
            System.out.println("Статус: " + responseStatus + "\nРезультат:" + responseResult);
        }
    }


    @Test
    public void testEx9() {

        ArrayList<String> pass = new ArrayList<>(Arrays.asList(
                "123456", "123456789", "12345678", "password",
                "qwerty123", "qwerty1", "111111", "12345", "secret", "123123", "1234567890", "1234567", "000000",
                "qwerty", "abc123", "password1", "iloveyou", "11111111", "dragon", "monkey", "letmein", "trustno1",
                "baseball", "master", "sunshine", "ashley", "bailey", "passw0rd", "shadow", "654321", "superman",
                "qazwsx", "michael", "Football", "welcome", "jesus"));


        for (int i=0; i < pass.size() ; i++) {
            Response response = RestAssured
                    .given()
                    .queryParam("login", "super_admin")
                    .queryParam("password", pass.get(i))
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .andReturn();

            String responseCookies = response.getCookie("auth_cookie");

            Map<String, String> cookies = new HashMap<>();
            if (responseCookies != null) {
                cookies.put("auth_cookie", responseCookies);
            }

            Response responseCheckCookies = RestAssured
                    .given()
                    .cookies(cookies)
                    .post("https://playground.learnqa.ru/ajax/api/check_auth_cookie")
                    .andReturn();

            String responseCheckCookie = responseCheckCookies.getBody().htmlPath().get().toString();

            if(responseCheckCookie.equals("You are authorized")) {
                System.out.println(pass.get(i));
                responseCheckCookies.print();
                break;
            }

        }
    }

    @Test
    public void testEx10() {
        String hello = "Hello, world";
        assertTrue(hello.length() > 15, "В строке меньше 15 символов!");
    }

    @Test
    public void testEx11() {
        Response responseCheckCookies = RestAssured
                .get("https://playground.learnqa.ru/api/homework_cookie")
                .andReturn();
        Map<String, String> cookies = responseCheckCookies.getCookies();
        assertEquals("hw_value", cookies.get("HomeWork"), "Значение куки из ответа не соответствует HomeWork -> hw_value");
    }

    @Test
    public void testEx12() {
        Response responseCheckHeader = RestAssured
                .get("https://playground.learnqa.ru/api/homework_header")
                .andReturn();
        Headers headers = responseCheckHeader.getHeaders();
        assertEquals(
                "Some secret value",
                headers.getValue("x-secret-homework-header"),
                "Значение заголовка 'x-secret-homework-header' из ответа не соответствует значению 'Some secret value' или такой заголовок отсутствует ");
    }

    @ParameterizedTest
    @CsvSource({
            "'Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30', Mobile, No, Android",
            "'Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1', Mobile, Chrome, iOS",
            "'Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)', Googlebot, Unknown, Unknown",
            "'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0', Web, Chrome, No",
            "'Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1', Mobile, No, iPhone"
    })
    public void testEx13(String userAgent, String expectedPlatform, String expectedBrowser, String expectedDevice) {
        Response response = RestAssured
                .given()
                .header("User-Agent", userAgent)
                .get("https://playground.learnqa.ru/ajax/api/user_agent_check")
                .andReturn();

        response.prettyPrint();
        assertEquals(expectedPlatform, response.jsonPath().getString("platform"));
        assertEquals(expectedBrowser, response.jsonPath().getString("browser"));
        assertEquals(expectedDevice, response.jsonPath().getString("device"));

    }
}
