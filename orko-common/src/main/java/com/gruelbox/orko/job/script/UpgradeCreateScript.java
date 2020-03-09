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

import static org.alfasoftware.morf.metadata.DataType.BOOLEAN;
import static org.alfasoftware.morf.metadata.DataType.CLOB;
import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import org.alfasoftware.morf.upgrade.DataEditor;
import org.alfasoftware.morf.upgrade.SchemaEditor;
import org.alfasoftware.morf.upgrade.Sequence;
import org.alfasoftware.morf.upgrade.UUID;
import org.alfasoftware.morf.upgrade.UpgradeStep;
import org.alfasoftware.morf.upgrade.Version;

@UUID("2a181650-04c7-4473-9455-8ab051335a96")
@Version("0.7.0")
@Sequence(1545850327)
class UpgradeCreateScript implements UpgradeStep {

  @Override
  public String getJiraId() {
    return "#99";
  }

  @Override
  public String getDescription() {
    return "Add the tables supporting scripted job parameters";
  }

  @Override
  public void execute(SchemaEditor schema, DataEditor data) {
    schema.addTable(
        table("Script")
            .columns(
                column("id", STRING, 45).primaryKey(),
                column("name", STRING, 255),
                column("script", CLOB),
                column("scriptHash", STRING, 255)));
    schema.addTable(
        table("ScriptParameter")
            .columns(
                column("scriptId", STRING, 45).primaryKey(),
                column("name", STRING, 255).primaryKey(),
                column("description", STRING, 255),
                column("defaultValue", STRING, 255),
                column("mandatory", BOOLEAN)));
  }
}
