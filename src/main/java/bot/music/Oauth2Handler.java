package bot.music;

import java.io.IOException;
import java.util.UUID;

import com.grack.nanojson.JsonWriter;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;

import bot.Bot;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import general.Main;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class Oauth2Handler {

    private static final String CLIENT_ID = "861556708454-d6dlm3lh05idd8npek18k6be8ba3oc68.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "SboVhoG9s0rNafixCSGGKXAT";
    private static final String SCOPES = "https://www.googleapis.com/auth/youtube";
    private static final String OAUTH_FETCH_CONTEXT_ATTRIBUTE = "yt-oauth";

    private final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createCookielessThreadLocalManager();

    private String oauth2refreshtoken;

    public Oauth2Handler() {
        this.oauth2refreshtoken = Bot.instance.configWorker.getBotConfig("oauth2refreshtoken").get(0);
        while(!isRefreshTokenValid()){
            generateTokenManually();
        }
    }

    public void setToken(String token){
        this.oauth2refreshtoken = token;
        if(!Bot.instance.configWorker.setBotConfig("oauth2refreshtoken", token))
            Main.error("Error when setting the oauth2refreshtoken into BotConfig.");
    }

    public String getRefreshToken(){
        return this.oauth2refreshtoken;
    }

    private boolean isRefreshTokenValid() {
        if (oauth2refreshtoken == null || oauth2refreshtoken.isEmpty()) {
            return false;
        }

        String requestJson = JsonWriter.string()
                .object()
                .value("client_id", CLIENT_ID)
                .value("client_secret", CLIENT_SECRET)
                .value("refresh_token", this.oauth2refreshtoken)
                .value("grant_type", "refresh_token")
                .end()
                .done();

        HttpPost request = new HttpPost("https://www.youtube.com/o/oauth2/token");
        StringEntity entity = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
        request.setEntity(entity);
        try (HttpInterface httpInterface = getHttpInterface();
            CloseableHttpResponse response = httpInterface.execute(request)) {
            HttpClientTools.assertSuccessWithContent(response, "oauth2 token fetch");
            JsonObject parsed = JsonParser.object().from(response.getEntity().getContent());
            System.out.println(parsed);
            return !parsed.has("error") || parsed.isNull("error");
        } catch (IOException | JsonParserException e) {
            Main.error("Exception when validating Refresh Token: " + e.getMessage());
            return false;
        }
    }

    private void generateTokenManually() {
        JsonObject deviceCodeResponse = fetchDeviceCode();
        String verificationUrl = deviceCodeResponse.getString("verification_url");
        String userCode = deviceCodeResponse.getString("user_code");

        Main.log("Go to: " + verificationUrl);
        Main.log("Enter this code: " + userCode);

        String deviceCode = deviceCodeResponse.getString("device_code");
        pollForToken(deviceCode);
    }

    private void pollForToken(String deviceCode) {
        String requestJson = JsonWriter.string()
                .object()
                .value("client_id", CLIENT_ID)
                .value("client_secret", CLIENT_SECRET)
                .value("code", deviceCode)
                .value("grant_type", "http://oauth.net/grant_type/device/1.0")
                .end()
                .done();
        HttpPost request = new HttpPost("https://www.youtube.com/o/oauth2/token");
        StringEntity body = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
        request.setEntity(body);

        int maxAttempts = 12;
        long pollingInterval = 20 * 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try (HttpInterface httpInterface = getHttpInterface();
                CloseableHttpResponse response = httpInterface.execute(request)) {
                HttpClientTools.assertSuccessWithContent(response, "oauth2 token fetch");
                JsonObject parsed = JsonParser.object().from(response.getEntity().getContent());

                Main.debug("oauth2 token fetch response: " + JsonWriter.string(parsed));

                if (parsed.has("error") && !parsed.isNull("error")) {
                    String error = parsed.getString("error");

                    Main.error("Response error: " + error);
                    Thread.sleep(pollingInterval);
                } else if (parsed.has("access_token")){
                    setToken(parsed.getString("refresh_token"));
                    Main.log("Refresh Token set!");
                    return;
                }
            } catch (IOException | JsonParserException | InterruptedException e) {
                Main.error("Failed to fetch OAuth2 token response: " + e.getMessage());
            }
        }
    }
    private JsonObject fetchDeviceCode() {
        String requestJson = JsonWriter.string()
                .object()
                .value("client_id", CLIENT_ID)
                .value("scope", SCOPES)
                .value("device_id", UUID.randomUUID().toString().replace("-", ""))
                .value("device_model", "ytlr::")
                .end()
                .done();
        HttpPost request = new HttpPost("https://www.youtube.com/o/oauth2/device/code");
        StringEntity body = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
        request.setEntity(body);
        try (HttpInterface httpInterface = getHttpInterface();
             CloseableHttpResponse response = httpInterface.execute(request)) {
            HttpClientTools.assertSuccessWithContent(response, "device code fetch");
            return JsonParser.object().from(response.getEntity().getContent());
        } catch (IOException | JsonParserException e) {
            Main.error("Failed to parse device code response: " + e.getMessage());
            return null;
        }
    }
    private HttpInterface getHttpInterface() {
        HttpInterface httpInterface = httpInterfaceManager.getInterface();
        httpInterface.getContext().setAttribute(OAUTH_FETCH_CONTEXT_ATTRIBUTE, true);
        return httpInterface;
    }
}