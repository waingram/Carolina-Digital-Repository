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
package edu.unc.lib.dl.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class UserToGroupFilter extends OncePerRequestFilter {

	private List<Map<String, String>> pathsAndGroups = null;
	private Timer reloadTimer = new Timer();

	@Override
	public void destroy() {
		super.destroy();
		this.reloadTimer.cancel();
	}
	
	@Override
	protected void initFilterBean() throws ServletException {
		super.initFilterBean();
		pathsAndGroups = loadAccessControl();
		reloadTimer.schedule(new ReloadTask(), 300 * 1000, 300 * 1000);
	}

	class ReloadTask extends TimerTask {
		@Override
		public void run() {
			pathsAndGroups = loadAccessControl();
		}
	}

	// TODO: request wrapper below not needed, use a "cdrRoles" request attribute instead of parameter.
	class FilteredRequest extends HttpServletRequestWrapper {
		String groups;

		public FilteredRequest(ServletRequest request, String groups) {
			super((HttpServletRequest) request);
			this.groups = groups;
		}

		@Override
		public String getParameter(String paramName) {
			logger.debug("in getParameter: " + paramName + " groups: " + groups);

			if (("cdrRoles".equals(paramName)) && (groups != null)) {
				return groups;
			}
			return super.getParameter(paramName);
		}
	}

	@Override
	public void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException,
			ServletException {
		// before we allow the request to proceed, we'll first get the user's
		// role and see if it's an administrator

		String groups = null;
		List<String> groupList = new ArrayList<String>(1);
		boolean permitted = hasAccess(req, groupList);
		groups = groupList.get(0);

		logger.debug("hasAccess groups: " + groups);

		if (permitted) {
			chain.doFilter(new FilteredRequest(req, groups), res);
		} else {
			StringBuffer hostUrl = req.getRequestURL();
			req.setAttribute("nopermission", req.getRequestURI());
			req.setAttribute("hostUrl", hostUrl.toString());
			req.getRequestDispatcher("/WEB-INF/jsp/nopermission.jsp").forward(req, res);
		}
	}

	public boolean hasAccess(HttpServletRequest request, List<String> groupList) {

		try {			
			String path = request.getRequestURI();
			if (path != null) {
				path = path.trim();
			} else {
				logger.debug("Path is null; denying access");

				return false;
			}
			String user = request.getRemoteUser();
			if (user != null) {
				user = user.trim();
				logger.debug("remoteUser: " + user);
			} else {
				logger.debug("remoteUser is NULL");
			}

			logger.debug("requestURI: " + path);

			String members = request.getHeader("isMemberOf");

			
			if((members == null) || (members.trim().equals(""))) {
				logger.debug("members is NULL");
			}

			groupList.add(members);
			
			logger.debug("isMemberOf: " + members);

			Map<String, List<String>> usersAndGroups = null;

			if ((members != null) && (!members.equals(""))) {
				String[] groups = members.split(";");
				usersAndGroups = new HashMap<String, List<String>>();
				List<String> roles = new ArrayList<String>();

				for (String group : groups) {
					roles.add(group);
				}
				usersAndGroups.put(user, roles);
			}

			if ((pathsAndGroups != null) && (pathsAndGroups.size() > 0)) {
				for (Object pathAndGroup : pathsAndGroups) {

					Object[] keys = ((Map) pathAndGroup).keySet().toArray();

					String temp = (String) keys[0];

					logger.debug("processing path: " + temp);

					if (path.startsWith(temp)) { // found a match, so get role
						String role = (String) ((Map) pathAndGroup).get(temp);

						logger.debug("need to match role: " + role);

						if (role.equals("IS_AUTHENTICATED_ANONYMOUSLY")) { // public
							// access

							logger.debug("Anonymous authentication; allowing access");
							return true;
						}

						if (user == null) {
							logger.debug("Remote user not found; denying access");
							return false;
						}

						List<String> roles = null;

						if (usersAndGroups != null) {
							roles = usersAndGroups.get(user);
						}

						if (roles != null) {
							for (String aRole : roles) {
								if (role.equals(aRole)) {
									logger.debug("Had role for path; allowing access");
									return true;
								}
							}
							logger.debug("Did not have role for path; denying access");

							return false;
						} else {
							logger.debug("User without roles; denying access");
							return false;
						}
					}
				}
			}
			logger.debug("Default action; denying access");

		} catch (Exception e) {
			logger.info(e);
		}

		return false;
	}

	private String getControlledPathsFile() {
		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		String result = null;
		result = wac.getBean("controlledPathsFile", String.class);
		if(result == null) {
			logger.error("got NULL controlled paths file setting");
		}
		return result;
	}

	private List<Map<String, String>> loadAccessControl() {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		InputStream is = null;
		BufferedReader reader = null;
		try {
			File f = new File(getControlledPathsFile());
			if (!f.exists()) {
				throw new Error("Filter cannot be started, missing controlled paths file here: "
						+ this.getControlledPathsFile());
			}
			is = new FileInputStream(f);
			reader = new BufferedReader(new InputStreamReader(is));
			String input = "";

			while ((input = reader.readLine()) != null) {
				String path = input.substring(0, input.indexOf('*')).trim();
				logger.debug("path = " + path);

				String role = input.substring(input.indexOf(' ') + 1).trim();
				logger.debug("role = " + role);
				Map<String, String> map = new HashMap<String, String>();

				map.put(path, role);
				results.add(map);
			}
			reader.close();
		} catch (IOException e) {
			throw new Error("There was a problem loading the controlled paths file: " + this.getControlledPathsFile(), e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignored) {
				}
			}
		}
		return results;
	}
}