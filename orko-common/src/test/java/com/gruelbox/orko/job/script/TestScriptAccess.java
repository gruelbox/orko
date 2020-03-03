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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.inject.util.Providers;
import com.gruelbox.orko.auth.Hasher;
import com.gruelbox.orko.db.DbTesting;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.util.ArrayList;
import java.util.List;
import org.alfasoftware.morf.metadata.SchemaUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;

@Tag("database")
@ExtendWith(DropwizardExtensionsSupport.class)
public class TestScriptAccess {

  private final Hasher hasher = new Hasher();
  private final ScriptConfiguration config =
      new ScriptConfiguration() {
        @Override
        public String getScriptSigningKey() {
          return "UGI&T&IUGousy9d7y2he3o8dyq9182y018yfouqhwdwe2";
        }
      };

  public DAOTestExtension database =
      DbTesting.extension()
          .addEntityClass(Script.class)
          .addEntityClass(ScriptParameter.class)
          .build();

  private ScriptAccess dao;

  @BeforeEach
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    dao = new ScriptAccess(Providers.of(database.getSessionFactory()), hasher, config);
    DbTesting.clearDatabase();
    DbTesting.invalidateSchemaCache();
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(new ScriptContribution().tables()));
  }

  @Test
  public void testEmpty() {
    database.inTransaction(() -> assertTrue(Iterables.isEmpty(dao.list())));
  }

  @Test
  public void testSimpleCrud() {

    Script script1 = new Script();
    script1.setId("AAA");
    script1.setName("Foo");
    script1.setScript("10 PRINT \"Hello world\"\n20 GOTO 10");
    script1.setParameters(new ArrayList<>());
    script1.parameters().add(new ScriptParameter());
    script1.parameters().add(new ScriptParameter());
    script1.parameters().get(0).setName("Param1");
    script1.parameters().get(0).setDescription("Does things");
    script1.parameters().get(0).setDefaultValue("Apple");
    script1.parameters().get(0).setMandatory(true);
    script1.parameters().get(1).setName("Param2");
    script1.parameters().get(1).setDescription("Does other things");
    script1.parameters().get(1).setDefaultValue("Banana");
    script1.parameters().get(1).setMandatory(false);

    database.inTransaction(() -> dao.saveOrUpdate(script1));

    // Force an evict so we can make sure we're picking up the data from the database next
    database.getSessionFactory().getCurrentSession().evict(script1);

    database.inTransaction(
        () -> {
          List<Script> list = FluentIterable.from(dao.list()).toList();
          assertThat(list, contains(script1));
          assertThat(
              list.get(0).parameters(),
              containsInAnyOrder(script1.parameters().get(0), script1.parameters().get(1)));
          list.forEach(o -> database.getSessionFactory().getCurrentSession().evict(o));
        });

    Script script2 = new Script();
    script2.setId("BBB");
    script2.setName("Two");
    script2.setScript("Stuff");
    script2.setParameters(new ArrayList<>());
    database.inTransaction(() -> dao.saveOrUpdate(script2));

    // Force an evict so we can make sure we're picking up the data from the database next
    database.getSessionFactory().getCurrentSession().evict(script2);

    script1.setName("Bar");
    script1.parameters().clear();
    database.inTransaction(() -> dao.saveOrUpdate(script1));

    // Force an evict so we can make sure we're picking up the data from the database next
    database.getSessionFactory().getCurrentSession().evict(script1);

    database.inTransaction(
        () -> {
          List<ScriptParameter> orphanedParameters =
              database
                  .getSessionFactory()
                  .getCurrentSession()
                  .createQuery("from " + ScriptParameter.TABLE_NAME, ScriptParameter.class)
                  .list();
          assertThat(orphanedParameters, Matchers.empty());
        });

    database.inTransaction(
        () -> {
          Iterable<Script> list = dao.list();
          assertThat(list, containsInAnyOrder(script1, script2));
          list.forEach(o -> database.getSessionFactory().getCurrentSession().evict(o));
        });

    database.inTransaction(() -> dao.delete(script1.id()));

    database.inTransaction(
        () -> {
          Iterable<Script> list = dao.list();
          assertThat(list, containsInAnyOrder(script2));
          list.forEach(o -> database.getSessionFactory().getCurrentSession().evict(o));
        });

    script2.parameters().add(new ScriptParameter());
    script2.parameters().get(0).setName("Param3");
    script2.parameters().get(0).setDescription("Change everything");
    script2.parameters().get(0).setDefaultValue("Orange");
    script2.parameters().get(0).setMandatory(true);
    database.inTransaction(() -> dao.saveOrUpdate(script2));

    // Force an evict so we can make sure we're picking up the data from the database next
    database.getSessionFactory().getCurrentSession().evict(script2);

    database.inTransaction(
        () -> {
          List<Script> list = FluentIterable.from(dao.list()).toList();
          assertThat(list, containsInAnyOrder(script2));
          assertThat(list.get(0).parameters(), containsInAnyOrder(script2.parameters().get(0)));
          list.forEach(o -> database.getSessionFactory().getCurrentSession().evict(o));
        });

    database.inTransaction(() -> dao.delete(script2.id()));

    database.inTransaction(
        () -> {
          List<ScriptParameter> orphanedParameters =
              database
                  .getSessionFactory()
                  .getCurrentSession()
                  .createQuery("from " + ScriptParameter.TABLE_NAME, ScriptParameter.class)
                  .list();
          assertThat(orphanedParameters, Matchers.empty());
        });
  }

  @Test
  public void testBadHash() {
    Script script1 = new Script();
    script1.setId("AAA");
    script1.setName("One");
    script1.setScript("Stuff 1");
    script1.setParameters(new ArrayList<>());
    Script script2 = new Script();
    script2.setId("BBB");
    script2.setName("Two");
    script2.setScript("Stuff 2");
    script2.setParameters(new ArrayList<>());
    database.inTransaction(
        () -> {
          dao.saveOrUpdate(script1);
          dao.saveOrUpdate(script2);
        });
    database.inTransaction(() -> assertThat(dao.list(), containsInAnyOrder(script1, script2)));
    script2.setScriptHash("WRONGWRONGWRONG");
    database.inTransaction(() -> database.getSessionFactory().getCurrentSession().save(script2));
    database.inTransaction(() -> assertThat(dao.list(), contains(script1)));
  }
}
