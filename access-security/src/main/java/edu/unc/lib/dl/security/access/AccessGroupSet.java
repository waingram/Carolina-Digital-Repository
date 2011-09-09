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
package edu.unc.lib.dl.security.access;

import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * Storage class for a list of access groups related to a single entity.
 * @author bbpennel
 * $Id: AccessGroupSet.java 1900 2011-03-14 20:57:38Z bbpennel $
 * $URL: https://vcs.lib.unc.edu/cdr/cdr-access/trunk/src/main/java/edu/unc/lib/dl/security/AccessGroupSet.java $
 */
public class AccessGroupSet extends HashSet<String> {
	private static final long serialVersionUID = 1L;
	
	public AccessGroupSet(){
		super();
	}
	
	public AccessGroupSet(String group){
		super();
		addAccessGroup(group);
	}
	
	public AccessGroupSet(String[] groups){
		super();
		addAccessGroups(groups);
	}
	
	public void addAccessGroups(String[] groups){
		if (groups == null)
			return;
		for (String group: groups){
			if (group != null && group.length() > 0)
				this.add(group);
		}
	}
	
	public void addAccessGroup(String group){
		if (group == null)
			return;
		this.add(group);
	}
	
	/**
	 * Determines is any of the objects contained within the specified collection
	 * are present in the access group set.
	 * @param c collection to be checked for matches.
	 * @return true if this collection contains any objects from the specified collection
	 */
	public static boolean containsAny(AccessGroupSet accessGroupSet, Collection<String> c){
		if (c == null || c.size() == 0 || accessGroupSet.size() == 0)
			return false;
		Iterator<String> cIt = c.iterator();
		while (cIt.hasNext())
			if (accessGroupSet.contains(cIt.next()))
				return true;
		return false;
	}
	
	public boolean containsAny(Collection<String> c){
		return containsAny(this, c);
	}
	
	public String joinAccessGroups(String delimiter, String prefix, boolean escapeColons){
		StringBuffer sb = new StringBuffer();
		String value;
		boolean firstEntry = true;
		Iterator<String> agIt = this.iterator();
		while (agIt.hasNext()){
			value = agIt.next();
			if (escapeColons)
				value = value.replaceAll("\\:", "\\\\:");
			if (firstEntry)
				firstEntry = false;
			else sb.append(delimiter);
			sb.append(prefix);
			sb.append(value);
		}
		
		return sb.toString();
	}
	
	public String toString(){
		return this.joinAccessGroups(" ", "", true);
	}
}
