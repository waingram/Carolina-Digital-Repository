/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unc.lib.dl.cdr.sword.server.servlets;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.swordapp.server.MediaResourceAPI;
import org.swordapp.server.MediaResourceManager;

import edu.unc.lib.dl.cdr.sword.server.SwordConfigurationImpl;

@Controller
@RequestMapping(SwordConfigurationImpl.EDIT_MEDIA_PATH)
public class MediaResourceServlet extends BaseSwordServlet {
	private static Logger LOG = Logger.getLogger(MediaResourceServlet.class);
	
	protected MediaResourceManager mrm;
	protected MediaResourceAPI api;
	
	@PostConstruct
	public void init() throws ServletException {
		
	}
	
	/**
	 * Retrieves the content files of the selected resource.
	 * @param req
	 * @param resp
	 * @param pid
	 */
	@RequestMapping(value = "/{pid}", method = RequestMethod.GET)
	public void doRetrieveContent(HttpServletRequest req, HttpServletResponse resp){
		LOG.debug("Called retrieve content");
		resp.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
	}
	
	/**
	 * Overwrites all the content files of the selected resource. 
	 * @param req
	 * @param resp
	 * @param pid
	 */
	@RequestMapping(value = "/{pid}", method = RequestMethod.PUT)
	public void doReplaceFileContent(HttpServletRequest req, HttpServletResponse resp){
		resp.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
	}
	
	/**
	 * Adds additional content to an existing resource.  If the POSTed content is a Atom Multipart, then it will add
	 * both new content and new metadata.  Does not overwrite the previous contents, but may overlay previous metadata.  
	 * @param req
	 * @param resp
	 * @param pid
	 */
	@RequestMapping(value = "/{pid}", method = RequestMethod.POST)
	public void doAddAdditionalContent(HttpServletRequest req, HttpServletResponse resp){
		resp.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
	}
	
	/**
	 * Deletes all the contents of the select resource, but not the resource itself.
	 * @param req
	 * @param resp
	 * @param pid
	 */
	@RequestMapping(value = "/{pid}", method = RequestMethod.DELETE)
	public void doDeleteContent(HttpServletRequest req, HttpServletResponse resp){
		resp.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
	}
}
