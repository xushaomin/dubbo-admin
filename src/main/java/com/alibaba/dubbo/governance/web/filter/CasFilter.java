package com.alibaba.dubbo.governance.web.filter;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.jasig.cas.client.validation.Assertion;

import com.alibaba.dubbo.governance.web.util.WebConstants;
import com.alibaba.dubbo.registry.common.domain.User;
import com.appleframework.config.core.PropertyConfigurer;
import com.appleframework.config.core.util.StringUtils;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CasFilter extends AbstractConfigurationFilter {
		
	public static String SESSION_CAS_KEY  = "_const_cas_assertion_";
		
	/**
     * 过滤地址集合
     */
    private Set<String> exclusionSet = null;
	
    /**
     * 过滤地址
     */
	private String exclusions = null;
	

	public void destroy() {
		
	}
		
	/**
	 * 过滤逻辑：首先判断单点登录的账户是否已经存在本系统中，
	 * 如果不存在使用用户查询接口查询出用户对象并设置在Session中
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		if(isExclusion((HttpServletRequest) request)){
			chain.doFilter(request, response);
			return;
		}
				
		// _const_cas_assertion_是CAS中存放登录用户名的session标志
		Object object = httpRequest.getSession().getAttribute(SESSION_CAS_KEY);
		if (object != null) {
			User user = (User)httpRequest.getSession().getAttribute(WebConstants.CURRENT_USER_KEY);
			if(null == user) {
				Assertion assertion = (Assertion) object;
				String username = assertion.getPrincipal().getName();
				user = new User();
				user.setUsername(username);
				
				AttributePrincipal principal = (AttributePrincipal) httpRequest.getUserPrincipal(); 
				Map<String, Object> attributes = principal.getAttributes();

				user.setId(Integer.parseInt(attributes.get("id").toString()));
				user.setIsadmin(Integer.parseInt(attributes.get("isadmin").toString()));
				if(null != attributes.get("realname")) {
					user.setRealname(attributes.get("realname").toString());
				}
				if(null != attributes.get("roles")) {
					user.setRoles(attributes.get("roles").toString());
				}
				if(null != attributes.get("mobile")) {
					user.setMobile(attributes.get("mobile").toString());
				}
				if(null != attributes.get("email")) {
					user.setEmail(attributes.get("email").toString());
				}
				httpRequest.getSession().setAttribute(WebConstants.CURRENT_USER_KEY, user);
			}
		}
		chain.doFilter(request, response);
	}
	
	/**
     * 判断请求地址是否拦截
     * @param request
     * @return
     * @throws IOException
     * @throws ServletException
     */
    private boolean isExclusion(HttpServletRequest request) throws IOException, ServletException {
		String servletPath = request.getServletPath();
		if(exclusionSet == null) {
			return false;
		}
		else {
			for (String set : exclusionSet) {
				if(servletPath.indexOf(set) > -1) {
					return true;
				}
			}
		}
		return false;
	}

	public void init(FilterConfig filterConfig) throws ServletException {		
		//获取需要过滤拦截地址
		exclusionSet = new HashSet<String>();
        setExclusions(getPropertyFromInitParams(filterConfig, "exclusions", null));
        if((exclusions != null) && exclusions.trim().length() > 0) {
			String[] exclusionArray = exclusions.split(",");
			if(exclusionArray != null && exclusionArray.length > 0){
				for (String exclusionUrl : exclusionArray) {
					exclusionSet.add(exclusionUrl);
				}
			}
		}
        
        String filterExclusions = PropertyConfigurer.getValue("filter.exclusions");
		if(!StringUtils.isNullOrEmpty(filterExclusions)) {
			String[] filterUrlss = filterExclusions.split(",");
			for (String filterUrl : filterUrlss) {
				exclusionSet.add(filterUrl);
			}
		}
	}
	
	public void setExclusions(String exclusions) {
		this.exclusions = exclusions;
	}
		
}

