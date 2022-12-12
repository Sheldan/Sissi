package dev.sheldan.sissi.module.debra.service;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.math.BigDecimal;

public class BigDecimalGsonAdapter implements JsonDeserializer<BigDecimal> {
    @Override
    public BigDecimal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return new BigDecimal(json.getAsString()
                    .replace(".", "")
                    .replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new JsonParseException(e);
        }
    }
}
