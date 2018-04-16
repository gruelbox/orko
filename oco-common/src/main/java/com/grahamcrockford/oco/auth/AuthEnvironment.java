package com.grahamcrockford.oco.auth;

import java.util.Objects;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;

import com.google.common.cache.CacheBuilderSpec;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import com.okta.jwt.JwtHelper;
import com.okta.jwt.JwtVerifier;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final AdminConstraintSecurityHandler securityHandler;
  private final IpWhitelistContainerRequestFilter ipWhitelistContainerRequestFilter;
  private final AuthConfiguration configuration;

  @Inject
  AuthEnvironment(AdminConstraintSecurityHandler securityHandler,
                  AuthConfiguration configuration,
                  IpWhitelistContainerRequestFilter ipWhitelistContainerRequestFilter) {
    this.securityHandler = securityHandler;
    this.ipWhitelistContainerRequestFilter = ipWhitelistContainerRequestFilter;
    this.configuration = configuration;
  }

  @Override
  public void init(Environment environment) {

    // Apply IP whitelisting outside the authentication stack so we can provide a different response
    environment.jersey().register(ipWhitelistContainerRequestFilter);

    if (configuration.okta != null) {
      configureOAuth(environment);
    }
    environment.admin().setSecurityHandler(securityHandler);
  }

  private void configureOAuth(final Environment environment) {
    try {
      JwtHelper helper = new JwtHelper()
        .setIssuerUrl(configuration.okta.issuer)
        .setClientId(configuration.okta.clientId);

      String audience = configuration.okta.audience;
      if (StringUtils.isNotEmpty(audience)) {
        helper.setAudience(audience);
      }
      JwtVerifier jwtVerifier = helper.build();

      OktaOAuthAuthenticator oktaOAuthAuthenticator = new OktaOAuthAuthenticator(jwtVerifier);

      CachingAuthenticator<String, AccessTokenPrincipal> cachingAuthenticator = new CachingAuthenticator<>(
          environment.metrics(), oktaOAuthAuthenticator, CacheBuilderSpec.parse(configuration.authCachePolicy));

      OAuthCredentialAuthFilter<AccessTokenPrincipal> oAuthCredentialAuthFilter = new OAuthCredentialAuthFilter.Builder<AccessTokenPrincipal>()
        .setAuthenticator(cachingAuthenticator)
        .setAuthorizer(oktaOAuthAuthenticator)
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