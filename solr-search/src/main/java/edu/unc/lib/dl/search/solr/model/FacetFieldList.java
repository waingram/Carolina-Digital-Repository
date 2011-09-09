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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import edu.unc.lib.dl.search.solr.model.FacetFieldObject;
import org.apache.solr.client.solrj.response.FacetField;

/**
 * Object containing a list of facets, normal or Hierarchical facet.
 * @author bbpennel
 * $Id: FacetFieldList.java 1682 2010-12-22 19:55:30Z bbpennel $
 * $URL: https://vcs.lib.unc.edu/cdr/cdr-access/trunk/src/main/java/edu/unc/lib/dl/ui/model/response/FacetFieldList.java $
 */
public class FacetFieldList extends ArrayList<FacetFieldObject> {
	private static final long serialVersionUID = 1L;
	
	public FacetFieldList(List<FacetField> facetFields, HashSet<String> hierarchicalNames, HashMap<String,String> fieldNameMappings){
		if (facetFields == null)
			return;
		for (FacetField facetField: facetFields){
			String fieldName = fieldNameMappings.get(facetField.getName()); 
			if (facetField.getValueCount() > 0){
				this.add(new FacetFieldObject(fieldName, facetField, 
						hierarchicalNames.contains(fieldName)));
			}
		}
	}
	
	public FacetFieldObject get(String facetName){
		return FacetFieldList.get(this, facetName);
	}
	
	public static FacetFieldObject get(FacetFieldList list, String facetName){
		int index = indexOf(list, facetName);
		if (index == -1)
			return null;
		return list.get(index);
	}
	
	public int indexOf(String facetName){
		return FacetFieldList.indexOf(this, facetName);
	}
	
	/**
	 * Returns the index of the facet with name facetName
	 * @param facetName name of the facet to find the index of
	 */
	public static int indexOf(FacetFieldList list, String facetName){
		return indexOf(list, facetName, 0);
	}
	
	public int indexOf(String facetName, int startIndex){
		return FacetFieldList.indexOf(this, facetName, startIndex);
	}
	
	public static int indexOf(FacetFieldList list, String facetName, int startIndex){
		int j=startIndex;
		for (; j < list.size() && !list.get(j).getName().equals(facetName); j++);
		if (j >= list.size())
			return -1;
		return j;
	}
	
	public void addMissing(List<String> allFacetNames){
		for (String facetName: allFacetNames){
			if (!this.contains(facetName))
				this.add(new FacetFieldObject(facetName, null, false));
		}
	}
	
	/**
	 * Reorders the list of facets according to the order of field names
	 * specified in orderedList, using a pseudo bubble sort.
	 * @param orderedList List of field names indicating the order to sort the facet list to.
	 */
	public void sort(List<String> orderedList){
		int swapIndex = 0;
		for (String orderEntry: orderedList){
			int matchIndex = indexOf(this, orderEntry, swapIndex);
			if (matchIndex != -1){
				if (matchIndex != swapIndex){
					FacetFieldObject swap = this.get(swapIndex);
					this.set(swapIndex, this.get(matchIndex));
					this.set(matchIndex, swap);
				}
				swapIndex++;
			}
		}
	}
}
