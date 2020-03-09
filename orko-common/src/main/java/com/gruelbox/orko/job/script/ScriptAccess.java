/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.job.script;

import static com.gruelbox.orko.job.script.Script.TABLE_NAME;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.gruelbox.orko.auth.Hasher;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ScriptAccess {

  static final String UNSIGNED = "UNSIGNED";

  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptAccess.class);

  private final Provider<SessionFactory> sessionFactory;
  private final Hasher hasher;
  private final ScriptConfiguration config;

  @Inject
  ScriptAccess(Provider<SessionFactory> sf, Hasher hasher, ScriptConfiguration config) {
    this.sessionFactory = sf;
    this.hasher = hasher;
    this.config = config;
  }

  void saveOrUpdate(Script script) {
    if (StringUtils.isNotEmpty(config.getScriptSigningKey())) {
      script.setScriptHash(hasher.hashWithString(script.script(), config.getScriptSigningKey()));
    } else {
      script.setScriptHash(UNSIGNED);
    }
    script.parameters().forEach(p -> p.setParent(script));
    LOGGER.debug("Saving script: {}", script);

    List<String> parameterNames =
        script.parameters().stream().map(ScriptParameter::name).collect(toList());
    if (parameterNames.isEmpty()) {
      session()
          .createQuery(
              "delete from " + ScriptParameter.TABLE_NAME + " where id.scriptId = :scriptId")
          .setParameter("scriptId", script.id())
          .executeUpdate();
    } else {
      session()
          .createQuery(
              "delete from "
                  + ScriptParameter.TABLE_NAME
                  + " where id.scriptId = :scriptId and id.name not in :names")
          .setParameter("scriptId", script.id())
          .setParameterList("names", parameterNames)
          .executeUpdate();
    }

    script.parameters().forEach(p -> session().saveOrUpdate(p));
    session().saveOrUpdate(script);
  }

  Iterable<Script> list() {
    Map<String, Script> scripts =
        Maps.uniqueIndex(
            session().createQuery("from " + TABLE_NAME, Script.class).stream()
                .filter(this::scriptValid)
                .collect(toList()),
            Script::id);
    session()
        .createQuery("from " + ScriptParameter.TABLE_NAME, ScriptParameter.class)
        .list()
        .forEach(
            p -> {
              Script script = scripts.get(p.scriptId());
              if (script == null) {
                LOGGER.warn("Ophaned parameter: {}", p);
              } else {
                script.parameters().add(p);
              }
            });
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Loaded scripts: {}", scripts.values());
    return scripts.values();
  }

  private boolean scriptValid(Script s) {
    if (StringUtils.isEmpty(config.getScriptSigningKey())) return true;
    boolean valid =
        hasher.hashWithString(s.script(), config.getScriptSigningKey()).equals(s.scriptHash());
    if (!valid)
      LOGGER.warn(
          "Ignoring script [{}] since script hash mismatches. Possible DB intrusion?", s.id());
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
