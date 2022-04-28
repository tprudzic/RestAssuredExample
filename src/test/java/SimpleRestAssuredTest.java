import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import pl.tprudzic.utils.EmailCases;
import pl.tprudzic.utils.EmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleRestAssuredTest {
    private final String URL = "https://gorest.co.in/public/v1/users";
    private static final String TOKEN = "7afa8764886599c1b14799bba7a8bf15833f464cb58f929343102213f6173889";
    private static final String NAME = "Tenali Raa";
    private static final String GENDER = "male";
    private static String email;
    private static final String STATUS = "active";
    private static RequestSpecification request;

    @BeforeAll
    static void testPrepare() throws JSONException{
        email = EmailGenerator.generateEmail(EmailCases.VALID, false);

        JSONObject requestParams = new JSONObject();
        requestParams.put("email", email);
        requestParams.put("name", NAME);
        requestParams.put("gender", GENDER);
        requestParams.put("status", STATUS);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + TOKEN);
        request.header("Content-Type", "application/json");
        request.body(requestParams.toString());
    }


    @Test
    @Order(1)
    void createUser() throws JSONException {
        Response createUserResponse = request.post(URL);

        createUserResponse
                .then()
                .assertThat()
                .statusCode(201);

        JSONObject createUserResponseJson = new JSONObject(createUserResponse.getBody().asString());
        String userId = createUserResponseJson.getJSONObject("data").get("id").toString();

        Response verifyCreateUserResponse = request.get(URL + "/" + userId);

        JSONObject getUserByIdResponseJson = new JSONObject(verifyCreateUserResponse.getBody().asString()).getJSONObject("data");

        assertThat(getUserByIdResponseJson.get("name"))
                .as("Expected different name")
                .isEqualTo(NAME);
        assertThat(getUserByIdResponseJson.get("gender"))
                .as("Expected different gender")
                .isEqualTo(GENDER);
        assertThat(getUserByIdResponseJson.get("email"))
                .as("Expected different email")
                .isEqualTo(email);
        assertThat(getUserByIdResponseJson.get("status"))
                .as("Expected different status")
                .isEqualTo(STATUS);
    }

    @Test
    @Order(2)
    void tryToCreateUserWithDuplicatedEmail() throws JSONException {
        Response createUserResponse = request.post(URL);

        createUserResponse
                .then()
                .assertThat()
                .statusCode(422);

        JSONObject createUserResponseJson = new JSONObject(createUserResponse.getBody().asString());
        JSONObject failureResponseJson = new JSONObject(createUserResponseJson.getJSONArray("data").get(0).toString());
        String problematicField = failureResponseJson.get("field").toString();
        String problemMessage = failureResponseJson.get("message").toString();

        assertSoftly(softly -> {
            softly.assertThat(problematicField)
                    .as("Response Json should have contained 'email' as problematic field!")
                    .isEqualTo("email");
            softly.assertThat(problemMessage)
                    .as("Problem with 'email' should have been caused by duplication!")
                    .isEqualTo("has already been taken");
        });
    }
}

