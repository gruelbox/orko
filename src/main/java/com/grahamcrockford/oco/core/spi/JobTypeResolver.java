package com.grahamcrockford.oco.core.spi;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.grahamcrockford.oco.core.jobs.LimitOrderJob;

final class JobTypeResolver implements TypeIdResolver {

  private static final String COMMAND_PACKAGE = LimitOrderJob.class.getPackage().getName();
  private JavaType baseType;

  @Override
  public void init(JavaType baseType) {
    this.baseType = baseType;
  }

  @Override
  public String idFromValue(Object obj) {
    return idFromValueAndType(obj, obj.getClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> clazz) {
    if (clazz.getPackage().getName().equals(COMMAND_PACKAGE)) {
      return clazz.getSimpleName().replaceAll("AutoValue_", "");
    }
    throw new IllegalStateException("class " + clazz + " is not in the package " + COMMAND_PACKAGE);
  }

  @Override
  public String idFromBaseType() {
    return idFromValueAndType(null, baseType.getRawClass());
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws IOException {
    Class<?> clazz;
    String clazzName = COMMAND_PACKAGE + ".AutoValue_" + id;
    try {
      clazz = Class.forName(clazzName);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Job type '" + clazzName + "' not found");
    }
    return TypeFactory.defaultInstance().constructSpecializedType(baseType, clazz);
  }

  @Override
  public String getDescForKnownTypeIds() {
    return "";
  }

  @Override
  public Id getMechanism() {
    return Id.CUSTOM;
  }
}