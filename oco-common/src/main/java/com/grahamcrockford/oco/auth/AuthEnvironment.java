package com.grahamcrockford.oco.auth;

import java.util.EnumSet;
import java.util.Objects;

import javax.inject.Inject;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;

import com.google.common.cache.CacheBuilderSpec;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final AdminConstraintSecurityHandler securityHandler;
  private final IpWhitelistServletFilter ipWhitelistServletFilter;
  private final AuthConfiguration configuration;
  private final AuthenticatorAuthoriser authenticatorAuthoriser;

  @Inject
  AuthEnvironment(AdminConstraintSecurityHandler securityHandler,
                  AuthConfiguration configuration,
                  IpWhitelistServletFilter ipWhitelistServletFilter,
                  AuthenticatorAuthoriser authenticatorAuthoriser) {
    this.securityHandler = securityHandler;
    this.ipWhitelistServletFilter = ipWhitelistServletFilter;
    this.configuration = configuration;
    this.authenticatorAuthoriser = authenticatorAuthoriser;
  }

  @Override
  public void init(Environment environment) {

    // Apply IP whitelisting outside the authentication stack so we can provide a different response
    environment.servlets().addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter)
      .addMappingForUrlPatterns(EnumSet.allOf(javax.servlet.DispatcherType.class), true, "/*");

    if (configuration.okta != null) {
      configureOAuth(environment);
    }
    environment.admin().setSecurityHandler(securityHandler);
  }

  private void configureOAuth(final Environment environment) {
    try {
      CachingAuthenticator<String, AccessTokenPrincipal> cachingAuthenticator = new CachingAuthenticator<>(
          environment.metrics(), authenticatorAuthoriser, CacheBuilderSpec.parse(configuration.authCachePolicy));

      OAuthCredentialAuthFilter<AccessTokenPrincipal> oAuthCredentialAuthFilter = new OAuthCredentialAuthFilter.Builder<AccessTokenPrincipal>()
        .setAuthenticator(cachingAuthenticator)
        .setAuthorizer(authenticatorAuthoriser)
        .setPrefix("Bearer")
        .buildAuthFilter();

      environment.jersey().register(new AuthDynamicFeature(oAuthCredentialAuthFilter));
      environment.jersey().register(new AuthValueFactoryProvider.Binder<>(AccessTokenPrincipal.class));

    } catch (Exception e) {
      throw new IllegalStateException("Failed to configure JwtVerifier", e);
    }
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
      setLoginService(new AdminLoginService(authConfiguration.adminUserName, authConfiguration.adminPassword));
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