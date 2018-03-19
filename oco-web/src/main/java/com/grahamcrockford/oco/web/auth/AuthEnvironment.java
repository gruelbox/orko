package com.grahamcrockford.oco.web.auth;

import java.util.EnumSet;
import java.util.Objects;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.auth.AuthConfiguration;
import com.grahamcrockford.oco.api.util.EnvironmentInitialiser;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final SimpleAuthenticator authenticator;
  private final AuthConfiguration configuration;
  private final AdminConstraintSecurityHandler securityHandler;

  @Inject
  AuthEnvironment(SimpleAuthenticator authenticator, AuthConfiguration configuration, AdminConstraintSecurityHandler securityHandler) {
    this.authenticator = authenticator;
    this.configuration = configuration;
    this.securityHandler = securityHandler;
  }

  @Override
  public void init(Environment environment) {

    // Allow CORS
    // TODO needs to be more secure.
    if (configuration.isCors()) {
      final FilterRegistration.Dynamic cors =
          environment.servlets().addFilter("CORS", CrossOriginFilter.class);
      cors.setInitParameter("allowedOrigins", "*");
      cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin,Authorization");
      cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");
      cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    // Auth
    environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
      .setAuthenticator(authenticator)
      .setAuthorizer(authenticator)
      .setRealm("SUPER SECRET STUFF")
      .buildAuthFilter()
    ));
    environment.jersey().register(RolesAllowedDynamicFeature.class);

    // Restrict admin access too
    environment.admin().setSecurityHandler(securityHandler);

  }


  /**
   * Hacky but gets authentication working on the admin pages
   *
   * @author grahamc (Graham Crockford)
   */
  @Singleton
  private static final class AdminConstraintSecurityHandler extends ConstraintSecurityHandler {

    private static final String ADMIN_ROLE = "admin";

    @Inject
    AdminConstraintSecurityHandler(AuthConfiguration authConfiguration) {
      final Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, ADMIN_ROLE);
      constraint.setAuthenticate(true);
      constraint.setRoles(new String[] { ADMIN_ROLE });
      final ConstraintMapping cm = new ConstraintMapping();
      cm.setConstraint(constraint);
      cm.setPathSpec("/*");
      setAuthenticator(new BasicAuthenticator());
      addConstraintMapping(cm);
      setLoginService(new AdminLoginService(authConfiguration.getUserName(), authConfiguration.getPassword()));
    }

    public class AdminLoginService extends AbstractLoginService {

      private final UserPrincipal adminPrincipal;
      private final String adminUserName;

      public AdminLoginService(final String userName, final String password) {
        this.adminUserName = Objects.requireNonNull(userName);
        this.adminPrincipal = new UserPrincipal(userName, new Password(Objects.requireNonNull(password)));
      }

      @Override
      protected String[] loadRoleInfo(final UserPrincipal principal) {
        if (adminUserName.equals(principal.getName())) {
          return new String[] { ADMIN_ROLE };
        }
        return new String[0];
      }

      @Override
      protected UserPrincipal loadUserInfo(final String userName) {
        return adminUserName.equals(userName) ? adminPrincipal : null;
      }
    }
  }
}