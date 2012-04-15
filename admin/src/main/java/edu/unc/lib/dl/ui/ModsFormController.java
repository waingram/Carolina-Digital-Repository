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
package edu.unc.lib.dl.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;

import edu.unc.lib.dl.agents.Agent;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.schema.GetBreadcrumbsAndChildrenResponse;
import edu.unc.lib.dl.schema.MetsSubmitIngestObject;
import edu.unc.lib.dl.schema.UserGroupDAO;
import edu.unc.lib.dl.util.Constants;
import edu.unc.lib.dl.util.ModsFormDAO;

public class ModsFormController extends CommonAdminObjectNavigationController {
	private static Logger log = Logger.getLogger(ModsFormController.class);
	private String modsFormUrl;

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors) throws ServletException, IOException {

		return onSubmitInternal(request, response, command, errors);
	}

	protected ModelAndView onSubmitInternal(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors) throws ServletException, IOException {
		Map model = errors.getModel();
	    SAXReader xmlReader = new SAXReader();

	    if ("POST".equals(request.getMethod())) {
	        // We go a query string
	        Document queryDocument;
			try {
				queryDocument = xmlReader.read(request.getInputStream());
		        String query = queryDocument.getRootElement().getStringValue();
		        log.warn(query);

		  		return new ModelAndView("admin", model);

		        
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

		
//		String testMods = "<mods:mods xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:mets=\"http://www.loc.gov/METS/\" xmlns:mods=\"http://www.loc.gov/mods/v3\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><mods:titleInfo><mods:title>A brutalized culture : the horror genre in contemporary Irish literature</mods:title></mods:titleInfo><mods:genre authority=\"local\">http://purl.org/eprint/type/Thesis</mods:genre><mods:typeOfResource>text</mods:typeOfResource><mods:name type=\"personal\"><mods:affiliation>English</mods:affiliation><mods:namePart>Eldred, Laura Gail.</mods:namePart><mods:role><mods:roleTerm>creator</mods:roleTerm></mods:role></mods:name><mods:originInfo><mods:dateIssued encoding=\"iso8601\" keyDate=\"yes\">200605</mods:dateIssued></mods:originInfo><mods:language><mods:languageTerm authority=\"iso639-2b\" type=\"code\">eng</mods:languageTerm></mods:language><mods:abstract>This dissertation...</mods:abstract><mods:accessCondition type=\"use\">The author has granted the University of North Carolina at Chapel Hill a limited, non-exclusive right to make this publication available to the public. The author retains all other rights.</mods:accessCondition><mods:accessCondition type=\"access\">Open access</mods:accessCondition></mods:mods>";

		
		boolean noErrors = true;

		RequestContext requestContext = new RequestContext(request);

//		request.getSession().setAttribute("mods", testMods);
		
		// get data transfer object if it exists
		ModsFormDAO dao = (ModsFormDAO) command;
		if (dao == null) {
			dao = new ModsFormDAO();
		}
		dao.setMessage(null);

		dao.setMods("<mods:mods xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:mets=\"http://www.loc.gov/METS/\" xmlns:mods=\"http://www.loc.gov/mods/v3\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><mods:titleInfo><mods:title>A brutalized culture : the horror genre in contemporary Irish literature</mods:title></mods:titleInfo><mods:genre authority=\"local\">http://purl.org/eprint/type/Thesis</mods:genre><mods:typeOfResource>text</mods:typeOfResource><mods:name type=\"personal\"><mods:affiliation>English</mods:affiliation><mods:namePart>Eldred, Laura Gail.</mods:namePart><mods:role><mods:roleTerm>creator</mods:roleTerm></mods:role></mods:name><mods:originInfo><mods:dateIssued encoding=\"iso8601\" keyDate=\"yes\">200605</mods:dateIssued></mods:originInfo><mods:language><mods:languageTerm authority=\"iso639-2b\" type=\"code\">eng</mods:languageTerm></mods:language><mods:abstract>This dissertation...</mods:abstract><mods:accessCondition type=\"use\">The author has granted the University of North Carolina at Chapel Hill a limited, non-exclusive right to make this publication available to the public. The author retains all other rights.</mods:accessCondition><mods:accessCondition type=\"access\">Open access</mods:accessCondition></mods:mods>");

		String filePath = dao.getFilePath();

		PID parentPid = new PID(dao.getPid());

		String pidPath = tripleStoreQueryService.lookupRepositoryPath(parentPid);

		if (uiUtilityMethods.notNull(dao.getPid())) {

			if ((filePath != null) && (!filePath.equals(""))) {
				StringBuffer buffer = new StringBuffer(128);
				buffer.append(pidPath);

				if (!filePath.startsWith("/")) {
					buffer.append("/");
				}

				buffer.append(filePath);
				pidPath = buffer.toString();

				PID ownerPid = new PID(dao.getOwnerPid());
				try {
					Agent mediator = agentManager.findPersonByOnyen(request.getRemoteUser(), false);

					Agent ownerAgent = agentManager.getAgent(ownerPid, false);

					folderManager.createPath(pidPath, ownerAgent, mediator);

				} catch (Exception e) {
					dao.setMessage(e.getLocalizedMessage().replace("\n", "<br />\n"));
					noErrors = false;
				}
			}

		}

		logger.debug("noErrors is: " + noErrors);
		logger.debug(dao.getMessage());

		MetsSubmitIngestObject wsResponse = new MetsSubmitIngestObject();

		// wsResponse = uiWebService.metsSubmit(ingest);

		if (wsResponse != null) {

			if (Constants.SUCCESS.equals(wsResponse.getMessage())) {

				String message = requestContext.getMessage("submit.file.added", "");

				return new ModelAndView("metsubmitbypid", "metsSubmitByPidDAO", dao);

			} else if (Constants.IN_PROGRESS_THREADED.equals(wsResponse.getMessage())) {
				dao.setMessage(requestContext.getMessage("submit.ingest.progress"));
			} else {
				dao.setMessage(wsResponse.getMessage().replace("\n", "<br />\n"));

				logger.debug("METS submit failure");
			}

		} else { // something went wrong upstream
			dao.setMessage(requestContext.getMessage("submit.ingest.error"));

			logger.debug("METS submit failure");
		}

		GetBreadcrumbsAndChildrenResponse getBreadcrumbsAndChildrenResponse = getBreadcrumbsAndChildren(request,
				modsFormUrl);

		dao.getBreadcrumbs().clear();
		dao.getBreadcrumbs().addAll(getBreadcrumbsAndChildrenResponse.getBreadcrumbs());

		dao.getPaths().clear();
		dao.getPaths().addAll(getBreadcrumbsAndChildrenResponse.getChildren());

		model.put("modsFormDAO", dao);

		return new ModelAndView("modsform", model);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		ModsFormDAO object = new ModsFormDAO();

		object.setMods("<mods:mods xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:mets=\"http://www.loc.gov/METS/\" xmlns:mods=\"http://www.loc.gov/mods/v3\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><mods:titleInfo><mods:title>A brutalized culture : the horror genre in contemporary Irish literature</mods:title></mods:titleInfo><mods:genre authority=\"local\">http://purl.org/eprint/type/Thesis</mods:genre><mods:typeOfResource>text</mods:typeOfResource><mods:name type=\"personal\"><mods:affiliation>English</mods:affiliation><mods:namePart>Eldred, Laura Gail.</mods:namePart><mods:role><mods:roleTerm>creator</mods:roleTerm></mods:role></mods:name><mods:originInfo><mods:dateIssued encoding=\"iso8601\" keyDate=\"yes\">200605</mods:dateIssued></mods:originInfo><mods:language><mods:languageTerm authority=\"iso639-2b\" type=\"code\">eng</mods:languageTerm></mods:language><mods:abstract>This dissertation...</mods:abstract><mods:accessCondition type=\"use\">The author has granted the University of North Carolina at Chapel Hill a limited, non-exclusive right to make this publication available to the public. The author retains all other rights.</mods:accessCondition><mods:accessCondition type=\"access\">Open access</mods:accessCondition></mods:mods>");

		logger.debug("in formBackingObject");

		GetBreadcrumbsAndChildrenResponse getBreadcrumbsAndChildrenResponse = getBreadcrumbsAndChildren(request,
				modsFormUrl);

		UserGroupDAO userGroupRequest = new UserGroupDAO();
		userGroupRequest.setType(Constants.GET_GROUPS);

		userGroupRequest.setUserName("");

		UserGroupDAO userGroupResponse = uiWebService.userGroupOperation(userGroupRequest);

		object.getBreadcrumbs().addAll(getBreadcrumbsAndChildrenResponse.getBreadcrumbs());

		object.getPaths().addAll(getBreadcrumbsAndChildrenResponse.getChildren());

		String pid = request.getParameter("id");

		logger.debug("pid: " + pid);

		if (pid == null) {
			PID collectionsPid = tripleStoreQueryService.fetchByRepositoryPath(Constants.COLLECTIONS);
			pid = collectionsPid.getPid();
		}

		object.setPid(pid);

		return object;
	}

	public String getModsFormUrl() {
		return modsFormUrl;
	}

	public void setModsFormUrl(String modsFormUrl) {
		this.modsFormUrl = modsFormUrl;
	}

}
