package com.gruelbox.orko.jobrun.spi;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

/**
 * A Jackson {@link TypeIdResolver} for {@link Job} instances which uses
 * {@link ServiceLoader} to find suitable concrete classes on the classpath.
 *
 * @author Graham Crockford
 */
final class JobTypeResolver extends TypeIdResolverBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobTypeResolver.class);

  private static final Map<String, Class<? extends Job>> registered = loadJobClasses();

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
    return clazz.getSimpleName().replaceAll("AutoValue_", "");
  }

  @Override
  public String idFromBaseType() {
    return idFromValueAndType(null, baseType.getRawClass());
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws IOException {
    Class<? extends Job> clazz = registered.get(id);
    if (clazz == null) {
      throw new IllegalArgumentException("Job type '" + id + "' not found");
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

  private static Map<String, Class<? extends Job>> loadJobClasses() {
    ServiceLoader<JobTypeContribution> serviceLoader = ServiceLoader.load(JobTypeContribution.class);
    FluentIterable<Class<? extends Job>> jobs = FluentIterable.from(serviceLoader).transformAndConcat(c -> c.jobTypes());
    try {
      ImmutableMap<String, Class<? extends Job>> result = jobs.uniqueIndex(c -> c.getSimpleName().replaceAll("AutoValue_", ""));
      LOGGER.info("Job types registered: {}", result);
      return result;
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Duplication job names registered", e);
    }
  }
}