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
package edu.unc.lib.dl.search.solr.model;

import org.apache.solr.client.solrj.response.FacetField;

/**
 * Stores a individual facet entry
 * @author bbpennel
 * $Id: GenericFacet.java 2766 2011-08-22 15:29:07Z bbpennel $
 * $URL: https://vcs.lib.unc.edu/cdr/cdr-master/trunk/solr-search/src/main/java/edu/unc/lib/dl/search/solr/model/GenericFacet.java $
 */
public class GenericFacet {
	//Name of the facet group to which this facet belongs.
	protected String fieldName;
	protected long count;
	protected String value;
	protected String displayValue;
	
	public GenericFacet(){
	}
	
	/**
	 * Default constructor which takes the name of the facet and the string representing it.
	 * @param fieldName name of the facet to which this entry belongs.
	 * @param facetString string from which the attributes of the facet will be interpreted.
	 */
	public GenericFacet(String fieldName, String facetString){
		this.count = 0;
		this.fieldName = fieldName;
		this.value = facetString;
		this.displayValue = facetString;
	}
	
	public GenericFacet(FacetField.Count countObject){
		this(countObject, countObject.getFacetField().getName());
	}
	
	public GenericFacet(FacetField.Count countObject, String fieldName){
		this.count = countObject.getCount();
		this.fieldName = fieldName;
		this.value = countObject.getName();
		this.displayValue = countObject.getName();
	}
	
	public GenericFacet(GenericFacet facet){
		this.fieldName = facet.getFieldName();
		this.count = facet.getCount();
		this.value = facet.getValue();
		this.displayValue = facet.getDisplayValue();
	}
	

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}
	
	public String getDisplayValue() {
		return displayValue;
	}

	public String getSearchValue() {
		return value;
	}
}
