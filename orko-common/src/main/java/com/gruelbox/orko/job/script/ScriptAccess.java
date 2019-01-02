package com.gruelbox.orko.job.script;

/*-
 * ===============================================================================L
 * Orko Common
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import static com.gruelbox.orko.job.script.Script.TABLE_NAME;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.inject.Provider;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.Hasher;

import jersey.repackaged.com.google.common.collect.Maps;

class ScriptAccess {

  static final String UNSIGNED = "UNSIGNED";

  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptAccess.class);

  private final Provider<SessionFactory> sessionFactory;
  private final Hasher hasher;
  private final OrkoConfiguration config;

  @Inject
  ScriptAccess(Provider<SessionFactory> sf, Hasher hasher, OrkoConfiguration orkoConfiguration) {
    this.sessionFactory = sf;
    this.hasher = hasher;
    this.config = orkoConfiguration;
  }

  void saveOrUpdate(Script script) {
    if (StringUtils.isNotEmpty(config.getScriptSigningKey())) {
      script.setScriptHash(hasher.hashWithString(script.script(), config.getScriptSigningKey()));
    } else {
      script.setScriptHash(UNSIGNED);
    }
    script.parameters().forEach(p -> p.setParent(script));
    LOGGER.debug("Saving script: " + script);

    List<String> parameterNames = script.parameters().stream().map(ScriptParameter::name).collect(toList());
    if (parameterNames.isEmpty()) {
      session().createQuery("delete from " + ScriptParameter.TABLE_NAME + " where id.scriptId = :scriptId")
        .setParameter("scriptId", script.id())
        .executeUpdate();
    } else {
      session().createQuery("delete from " + ScriptParameter.TABLE_NAME + " where id.scriptId = :scriptId and id.name not in :names")
        .setParameter("scriptId", script.id())
        .setParameterList("names", parameterNames)
        .executeUpdate();
    }

    script.parameters().forEach(p -> session().saveOrUpdate(p));
    session().saveOrUpdate(script);
  }

  Iterable<Script> list() {
    Map<String, Script> scripts = Maps.uniqueIndex(
      FluentIterable.from(session().createQuery("from " + TABLE_NAME, Script.class).list())
        .filter(this::scriptValid),
      Script::id
    );
    session().createQuery("from " + ScriptParameter.TABLE_NAME, ScriptParameter.class).list().forEach(p -> {
      Script script = scripts.get(p.scriptId());
      if (script == null) {
        LOGGER.warn("Ophaned parameter: {}", p);
      } else {
        script.parameters().add(p);
      }
    });
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Loaded scripts: " + scripts.values());
    return scripts.values();
  }

  private boolean scriptValid(@Nullable Script s) {
    if (StringUtils.isEmpty(config.getScriptSigningKey()))
      return true;
    boolean valid = hasher.hashWithString(s.script(), config.getScriptSigningKey()).equals(s.scriptHash());
    if (!valid)
      LOGGER.warn("Ignoring script [{}] since script hash mismatches. Possible DB intrusion?");
    return valid;
  }

  void delete(String id) {
    session()
      .createQuery("delete from " + TABLE_NAME + " where id = :id")
      .setParameter("id", id)
      .executeUpdate();
    session()
      .createQuery("delete from " + ScriptParameter.TABLE_NAME + " where id.scriptId = :id")
      .setParameter("id", id)
      .executeUpdate();
  }

  private Session session() {
    return sessionFactory.get().getCurrentSession();
  }
}
