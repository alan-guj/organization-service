package top.jyx365.organizationService;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import javax.naming.Name;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.ldap.support.LdapNameBuilder;

@JsonComponent
public class NameJsonConverter {
    public static class Serializer extends JsonSerializer<Name> {
        @Override
        public void serialize(Name name, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
            gen.writeString(name.toString());
        }
    }

    public static class Deserializer extends JsonDeserializer<Name> {
        @Override
        public Name deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            String strName = parser.getText();
            return LdapNameBuilder.newInstance(strName).build();
        }
    }
}
