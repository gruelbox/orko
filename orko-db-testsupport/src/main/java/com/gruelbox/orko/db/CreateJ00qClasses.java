package com.gruelbox.orko.db;

import java.util.Collections;

import org.alfasoftware.morf.jdbc.ConnectionResourcesBean;
import org.alfasoftware.morf.jdbc.h2.H2;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.Deployment;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;

public class CreateJ00qClasses {

  @Inject private Schema schema;

  public static void main(String[] args) throws Exception {
    Guice.createInjector(
      new Module() {
        @Override
        public void configure(Binder binder) {
          binder.bind(DbConfiguration.class).toInstance(new DbConfiguration());
        }
      },
      new DbModule()
    ).getInstance(CreateJ00qClasses.class).run();
  }

  private void run() throws Exception {
    ConnectionResourcesBean conn = new ConnectionResourcesBean();
    conn.setDatabaseType(H2.IDENTIFIER);
    conn.setDatabaseName("schemagen");
    Deployment.deploySchema(schema, Collections.emptySet(), conn);
    generate();
  }

  private void generate() throws Exception {
//    Configuration configuration = new Configuration();
//    configuration.setJdbc(new Jdbc());
//    configuration.getJdbc().setDriver("org.h2.Driver");
//    configuration.getJdbc().setUrl("jdbc:h2:mem:schemagen");
//    configuration.setGenerator(new Generator());
//    configuration.getGenerator().setName("org.jooq.codegen.JavaGenerator");
//    configuration.getGenerator().setDatabase(new Database());
//    configuration.getGenerator().getDatabase().setName("org.jooq.meta.h2.H2Database");
//    configuration.getGenerator().getDatabase().setIncludes(".*");
//    configuration.getGenerator().getDatabase().setExcludes("");
//    configuration.getGenerator().getDatabase().setInputSchema("PUBLIC");
//    configuration.getGenerator().setTarget(new Target());
//    configuration.getGenerator().getTarget().setPackageName("com.gruelbox.orko.db.generated");
//    configuration.getGenerator().getTarget().setDirectory("./src/main/java");
//    GenerationTool.generate(configuration);
  }
}