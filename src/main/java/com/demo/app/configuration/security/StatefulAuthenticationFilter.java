package com.demo.app.configuration.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.demo.app.util.Constants;

@Component
public class StatefulAuthenticationFilter extends GenericFilterBean {

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		((HttpServletResponse) response).addCookie(new Cookie(Constants.AUTH_HEADER_NAME, "eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJhZWJmM2Y3NS1iNzk5LTRlZmItOGExMi0zZDczN2M2ZTUzNDAiLCJzdWIiOiJzdXBlcnVzZXIiLCJpYXQiOjE1MDM4NTY1NDksImV4cCI6MTUzNTM5MjU0OX0.tDQrOUKVH4RGl45bfcqv5HAUfoRQ62j3187Y0ysjBF34qQSAJtI8AkHljwVBhS6BDnhHBZaFw6NHgO0R-rkCvA"));
		if (((HttpServletRequest) request).getCookies() == null) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			return;
		}
		// Filter is not managed by Spring, beans need to be loaded manually
		if (tokenAuthenticationService == null) {
			ServletContext servletContext = request.getServletContext();
			WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			tokenAuthenticationService = webApplicationContext.getBean(TokenAuthenticationService.class);
		}
		Optional<Cookie> optToken = Arrays.stream(((HttpServletRequest) request).getCookies()).filter(c -> c.getName().equals(Constants.AUTH_HEADER_NAME)).findFirst();
		try {
			if (optToken.isPresent()) {
				String token = optToken.get().getValue();
				// UserAuthentication authentication = (UserAuthentication) tokenAuthenticationService.getAuthenticationForQueues(token);
				tokenAuthenticationService.getAuthenticationForQueues(token);
				// SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
				return;
			}
		} catch (Exception e) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			return;
		}
		filterChain.doFilter(request, response);
	}
}
