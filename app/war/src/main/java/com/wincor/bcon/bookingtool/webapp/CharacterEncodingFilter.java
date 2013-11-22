package com.wincor.bcon.bookingtool.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * This is a known problem since PrimeFaces 3.0. It's caused by a change in how
 * it checks if the current HTTP request is an ajax request. It's been
 * identified by a request parameter instead of a request header. When a request
 * parameter is retrieved for the first time before the JSF view is restored,
 * then all request parameters will be parsed using server's default character
 * encoding which is often ISO-8859-1 instead of JSF's own default character
 * encoding UTF-8
 * 
 * See http://stackoverflow.com/questions/10721342/utf-8-form-submit-in-jsf-is-corrupting-data
 */
@WebFilter("/*")
public class CharacterEncodingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}
