package models.dto.requests;

import com.google.gson.*;
import exceptions.models.dto.requests.contract.function.UnsupportedTypeException;
import models.contract.PayToContract;
import models.dto.requests.contract.FunctionArgument;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
        ArrayList<FunctionArgument> args = new ArrayList<>();

        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray arguments = jsonObject.get("arguments").getAsJsonArray();

        for (int i = 0; i < arguments.size(); i++) {
            JsonObject argument = arguments.get(i).getAsJsonObject().get("argument").getAsJsonObject();
            String argumentType = argument.get("first").getAsString();
            String variableName = argument.get("second").getAsString();

            if (argumentType.equals("asset")) {
                JsonObject val = argument.get("third").getAsJsonObject();
                String propertyId = val.get("propertyId").getAsString();
                String address = val.get("address").getAsString();
                String unlockScript = val.get("unlockScript").getAsString();

                try {
                    args.add(new FunctionArgument(argumentType, variableName, new PayToContract(propertyId, address, unlockScript)));
                } catch (UnsupportedTypeException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String val = argument.get("third").getAsString();
                try {
                    args.add(new FunctionArgument(argumentType, variableName, val));
                } catch (UnsupportedTypeException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        String value = jsonObject.get("type").getAsString();

        for (HashMap.Entry<String, Class<? extends Message>> entry : dataTypeRegistry.entrySet()) {
            if (value.equals(entry.getKey())) {
                Class<? extends Message> dataType = dataTypeRegistry.get(value);
                Message message = context.deserialize(jsonObject, dataType);

                if (message instanceof FunctionCall) {
                    FunctionCall functionCall = (FunctionCall) message;
                    return new FunctionCall(functionCall.getContractInstanceId(), functionCall.getFunctionName(), args);
                } else {
                    AgreementCall agreementCall = (AgreementCall) message;
                    return new AgreementCall(agreementCall.getContractId(), args, agreementCall.getParties());
                }
            }
        }

        // TODO: Set up the error properly
        throw new RuntimeException("Oops");
    }
}
