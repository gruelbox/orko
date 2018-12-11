package com.gruelbox.orko.auth.ipwhitelisting;

import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import java.util.Collection;

import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.EntityContribution;

@Singleton
class IpWhitelistContribution implements TableContribution, EntityContribution {

  @Override
  public Collection<Table> tables() {
    return ImmutableList.of(
      table(IpWhitelist.TABLE_NAME)
        .columns(
          column(IpWhitelist.IP, DataType.STRING, 45).primaryKey(),
          column(IpWhitelist.EXPIRES, DataType.BIG_INTEGER)
        )
        .indexes(
          index(IpWhitelist.TABLE_NAME + "_1").columns("expires")
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }

  @Override
  public Iterable<Class<?>> getEntities() {
    return ImmutableList.of(IpWhitelist.class);
  }
}