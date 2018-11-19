package com.grahamcrockford.orko.auth.ipwhitelisting;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.db.DbTesting;;

public class TestIpWhitelistAccess {
  
  private IpWhitelistAccessImpl dao;

  @Before
  public void setup() throws Exception {
    AuthConfiguration authConfiguration = new AuthConfiguration();
    authConfiguration.setIpWhitelisting(new IpWhitelistingConfiguration());
    authConfiguration.getIpWhitelisting().setWhitelistExpirySeconds(3);
    dao = new IpWhitelistAccessImpl(DbTesting.connectionSource(), authConfiguration);
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(dao.tables()));
    dao.start();
  }
  
  @After
  public void tearDown() throws Exception {
    dao.stop();
  }
  
  @Test
  public void testAll() throws InterruptedException {
    dao.add("1");
    dao.add("2");
    dao.add("3");
    dao.delete("2");
    assertTrue(dao.exists("1"));
    assertFalse(dao.exists("2"));
    Thread.sleep(2000);
    dao.cleanup();
    dao.add("4");
    Thread.sleep(1100);
    dao.cleanup();
    assertFalse(dao.exists("1"));
    assertFalse(dao.exists("3"));
    assertTrue(dao.exists("4"));
  }
}