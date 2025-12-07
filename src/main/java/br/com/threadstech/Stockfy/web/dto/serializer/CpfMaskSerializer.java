package br.com.threadstech.stockfy.web.dto.serializer;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class CpfMaskSerializer extends StdSerializer<String> {

  protected CpfMaskSerializer() {
    super(String.class);
  }

  @Override
  public void serialize(String value, JsonGenerator gen, SerializationContext provider) {
    if (value != null) {
      gen.writeString(value.replaceAll("(\\d{3})\\.\\d{3}\\.\\d{3}-(\\d{2})", "***.$1.$2-**"));
    } else {
      gen.writeNull();
    }
  }
}
