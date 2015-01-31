/**
 * Project: dubbo.registry-1.1.0-SNAPSHOT
 * 
 * File Created at 2010-4-15
 * $Id: User.java 181192 2012-06-21 05:05:47Z tony.chenl $
 * 
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.alibaba.dubbo.registry.common.domain;

import java.util.Arrays;
import java.util.List;

import com.alibaba.dubbo.registry.common.route.ParseUtils;
import com.appleframework.config.core.PropertyConfigurer;

/**
 * User
 * 
 * @author william.liangf
 */
public class User extends org.jasig.cas.client.model.User {

    private static final long serialVersionUID = 7330539198581235339L;
    
    public static final String REALM = "dubbo";

    //public static final String ROOT = PropertyConfigurer.getValue("dubbo.admin.root");

    //public static final String GUEST = PropertyConfigurer.getValue("dubbo.admin.guest");
            
    private String department;
        
    private String locale;
    
    private String servicePrivilege;

    private List<String> servicePrivileges;

    public boolean hasServicePrivilege(String[] services) {
        if (services == null || services.length == 0)
            throw new IllegalArgumentException("services == null");
        for (String service : services) {
            boolean r = hasServicePrivilege(service);
            if (! r)
                return false;
        }
        return true;
    }
    
    public static boolean isValidPrivilege(String servicePrivilege) {
    	if (servicePrivilege == null || servicePrivilege.length() == 0) {
    		return true;
    	}
    	String[] privileges = servicePrivilege.trim().split("\\s*,\\s*");
    	for (String privilege : privileges) {
            if(privilege.endsWith("*")){
            	privilege = privilege.substring(0, privilege.length() - 1);
            }
            if (privilege.indexOf('*') > -1) {
        		return false;
        	}
    	}
        return true;
    }
    
    public boolean canGrantPrivilege(String servicePrivilege) {
    	if (servicePrivilege == null || servicePrivilege.length() == 0) {
    		return true;
    	}
    	if (servicePrivileges == null || servicePrivileges.size() == 0) {
    		return false;
    	}
    	String[] privileges = servicePrivilege.trim().split("\\s*,\\s*");
		for (String privilege : privileges) {
			boolean hasPrivilege = false;
			for (String ownPrivilege : servicePrivileges) {
	            if (matchPrivilege(ownPrivilege, privilege)) {
	            	hasPrivilege = true;
	        	}
	    	}
			if (! hasPrivilege) {
				return false;
			}
    	}
    	return true;
    }

    private boolean matchPrivilege(String ownPrivilege, String privilege) {
    	if ("*".equals(ownPrivilege) || ownPrivilege.equals(privilege)) {
    		return true;
    	}
    	if(privilege.endsWith("*")){
        	if(! ownPrivilege.endsWith("*")){
        		return false;
            }
        	privilege = privilege.substring(0, privilege.length() - 1);
        	ownPrivilege = ownPrivilege.substring(0, ownPrivilege.length() - 1);
        	return privilege.startsWith(ownPrivilege);
        } else {
        	if(ownPrivilege.endsWith("*")){
        		ownPrivilege = ownPrivilege.substring(0, ownPrivilege.length() - 1);
            }
        	return privilege.startsWith(ownPrivilege);
        }
    }

	public boolean hasServicePrivilege(String service) {
		if (service == null || service.length() == 0)
			return false;
		if(getIsadmin() == 1) {
		    return true;
		}
		if (roles == null) {
			return false;
		}
		String ROOT = PropertyConfigurer.getValue("dubbo.admin.root");
		if (ROOT == null) {
			return false;
		}
		if(roles.indexOf(ROOT) > -1) {
		    return true;
		}
		
		if (servicePrivileges != null && servicePrivileges.size() > 0) {
    		for (String privilege : servicePrivileges) {
                boolean ok = ParseUtils.isMatchGlobPattern(privilege,service);
                if (ok) {
                	return true;
                }
    		}
		}
		return false;
	}
	
	public boolean isRoot() {
		if(getIsadmin() == 1) {
		    return true;
		}
		if (roles == null) {
			return false;
		}
		String ROOT = PropertyConfigurer.getValue("dubbo.admin.root");
		if (ROOT == null) {
			return false;
		}
		if(roles.indexOf(ROOT) > -1) {
		    return true;
		}
		return false;
	}
	
	public String getServicePrivilege() {
		return servicePrivilege;
	}

	public void setServicePrivilege(String servicePrivilege) {
		this.servicePrivilege = servicePrivilege;
		if (servicePrivilege != null && servicePrivilege.length() > 0) {
			servicePrivileges = Arrays.asList(servicePrivilege.trim().split("\\s*,\\s*"));
		}
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

}
