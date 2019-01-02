package com.gruelbox.orko.job.script;

import static org.alfasoftware.morf.metadata.SchemaUtils.schema;

import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.TableContribution;

import com.gruelbox.orko.db.AbstractTableContributionTest;

public class TestScriptContribution extends AbstractTableContributionTest {

  @Override
  protected Schema initialSchema() {
    return schema();
  }

  @Override
  protected TableContribution tableContribution() {
    return new ScriptContribution();
  }
}
