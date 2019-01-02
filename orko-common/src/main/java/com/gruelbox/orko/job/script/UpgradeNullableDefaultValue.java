package com.gruelbox.orko.job.script;

import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;

import org.alfasoftware.morf.upgrade.DataEditor;
import org.alfasoftware.morf.upgrade.SchemaEditor;
import org.alfasoftware.morf.upgrade.Sequence;
import org.alfasoftware.morf.upgrade.UUID;
import org.alfasoftware.morf.upgrade.UpgradeStep;
import org.alfasoftware.morf.upgrade.Version;

@UUID("e47d27b1-38bb-4921-8c24-486c9432f365")
@Version("0.7.0")
@Sequence(1545935699 )
class UpgradeNullableDefaultValue implements UpgradeStep {

  @Override
  public String getJiraId() {
    return "#99";
  }

  @Override
  public String getDescription() {
    return "Make the default value nullable";
  }

  @Override
  public void execute(SchemaEditor schema, DataEditor data) {
    schema.changeColumn("ScriptParameter",
        column("defaultValue", STRING, 255),
        column("defaultValue", STRING, 255).nullable());
  }
}