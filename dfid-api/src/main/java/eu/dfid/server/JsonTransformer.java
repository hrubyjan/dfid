package eu.dfid.server;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import spark.ResponseTransformer;

/**
 * Simple transformer of all the objects into JSON.
 *
 * @author Kuba Krafka
 */
public final class JsonTransformer implements ResponseTransformer {

    @Override
    public String render(final Object data) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            mapper.setSerializationInclusion(Include.NON_NULL);
            mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
            return mapper.writeValueAsString(data);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize message", e);
        }
    }

}
