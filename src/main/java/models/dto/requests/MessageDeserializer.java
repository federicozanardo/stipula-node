package models.dto.requests;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;

public class MessageDeserializer implements JsonDeserializer<Message> {
    private final HashMap<String, Class<? extends Message>> dataTypeRegistry;

    public MessageDeserializer() {
        this.dataTypeRegistry = new HashMap<>();
    }

    public void registerDataType(String jsonElementName, Class<? extends Message> javaType) {
        dataTypeRegistry.put(jsonElementName, javaType);
    }

    @Override
    public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String value = jsonObject.get("className").getAsString();

        for (HashMap.Entry<String, Class<? extends Message>> entry : dataTypeRegistry.entrySet()) {
            if (value.equals(entry.getKey())) {
                Class<? extends Message> dataType = dataTypeRegistry.get(value);
                return context.deserialize(jsonObject, dataType);
            }
        }

        // TODO: Set up the error properly
        throw new RuntimeException("Oops");
    }
}
