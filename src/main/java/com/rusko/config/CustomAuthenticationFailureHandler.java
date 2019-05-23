package com.rusko.config;

import com.rusko.config.exception.InvalidTokenException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
  protected final Log logger = LogFactory.getLog(this.getClass());
  private String defaultFailureUrl;
  private boolean forwardToDestination = false;
  private boolean allowSessionCreation = true;
  private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  public CustomAuthenticationFailureHandler() {
  }

  public CustomAuthenticationFailureHandler(String defaultFailureUrl) {
    this.setDefaultFailureUrl(defaultFailureUrl);
  }

  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
    if (this.defaultFailureUrl == null) {
      this.logger.debug("No failure URL set, sending 401 Unauthorized error");
      response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
    } else {
      this.saveException(request, exception);
      if (this.forwardToDestination) {
        this.logger.debug("Forwarding to " + this.defaultFailureUrl);
        request.getRequestDispatcher(this.defaultFailureUrl).forward(request, response);
      } else if (exception instanceof InvalidTokenException) {
        String verificationUrl = "/verification";
        this.redirectStrategy.sendRedirect(request, response, verificationUrl);
      } else {
        this.logger.debug("Redirecting to " + this.defaultFailureUrl);
        this.redirectStrategy.sendRedirect(request, response, this.defaultFailureUrl);
      }
    }

  }

  public void setDefaultFailureUrl(String defaultFailureUrl) {
    Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl), () -> {
      return "'" + defaultFailureUrl + "' is not a valid redirect URL";
    });
    this.defaultFailureUrl = defaultFailureUrl;
  }

  protected boolean isUseForward() {
    return this.forwardToDestination;
  }

  public void setUseForward(boolean forwardToDestination) {
    this.forwardToDestination = forwardToDestination;
  }

  public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
    this.redirectStrategy = redirectStrategy;
  }

  protected RedirectStrategy getRedirectStrategy() {
    return this.redirectStrategy;
  }

  protected boolean isAllowSessionCreation() {
    return this.allowSessionCreation;
  }

  public void setAllowSessionCreation(boolean allowSessionCreation) {
    this.allowSessionCreation = allowSessionCreation;
  }
}