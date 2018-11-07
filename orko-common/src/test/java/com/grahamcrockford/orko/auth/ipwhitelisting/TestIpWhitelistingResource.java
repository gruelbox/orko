package com.grahamcrockford.orko.auth.ipwhitelisting;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import io.dropwizard.testing.junit.ResourceTestRule;

public class TestIpWhitelistingResource {

  private static final IpWhitelisting ipWhitelisting = mock(IpWhitelisting.class);

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
          .addResource(new IpWhitelistingResource(ipWhitelisting))
          .build();

  @After
  public void tearDown(){
      reset(ipWhitelisting);
  }

  @Test
  public void testGetFail() {
    when(ipWhitelisting.authoriseIp()).thenReturn(false);
    Boolean result = resources.target("/auth").request().get(Boolean.class);
    assertFalse(result);
  }

  @Test
  public void testGetSuccess() {
    when(ipWhitelisting.authoriseIp()).thenReturn(true);
    Boolean result = resources.target("/auth").request().get(Boolean.class);
    assertTrue(result);
  }

  @Test
  public void testDeleteFail() {
    when(ipWhitelisting.deWhitelistIp()).thenReturn(false);
    Response result = resources.target("/auth").request().delete();
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), result.getStatus());
  }

  @Test
  public void testDeleteSuccess() {
    when(ipWhitelisting.deWhitelistIp()).thenReturn(true);
    Response result = resources.target("/auth").request().delete();
    assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
  }

  @Test
  public void testPutFail() {
    when(ipWhitelisting.whiteListRequestIp(1234)).thenReturn(false);
    Response result = resources.target("/auth").queryParam("token", 1234).request().put(Entity.entity("", MediaType.TEXT_PLAIN));
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), result.getStatus());
  }

  @Test
  public void testPutSuccess() {
    when(ipWhitelisting.whiteListRequestIp(1234)).thenReturn(true);
    Response result = resources.target("/auth").queryParam("token", 1234).request().put(Entity.entity("", MediaType.TEXT_PLAIN));
    assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
  }
}
