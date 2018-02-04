package com.grahamcrockford.oco.db;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer<T>  {

  private final ObjectMapper objectMapper;
  private final Type type;

  public JsonSerializer(ObjectMapper objectMapper, Type type) {
    this.objectMapper = objectMapper;
    this.type = type;
  }

  public String serialize(T value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public T deserialize(String data) {
    try {
      return objectMapper.readValue(data, new TypeReference<T>() {
        @Override
        public Type getType() {
          return type;
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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