/**
 * Function: 拦截器
 * 
 * File Created at 2011-08-11
 * 
 * Copyright 2011 Alibaba.com Croporation Limited.
 * All rights reserved.
 */
package com.alibaba.dubbo.governance.web.common.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.service.pipeline.PipelineContext;
import com.alibaba.citrus.service.pipeline.support.AbstractValve;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

/**
 * @author william.liangf
 * @author guanghui.shigh
 * @author ding.lid
 * @author tony.chenl
 */
public class AuthorizationValve extends AbstractValve {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationValve.class);

    @Autowired
    private HttpServletRequest  request;
    
    @Override
    protected void init() throws Exception {
    }

    public void invoke(PipelineContext pipelineContext) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("AuthorizationValve of uri: " + request.getRequestURI());
        }
        String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && contextPath.length() > 0  && ! "/".equals(contextPath)) {
		    uri = uri.substring(contextPath.length());
		}
		pipelineContext.invokeNext();
    }
	        
    private static Pattern PARAMETER_PATTERN = Pattern.compile("(\\w+)=[\"]?([^,\"]+)[\"]?[,]?\\s*");

    static Map<String, String> parseParameters(String query) {
        Matcher matcher = PARAMETER_PATTERN.matcher(query);
        Map<String, String> map = new HashMap<String, String>();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            map.put(key, value);
        }
        return map;
    }

    static byte[] readToBytes(InputStream in) throws IOException {
        byte[] buf = new byte[in.available()];
        in.read(buf);
        return buf;
    }
}
