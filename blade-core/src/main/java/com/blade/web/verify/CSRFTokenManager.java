/**
 * Copyright (c) 2015, biezhi 王爵 (biezhi.me@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blade.web.verify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.web.http.Request;
import com.blade.web.http.Response;
import com.blade.web.http.wrapper.Session;

import blade.kit.HashidKit;
import blade.kit.StringKit;

/**
 * CSRF token Manager
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class CSRFTokenManager {
	
	private static Logger LOGGER = LoggerFactory.getLogger(CSRFTokenManager.class);
	
	private static CSRFConfig config = new CSRFConfig();
	
	private static HashidKit HASHID = new HashidKit(config.secret, config.length);
	
	private CSRFTokenManager() {
	}
	
	public static void config(CSRFConfig config){
		CSRFTokenManager.config = config;
		HASHID = new HashidKit(config.secret, config.length);
	}
	
	/**
	 * Create a token
	 * 
	 * @param request		request object
	 * @param response		response object
	 * @return				return token
	 */
    public static String createToken(Request request, Response response) {
        String token = null;
        synchronized (request) {
        	Session session = request.session();
            String objToken = session.attribute(config.session);
            if (null == objToken) {
            	token = HASHID.encode( System.currentTimeMillis() );
            } else {
            	token = objToken.toString();
			}
            session.attribute(config.session, token);
        	if(config.setHeader){
        		response.header(config.header, token);
        	}
        	if(config.setCookie){
        		response.cookie(config.cookiePath, config.cookie, token, config.expire, config.secured);
        	}
        	LOGGER.debug("create csrf_token：{}", token);
        }
        return token;
    }
    
    /**
     * According to form parameter verification
     * 
     * @param request		request object
	 * @param response		response object
     * @return				return verify is success
     */
    public static boolean verifyAsForm(Request request, Response response) {
		// csrftoken attribute from session
		String sToken = request.session().attribute(config.session);
		if (sToken == null) {
			// Generate new token into session
			sToken = CSRFTokenManager.createToken(request, response);
			return true;
		} else {
			String pToken = request.query(config.form);
			if (StringKit.isNotBlank(pToken) && sToken.equals(pToken)) {
				return true;
			}
		}
		
		return false;
	}
    
    /**
     * According to header information verification
     * 
     * @param request		request object
	 * @param response		response object
     * @return				return verify is success
     */
    public static boolean verifyAsHeader(Request request, Response response) {
		// csrftoken attribute from session
		String sToken = request.session().attribute(config.session);
		if (sToken == null) {
			// Generate new token into session
			sToken = CSRFTokenManager.createToken(request, response);
			return true;
		} else {
			String pToken = request.header(config.header);
			if (StringKit.isNotBlank(pToken) && sToken.equals(pToken)) {
				return true;
			}
		}
		
		return false;
	}
    
    /**
     * According to cookie verification
     * 
     * @param request		request object
	 * @param response		response object
     * @return				return verify is success
     */
    public static boolean verifyAsCookie(Request request, Response response) {
		// csrftoken attribute from session
		String sToken = request.session().attribute(config.session);
		if (sToken == null) {
			// Generate new token into session
			sToken = CSRFTokenManager.createToken(request, response);
			return true;
		} else {
			String pToken = request.cookie(config.cookie);
			if (StringKit.isNotBlank(pToken) && sToken.equals(pToken)) {
				return true;
			}
		}
		return false;
	}
    
}
