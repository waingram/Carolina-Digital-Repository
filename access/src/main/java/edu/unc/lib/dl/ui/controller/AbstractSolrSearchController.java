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
package edu.unc.lib.dl.ui.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import edu.unc.lib.dl.security.access.AccessGroupSet;
import edu.unc.lib.dl.search.solr.model.SearchState;
import edu.unc.lib.dl.search.solr.model.SearchRequest;
import edu.unc.lib.dl.search.solr.model.SearchResultResponse;
import edu.unc.lib.dl.search.solr.service.SearchActionService;
import edu.unc.lib.dl.search.solr.service.SearchStateFactory;
import edu.unc.lib.dl.search.solr.util.SearchSettings;
import edu.unc.lib.dl.search.solr.validator.SearchStateValidator;
import edu.unc.lib.dl.ui.model.request.HierarchicalBrowseRequest;
import edu.unc.lib.dl.ui.service.SolrQueryLayerService;

/**
 * Abstract base class for controllers which interact with solr services.
 * @author bbpennel
 * $Id: AbstractSolrSearchController.java 2736 2011-08-08 20:04:52Z count0 $
 * $URL: https://vcs.lib.unc.edu/cdr/cdr-master/trunk/access/src/main/java/edu/unc/lib/dl/ui/controller/AbstractSolrSearchController.java $
 */
public abstract class AbstractSolrSearchController extends CDRBaseController {
	
	@Autowired(required=true)
	protected SolrQueryLayerService queryLayer;
	@Autowired(required=true)
	protected SearchStateValidator briefSearchRequestValidator;
	@Autowired(required=true)
	protected SearchActionService searchActionService;
	@Autowired
	protected SearchSettings searchSettings;
	
	protected SearchRequest generateSearchRequest(HttpServletRequest request){
		return this.generateSearchRequest(request, null, new SearchRequest());
	}
	
	protected SearchRequest generateSearchRequest(HttpServletRequest request, SearchState searchState){
		return this.generateSearchRequest(request, searchState, new SearchRequest());
	}
	
	/**
	 * Builds a search request model object from the provided http servlet request and the provided 
	 * search state.  If the search state is null, then it will attempt to retrieve it from first
	 * the session and if that fails, then from current GET parameters.  Validates the search state
	 * and applies any actions provided as well.
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected SearchRequest generateSearchRequest(HttpServletRequest request, SearchState searchState, SearchRequest searchRequest){
		
		//Get user access groups.  Fill this in later, for now just set to public
		HttpSession session = request.getSession();
		//Get the access group list
		AccessGroupSet accessGroups = getUserAccessGroups(request);
		searchRequest.setAccessGroups(accessGroups);
		
		//Retrieve the last search state
		if (searchState == null){
			searchState = (SearchState)session.getAttribute("searchState");
			if (searchState == null){
				if (searchRequest != null && searchRequest instanceof HierarchicalBrowseRequest){
					searchState = SearchStateFactory.createHierarchicalBrowseSearchState(request.getParameterMap());
				} else {
					String resourceTypes = request.getParameter(searchSettings.searchStateParam("RESOURCE_TYPES"));
					if (resourceTypes == null || resourceTypes.contains(searchSettings.resourceTypeFile)){
						searchState = SearchStateFactory.createSearchState(request.getParameterMap());
					} else {
						searchState = SearchStateFactory.createCollectionBrowseSearchState(request.getParameterMap());
					}
				}
			} else {
				session.removeAttribute("searchState");
			}
		}
		
		//Perform actions on search state
		String actionsParam = request.getParameter(searchSettings.searchStateParams.get("ACTIONS"));
		if (actionsParam != null){
			searchActionService.executeActions(searchState, actionsParam);
		}
		
		//Validate the search state to make sure that it contains appropriate values and field names
		briefSearchRequestValidator.validate(searchState);
		
		//Store the search state into the search request
		searchRequest.setSearchState(searchState);
		
		return searchRequest;
	}
	
	protected SearchResultResponse getSearchResults(SearchRequest searchRequest){
		return queryLayer.getSearchResults(searchRequest);
	}

	public SearchActionService getSearchActionService() {
		return searchActionService;
	}

	public void setSearchActionService(SearchActionService searchActionService) {
		this.searchActionService = searchActionService;
	}

	public SolrQueryLayerService getQueryLayer() {
		return queryLayer;
	}

	public void setQueryLayer(SolrQueryLayerService queryLayer) {
		this.queryLayer = queryLayer;
	}
}
