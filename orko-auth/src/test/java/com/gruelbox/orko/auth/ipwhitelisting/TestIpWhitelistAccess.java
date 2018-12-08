package com.gruelbox.orko.auth.ipwhitelisting;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.util.Providers;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.db.Transactionally;

import io.dropwizard.testing.junit.DAOTestRule;;

public class TestIpWhitelistAccess {

  @Rule
  public DAOTestRule database = DbTesting.rule()
      .addEntityClass(IpWhitelist.class)
      .build();

  private IpWhitelistAccessImpl dao;
  private Transactionally transactionally;

  @Before
  public void setup() throws Exception {
    AuthConfiguration authConfiguration = new AuthConfiguration();
    authConfiguration.setIpWhitelisting(new IpWhitelistingConfiguration());
    authConfiguration.getIpWhitelisting().setWhitelistExpirySeconds(3);
    transactionally = new Transactionally(database.getSessionFactory());
    dao = new IpWhitelistAccessImpl(Providers.of(database.getSessionFactory()), transactionally, authConfiguration);
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(dao.tables()));

    dao.start();
  }

  @After
  public void tearDown() throws Exception {
    dao.stop();
  }

  @Test
  public void testAll() throws InterruptedException {
    transactionally.run(() -> {
      dao.add("1");
      dao.add("2");
      dao.add("3");
      dao.delete("2");
    });

    transactionally.run(() -> {
      assertTrue(dao.exists("1"));
      assertFalse(dao.exists("2"));
    });

    Thread.sleep(2000);
    dao.cleanup();

    transactionally.run(() -> {
      dao.add("4");
    });

    Thread.sleep(1100);
    dao.cleanup();

    transactionally.run(() -> {
      assertFalse(dao.exists("1"));
      assertFalse(dao.exists("3"));
      assertTrue(dao.exists("4"));
    });
  }

  @Test
  public void testMerge() throws InterruptedException {
    transactionally.run(() -> {
      dao.add("1");
    });

    transactionally.run(() -> {
      dao.add("1");
    });

    transactionally.run(() -> {
      assertTrue(dao.exists("1"));
      assertFalse(dao.exists("2"));
    });
  }
}