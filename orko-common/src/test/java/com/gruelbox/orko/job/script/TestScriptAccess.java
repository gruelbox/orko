package com.gruelbox.orko.job.script;

import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.alfasoftware.morf.metadata.SchemaUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.google.inject.util.Providers;
import com.gruelbox.orko.db.DbTesting;

import io.dropwizard.testing.junit.DAOTestRule;

public class TestScriptAccess {

  @Rule
  public DAOTestRule database = DbTesting.rule()
    .addEntityClass(Script.class)
    .addEntityClass(ScriptParameter.class)
    .build();

  private ScriptAccess dao;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    dao = new ScriptAccess(Providers.of(database.getSessionFactory()));
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(new ScriptContribution().tables()));
  }

  @Test
  public void testEmpty() {
    database.inTransaction(() -> assertThat(dao.list(), Matchers.empty()));
  }

  @Test
  public void testSimpleCrud() {

    Script script1 = new Script();
    script1.setId("AAA");
    script1.setName("Foo");
    script1.setScript("10 PRINT \"Hello world\"\n20 GOTO 10");
    script1.setScriptHash("sdfsfsdf");
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

    database.inTransaction(() -> dao.insert(script1));
    database.inTransaction(() -> assertThat(dao.list(), Matchers.contains(script1)));

    Script script2 = new Script();
    script2.setId("BBB");
    script2.setName("Two");
    script2.setScript("Stuff");
    script2.setScriptHash("hashed");
    script2.setParameters(new ArrayList<>());
    database.inTransaction(() -> dao.insert(script2));

    script1.setName("Bar");
    script1.parameters().clear();
    database.inTransaction(() -> dao.update(script1));

    database.inTransaction(() -> assertThat(dao.list(), Matchers.containsInAnyOrder(script1, script2)));

    database.inTransaction(() -> dao.delete(script1.id()));
    database.inTransaction(() -> assertThat(dao.list(), Matchers.contains(script2)));

    database.inTransaction(() -> {
      List<ScriptParameter> orphanedParameters = database.getSessionFactory().getCurrentSession()
        .createQuery("from " + ScriptParameter.TABLE_NAME, ScriptParameter.class).list();
      assertThat(orphanedParameters, Matchers.empty());
    });
  }
}