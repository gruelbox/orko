package com.grahamcrockford.oco.db;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Inject;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonSerializer<T> implements Serializer<T> {

  private final ObjectMapper objectMapper;
  private final Type type;

  JsonSerializer(ObjectMapper objectMapper, Type type) {
    this.objectMapper = objectMapper;
    this.type = type;
  }

  @Override
  public void serialize(DataOutput2 out, T value) throws IOException {
    out.writeUTF(objectMapper.writeValueAsString(value));
  }

  @Override
  public T deserialize(DataInput2 input, int available) throws IOException {
    return objectMapper.readValue(input.readUTF(), new TypeReference<T>() {
      @Override
      public Type getType() {
        return type;
      }
    });
  }


  public static class Factory {

    private final ObjectMapper objectMapper;

    @Inject
    Factory(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    public <T> JsonSerializer<T> create(Class<T> clazz) {
      return new JsonSerializer<>(objectMapper, clazz);
    }

    public <T> JsonSerializer<T> createGeneric(Class<?> parameterized, Class<?>... parameters) {
      return new JsonSerializer<>(objectMapper, new ParameterizedType() {

        @Override
        public Type getRawType() {
          return parameterized;
        }

        @Override
        public Type getOwnerType() {
          return null;
        }

        @Override
        public Type[] getActualTypeArguments() {
          return parameters;
        }
      });
    }

    public <T> JsonSerializer<T> create(Type type) {
      return new JsonSerializer<>(objectMapper, type);
    }
  }
}