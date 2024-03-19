package it.paolinucs.ramenpersist.service;

import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class JsonService {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private EncryptionService encryptionService;

    private JsonObject parseFromString(String jsonString) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
        return jsonObject;
    }

    public void save(String jsonString, String masterPassword) {
        JsonObject parsedJson = parseFromString(jsonString);

        if (!checkJsonIntegrity(parsedJson))
            return;

        JsonObject encrypted = new JsonObject();

        Set<Map.Entry<String, JsonElement>> entrySet = parsedJson.entrySet();
        LOG.info("Saving new Json Data");

        for (Map.Entry<String, JsonElement> entry : entrySet) {
            try {
                JsonElement value = entry.getValue();
                if (value.isJsonArray()) {
                    JsonArray jsonArray = value.getAsJsonArray();
                    JsonArray encryptedArray = new JsonArray();
                    for (JsonElement element : jsonArray) {
                        encryptedArray.add(encryptionService.encrypt(element.getAsString(), masterPassword));
                    }
                    encrypted.add(entry.getKey(), encryptedArray);
                } else {
                    encrypted.addProperty(entry.getKey(),
                            encryptionService.encrypt(value.getAsString(), masterPassword));
                }
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                    | BadPaddingException exc) {
                LOG.error("Error occurred while encrypting JSON data", exc);
                break;
            }
        }

        try {
            writeJson(encrypted);
            LOG.info("Data saved succesfully!");
            LOG.info("Encrypted Data: " + encrypted);
        } catch (IOException exc) {
            LOG.error("Cannot save Json Data!", exc);
        }
    }

    private void writeJson(JsonObject el) throws IOException {
        UUID rand = UUID.randomUUID();
        FileWriter file = new FileWriter("data/" + rand + ".json");
        LOG.info("Writing JSON [{}]", rand);
        file.write(el.toString());
        file.close();
    }

    private boolean checkJsonIntegrity(JsonObject jsonData) {

        LOG.info("Checking JSON integrity");

        try {
            String url = jsonData.get("url").getAsString();
            LOG.info("URL OK [{}]", url);
        } catch (Exception exc) {
            LOG.error("JSON check integrity failed");
            return false;
        }

        try {
            String requestMethod = jsonData.get("method").getAsString();
            LOG.info("REQUEST METHOD OK [{}]", requestMethod);
        } catch (Exception exc) {
            LOG.error("JSON check integrity failed");
            return false;
        }

        try {
            int responseCode = jsonData.get("response_code").getAsInt();
            LOG.info("RESPONSE CODE OK [{}]", responseCode);
        } catch (Exception exc) {
            LOG.error("JSON check integrity failed");
            return false;
        }

        try {
            String requestBody = jsonData.get("request_body").getAsString();
            LOG.info("REQUEST BODY OK [{}]", requestBody);
        } catch (Exception exc) {
            LOG.error("JSON check integrity failed", exc);
            return false;
        }

        try {
            String responseBody = jsonData.get("response_body").getAsString();
            LOG.info("RESPONSE BODY OK [{}]", responseBody);
        } catch (Exception exc) {
            LOG.error("JSON check integrity failed", exc);
            return false;
        }

        try {
            String comment = jsonData.get("comment").getAsString();
            LOG.info("COMMENT OK [{}]", comment);
        } catch (Exception exc) {
            LOG.error("JSON check integrity failed", exc);
            return false;
        }

        try {
            JsonArray headers = jsonData.get("headers").getAsJsonArray();
            LOG.info("HEADERS OK [{}]", headers);
        } catch (Exception exc) {
            LOG.error("JSON check integrity failed", exc);
            return false;
        }

        LOG.info("JSON integrity check completed succesfully.");
        return true;
    }

}
