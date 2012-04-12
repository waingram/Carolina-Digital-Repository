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
package edu.unc.lib.dl.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.CDATASection;
import org.w3c.dom.NodeList;

import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.xml.JDOMNamespaceUtil;

/**
 * Provides an adapter for querying and modifying the triple store.
 * 
 * @author count0
 * 
 */
public class TripleStoreQueryServiceMulgaraImpl implements TripleStoreQueryService {
	private static final Log log = LogFactory.getLog(TripleStoreQueryServiceMulgaraImpl.class);

	private String inferenceRulesModelUri;

	private String inferredRIModelUri;
	private String itqlEndpointURL;
	private String sparqlEndpointURL;
	private String name;
	private String pass;
	private String resourceIndexModelUri;
	private String serverModelUri;
	private HttpClient httpClient;
	private ObjectMapper mapper;
	private PID collections;

	private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;

	public TripleStoreQueryServiceMulgaraImpl() {
		this.multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
		this.httpClient = new HttpClient(this.multiThreadedHttpConnectionManager);
		this.mapper = new ObjectMapper();
		this.collections = null;
	}

	public void destroy() {
		this.httpClient = null;
		this.mapper = null;
		this.multiThreadedHttpConnectionManager.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.services.TripleStoreService#fetchAllContents(edu.unc.lib .dl.services.PID)
	 */
	public List<PID> fetchAllContents(PID key) {
		String query = String.format(
				"select $desc from <%1$s> where walk( <%3$s> <%2$s> $child and $child <%2$s> $desc);",
				this.getResourceIndexModelUri(), ContentModelHelper.Relationship.contains.getURI(), key.getURI());
		return this.lookupDigitalObjects(query);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.services.TripleStoreService#fetchAllContents(edu.unc.lib .dl.services.PID)
	 */
	public Map<String, PID> fetchChildSlugs(PID parent) {
		Map<String, PID> result = new HashMap<String, PID>();
		String query = String.format(
				"select $slug $child from <%1$s> where <%2$s> <%3$s> $child and $child <%4$s> $slug;",
				this.getResourceIndexModelUri(), parent.getURI(), ContentModelHelper.Relationship.contains.getURI(),
				ContentModelHelper.CDRProperty.slug.getURI());
		List<List<String>> res = this.lookupStrings(query);
		for (List<String> list : res) {
			result.put(list.get(0), new PID(list.get(1)));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.services.TripleStoreService#fetchByPredicateAndLiteral (java.lang.String, java.lang.String)
	 */
	public List<PID> fetchByPredicateAndLiteral(String predicateURI, String literal) {
		String query = String.format("select $pid from <%1$s> where $pid <%2$s> '%3$s';",
				this.getResourceIndexModelUri(), predicateURI, literal);
		return this.lookupDigitalObjects(query);
	}
	
	@Override
	public List<String> fetchBySubjectAndPredicate(PID subject, String predicateURI){
		String query = String.format(
				"select $literal from <%1$s> where <%2$s> <%3$s> $literal;",
				this.getResourceIndexModelUri(), subject.getURI(), predicateURI);
		List<List<String>> res = this.lookupStrings(query);
		if (res == null)
			return null;
		List<String> literals = new ArrayList<String>();
		for (List<String> row: res){
			if (row.size() > 0)
				literals.add(row.get(0));
		}
		return literals;
	}
	
	@Override
	public String fetchFirstBySubjectAndPredicate(PID subject, String predicateURI) {
		String query = String.format(
				"select $literal from <%1$s> where <%2$s> <%3$s> $literal;",
				this.getResourceIndexModelUri(), subject.getURI(), predicateURI);
		
		return this.lookupFirstString(query);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.services.TripleStoreService#fetchByRepositoryPath(java .lang.String)
	 */
	public PID fetchByRepositoryPath(String path) {
		// TODO needs many CDRs one Fedora fix
		PID result = null;
		StringBuffer query = new StringBuffer();
		path = path.trim();
		List<String> slugs;
		if ("/".equals(path) || "REPOSITORY".equals(path)) {
			slugs = new ArrayList<String>();
			slugs.add("REPOSITORY");
		} else {
			slugs = Arrays.asList(path.split("/"));
		}
		String first = slugs.get(0);
		if ("".equals(first)) {
			slugs.set(0, "REPOSITORY");
		} else if (!"REPOSITORY".equals(first)) {
			List<String> newslugs = new ArrayList<String>();
			newslugs.addAll(slugs);
			newslugs.add(0, "REPOSITORY");
			slugs = newslugs;
		}
		query.append("select $r").append(slugs.size() - 1).append(" from <%1$s>");
		for (int i = 0; i < slugs.size(); i++) {
			if (i == 0) {
				query.append(" where $r").append(i).append(" <%3$s> '").append(slugs.get(i)).append("'");
			} else {
				query.append(" and $r").append(i - 1).append(" <%2$s> $r").append(i);
				query.append(" and $r").append(i).append(" <%3$s> '").append(slugs.get(i)).append("'");
			}
		}
		String q = String.format(query.append(";").toString(), this.getResourceIndexModelUri(),
				ContentModelHelper.Relationship.contains.getURI(), ContentModelHelper.CDRProperty.slug.getURI());
		List<PID> results = this.lookupDigitalObjects(q);
		if (results.size() == 1) {
			result = results.get(0);
		} else if (results.size() == 0) {
			result = null;
		} else {
			StringBuffer pidlist = new StringBuffer();
			for (PID pid : results) {
				pidlist.append(" ").append(pid.toString());
			}
			throw new IllegalRepositoryStateException("Multiple objects share the same path " + path + " PIDS:"
					+ pidlist.toString());
		}
		return result;
	}

	private PID fetchCollectionsObject() {
		return fetchCollectionsObject(false);
	}

	private PID fetchCollectionsObject(boolean refresh) {
		if (refresh || collections == null) {
			collections = this.fetchByRepositoryPath("/Collections");
		}
		return collections;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.services.TripleStoreService#fetchCollection(edu.unc.lib .dl.services.PID)
	 */
	public PID fetchCollection(PID key) {
		PID result = null;
		String query = String.format(
				"select $pid from <%1$s> where $keypid <%2$s> $pid and $keypid <http://mulgara.org/mulgara#is> <%3$s>;",
				this.getInferredModelUri(), ContentModelHelper.EntailedRelationship.isMemberOfCollection.getURI(),
				key.getURI());
		List<PID> response = this.lookupDigitalObjects(query);
		if (!response.isEmpty()) {
			if (response.size() > 1) {
				throw new IllegalRepositoryStateException(
						"The repository is in an illegal state, an object is part of more than one collection.", response);
			} else {
				result = response.get(0);
			}
		}
		return result;
	}

	public PID fetchContainer(PID child) {
		String query = String.format("select $pid from <%1$s> where $pid <%2$s> <%3$s>;",
				this.getResourceIndexModelUri(), ContentModelHelper.Relationship.contains, child.getURI());
		List<PID> result = this.lookupDigitalObjects(query);
		if (result.size() > 1) {
			throw new IllegalRepositoryStateException("An objects seems to be contained by more than one object: " + child);
		} else if (result.size() == 0) {
			return null; // only the REPOSITORY object.
		} else {
			return result.get(0);
		}
	}

	public List<PID> lookupAllContainersAbove(PID pid) {
		List<PID> result = new ArrayList<PID>();

		// construct path from contains relationships
		StringBuffer query = new StringBuffer();
		query.append("select $p <%2$s> $c from <%1$s>").append(" where walk( $p <%2$s> <%3$s> and $p <%2$s> $c);");
		String q = String.format(query.toString(), this.getResourceIndexModelUri(),
				ContentModelHelper.Relationship.contains.getURI(), pid.getURI());

		List<List<String>> response = this.lookupStrings(q);
		if (!response.isEmpty()) {
			// build a map of key:parent to val:child
			Map<String, String> parent2child = new HashMap<String, String>();
			for (List<String> solution : response) {
				parent2child.put(solution.get(0), solution.get(2));
			}
			// follow REPOSITORY through all parents, building path
			if (parent2child.containsKey(ContentModelHelper.Administrative_PID.REPOSITORY.getPID().getURI())) {
				result.add(ContentModelHelper.Administrative_PID.REPOSITORY.getPID());
			} else {
				throw new IllegalRepositoryStateException(
						"The repository object should be in the parent tree for every CDR object.");
			}
			for (String step = ContentModelHelper.Administrative_PID.REPOSITORY.getPID().getURI(); parent2child
					.containsKey(step); step = parent2child.get(step)) {
				result.add(new PID(parent2child.get(step)));
			}
		}
		return result;
	}

	@Deprecated
	public List<PID> fetchObjectReferences(List<PID> pids) {
		// this query can be very inefficient
		StringBuffer q = new StringBuffer();
		boolean first = true;
		if (pids.size() > 1) {
			q.append(" ( ");
		}
		for (PID pid : pids) {
			if (first) {
				first = false;
				q.append(" $obj <mulgara:is> <").append(pid.getURI()).append(">");
			} else {
				q.append(" or");
				q.append(" $obj <mulgara:is> <").append(pid.getURI()).append(">");
			}
		}
		if (pids.size() > 1) {
			q.append(" )");
		}

		String query = String.format("select $pid from <%1$s> where $pid $rel $obj and $pid <%2$s> <%3$s> and %4$s ;",
				this.getResourceIndexModelUri(), ContentModelHelper.FedoraProperty.state,
				ContentModelHelper.FedoraProperty.Active, q.toString());
		return this.lookupDigitalObjects(query);
	}

	/**
	 * Fetches a list of PIDs that depend on this object or it's descendants.
	 * 
	 * @param pid
	 *           the PID of the object
	 * @return a list of dependent object PIDs
	 */
	public List<PID> fetchObjectReferences(PID pid) {
		List<PID> result = null;
		// fetch references to this pid
		String query = String.format("select $pid from <%1$s> where $pid $rel $obj and $obj <mulgara:is> <%2$s>;",
				this.getResourceIndexModelUri(), pid.getURI());
		result = this.lookupDigitalObjects(query);

		// fetch references to descendants
		String query2 = String.format("select $pid from <%1$s> where $pid $rel $obj and walk( " + "<%2$s> <%3$s> $child"
				+ " and $child <%3$s> $obj);", this.getResourceIndexModelUri(), pid.getURI(),
				ContentModelHelper.Relationship.contains.getURI());
		result.addAll(this.lookupDigitalObjects(query2));
		return result;
	}
	
	@Override
	public String fetchState(PID pid) {
		String query = String.format("select $state from <%1$s> where <%2$s> <%3$s> $state;",
				this.getResourceIndexModelUri(), pid.getURI(), ContentModelHelper.FedoraProperty.Active.toString());
		return this.lookupFirstString(query);
	}

	public String getInferenceRulesModelUri() {
		return this.inferenceRulesModelUri;
	}

	public String getInferredModelUri() {
		return this.inferredRIModelUri;
	}

	public String getItqlEndpointURL() {
		return itqlEndpointURL;
	}

	public String getName() {
		return name;
	}

	public String getPass() {
		return pass;
	}

	private List<Element> getQuerySolutions(String response) {
		List<Element> result = null;
		Document r = null;
		StringReader sr = null;
		try {
			sr = new StringReader(response);
			r = new SAXBuilder().build(sr);
			result = extracted(r.getRootElement().getChild("query", JDOMNamespaceUtil.MULGARA_TQL_NS)
					.getChildren("solution", JDOMNamespaceUtil.MULGARA_TQL_NS));
			return result;
		} catch (IOException e) {
			log.error(response);
			throw new RuntimeException("IOException reading Mulgara answer string.", e);
		} catch (JDOMException e) {
			log.error(response);
			throw new RuntimeException("Unexpected error parsing Mulgara answer.", e);
		} finally {
			if (sr != null) {
				sr.close();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Element> extracted(List children) {
		return children;
	}

	public String getResourceIndexModelUri() {
		return resourceIndexModelUri;
	}

	public String getServerModelUri() {
		return serverModelUri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.services.TripleStoreService#isContainer(edu.unc .lib.dl.services.PID)
	 */
	public boolean isContainer(PID key) {
		String query = String
				.format(
						"select $keypid from <%1$s> where $keypid <%2$s> <%3$s> and $keypid <http://mulgara.org/mulgara#is> <%4$s>;",
						this.getResourceIndexModelUri(), ContentModelHelper.FedoraProperty.hasModel,
						ContentModelHelper.Model.CONTAINER, key.getURI());
		List<URI> response = this.lookupResources(query.toString());
		if (!response.isEmpty()) {
			if (response.size() > 0) {
				return true;
			}
		} else {
			return false;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.services.TripleStoreService#lookupContentModel(edu.unc .lib.dl.services.PID)
	 */
	public List<URI> lookupContentModels(PID key) {
		List<URI> result = null;
		String query = String
				.format(
						"select $resource from <%1$s> where $keypid <%2$s> $resource and $keypid <http://mulgara.org/mulgara#is> <%3$s>;",
						this.getResourceIndexModelUri(), ContentModelHelper.FedoraProperty.hasModel.getURI(), key.getURI());
		List<URI> response = this.lookupResources(query.toString());
		if (!response.isEmpty()) {
			if (response.size() == 0) {
				throw new IllegalRepositoryStateException(
						"The repository is in an illegal state, an object has no content models.", key);
			} else {
				result = response;
			}
		}
		return result;
	}

	/**
	 * Lookup a list of digital object ids. The query must return pairs of $pid and $repositoryPath.
	 * 
	 * @param query
	 *           a query that returns resource values, one per row
	 * @return a list of the URIs found
	 * @throws RemoteException
	 *            for communication failure
	 */
	private List<PID> lookupDigitalObjects(String query) {
		List<PID> result = new ArrayList<PID>();
		String response = this.sendTQL(query);
		for (Element solution : getQuerySolutions(response)) {
			Object o = solution.getContent(0);
			if (o instanceof Element) {
				String res = ((Element) o).getAttributeValue("resource");
				if (res != null) {
					result.add(new PID(res.substring(res.indexOf("/") + 1)));
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.services.TripleStoreService#lookupRepositoryPath(edu.unc .lib.dl.services.PID)
	 */
	@Override
	public String lookupRepositoryPath(PID key) {
		String result = null;
		// Walk the hierarchy and gather the slugs for each child as we go.
		StringBuffer squery = new StringBuffer();
		squery.append("select $p $c $slug from <%1$s>").append(" where walk( $p <%2$s> <%3$s> and $p <%2$s> $c)")
				.append(" and $c <%4$s> $slug;");
		String sq = String.format(squery.toString(), this.getResourceIndexModelUri(),
				ContentModelHelper.Relationship.contains.getURI(), key.getURI(),
				ContentModelHelper.CDRProperty.slug.getURI());
		List<List<String>> sresponse = this.lookupStrings(sq);

		if (!sresponse.isEmpty()) {
			Map<String, String> child2slug = new HashMap<String, String>();
			Map<String, String> parent2child = new HashMap<String, String>();
			for (List<String> solution : sresponse) {
				parent2child.put(solution.get(0), solution.get(1));
				child2slug.put(solution.get(1), solution.get(2));
			}

			StringBuffer sb = new StringBuffer();
			for (String step = ContentModelHelper.Administrative_PID.REPOSITORY.getPID().getURI(); parent2child
					.containsKey(step); step = parent2child.get(step)) {
				String stepChild = parent2child.get(step);
				sb.append("/").append(child2slug.get(stepChild));
			}

			result = sb.toString();
		} else {
			return "/";
		}
		return result;
	}

	// USEFUL PUBLIC METHODS BELOW

	/**
	 * Generates a list containing PathInfo objects for each hierarchical step in the path leading up to and including
	 * PID key. If the object is an orphan, then an empty list is returned
	 */
	@Override
	public List<PathInfo> lookupRepositoryPathInfo(PID key) {
		List<PathInfo> result = new ArrayList<PathInfo>();

		// now get all the child slugs
		StringBuffer squery = new StringBuffer();
		squery.append("select $p $pid $slug $label from <%1$s>")
				.append(" where walk( $p <%2$s> <%3$s> and $p <%2$s> $pid)").append(" and $pid <%4$s> $slug")
				.append(" and $pid <%5$s> $label;");
		String sq = String.format(squery.toString(), this.getResourceIndexModelUri(),
				ContentModelHelper.Relationship.contains, key.getURI(), ContentModelHelper.CDRProperty.slug,
				ContentModelHelper.FedoraProperty.label);
		List<List<String>> sresponse = this.lookupStrings(sq);

		if (!sresponse.isEmpty()) {
			// add the REPOSITORY object path info
			PathInfo rootInfo = new PathInfo();
			rootInfo.setLabel("Repository Home");
			rootInfo.setPath("/");
			rootInfo.setSlug("REPOSITORY");
			rootInfo.setPid(ContentModelHelper.Administrative_PID.REPOSITORY.getPID());
			result.add(rootInfo);

			Map<String, PathInfo> parent2child = new HashMap<String, PathInfo>();

			// Build the PathInfo objects representing each tuple then store them as the child of their parent.
			for (List<String> solution : sresponse) {
				PathInfo info = new PathInfo();
				info.setPid(new PID(solution.get(1)));
				info.setSlug(solution.get(2));
				info.setLabel(solution.get(3));
				parent2child.put(solution.get(0), info);
			}

			StringBuffer sb = new StringBuffer();
			// Now add the steps into the file result list in the correct walk order.
			for (String step = ContentModelHelper.Administrative_PID.REPOSITORY.getPID().getURI(); parent2child
					.containsKey(step); step = parent2child.get(step).getPid().getURI()) {
				PathInfo stepChild = parent2child.get(step);
				sb.append("/").append(stepChild.getSlug());
				stepChild.setPath(sb.toString());
				result.add(stepChild);
			}
		}
		return result;
	}

	/**
	 * @param query
	 *           a query that returns resource values, one per row
	 * @return a list of the URIs found
	 * @throws RemoteException
	 *            for communication failure
	 */
	private List<URI> lookupResources(String query) {
		List<URI> result = new ArrayList<URI>();
		String response = this.sendTQL(query);
		for (Element solution : getQuerySolutions(response)) {
			Object o = solution.getContent(0);
			if (o instanceof Element) {
				String res = ((Element) o).getAttributeValue("resource");
				if (res != null) {
					try {
						result.add(new URI(res));
					} catch (URISyntaxException exception) {
						throw new IllegalRepositoryStateException("Found an invalid resource URI in the resource index.");
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param query
	 *           a query that returns string values, literal or URI
	 * @return a list of the literal strings found
	 * @throws RemoteException
	 *            for communication failure
	 */
	private List<List<String>> lookupStrings(String query) {
		List<List<String>> result = new ArrayList<List<String>>();
		String response = this.sendTQL(query);
		for (Element solution : getQuerySolutions(response)) {
			List<String> row = new ArrayList<String>();
			for (Object o : solution.getChildren()) {
				if (o instanceof Element) {
					Element el = (Element) o;
					if (el.getAttributeValue("resource") != null) {
						row.add(el.getAttributeValue("resource"));
					} else {
						row.add(el.getTextTrim());
					}
				}
			}
			result.add(row);
		}
		return result;
	}
	
	private String lookupFirstString(String query) {
		String response = this.sendTQL(query);
		for (Element solution : getQuerySolutions(response)) {
			if (solution.getChildren().size() > 0){
				Object o = solution.getChildren().get(0);
				if (o instanceof Element) {
					Element el = (Element) o;
					String resourceAttribute = el.getAttributeValue("resource");
					if (resourceAttribute == null) {
						return el.getTextTrim();
					}
					return resourceAttribute;
				}
			}
		}
		return null;
	}

	public List<List<String>> queryResourceIndex(String query) {
		return this.lookupStrings(query);
	}

	private void reportSOAPFault(SOAPMessage reply) throws SOAPException {
		String error = reply.getSOAPBody().getFirstChild().getTextContent();
		throw new RuntimeException("There was a SOAP Fault from Mulgara: " + error);
	}

	private String sendTQL(String query) {
		log.debug(query);
		String result = null;
		SOAPMessage reply = null;
		SOAPConnection connection = null;
		try {
			// Next, create the actual message
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage message = messageFactory.createMessage();
			message.getMimeHeaders().setHeader("SOAPAction", "itqlbean:executeQueryToString");
			SOAPBody soapBody = message.getSOAPPart().getEnvelope().getBody();
			soapBody.addNamespaceDeclaration("xsd", JDOMNamespaceUtil.XSD_NS.getURI());
			soapBody.addNamespaceDeclaration("xsi", JDOMNamespaceUtil.XSI_NS.getURI());
			soapBody.addNamespaceDeclaration("itqlbean", this.getItqlEndpointURL());
			SOAPElement eqts = soapBody.addChildElement("executeQueryToString", "itqlbean");
			SOAPElement queryStr = eqts.addChildElement("queryString", "itqlbean");
			queryStr.setAttributeNS(JDOMNamespaceUtil.XSI_NS.getURI(), "xsi:type", "xsd:string");
			CDATASection queryCDATA = message.getSOAPPart().createCDATASection(query);
			queryStr.appendChild(queryCDATA);
			message.saveChanges();

			// First create the connection
			SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory.newInstance();
			connection = soapConnFactory.createConnection();
			reply = connection.call(message, this.getItqlEndpointURL());

			if (reply.getSOAPBody().hasFault()) {
				reportSOAPFault(reply);
				if (log.isDebugEnabled()) {
					// log the full soap body response
					DOMBuilder builder = new DOMBuilder();
					org.jdom.Document jdomDoc = builder.build(reply.getSOAPBody().getOwnerDocument());
					log.debug(new XMLOutputter().outputString(jdomDoc));
				}
			} else {
				NodeList nl = reply.getSOAPPart().getEnvelope().getBody()
						.getElementsByTagNameNS("*", "executeQueryToStringReturn");
				if (nl.getLength() > 0) {
					result = nl.item(0).getTextContent();
				}
				log.debug(result);
			}
		} catch (SOAPException e) {
			log.error("Failed to prepare or send iTQL via SOAP", e);
			throw new RuntimeException("Cannot query triple store at " + this.getItqlEndpointURL(), e);
		} finally {
			try {
				connection.close();
			} catch (SOAPException e) {
				log.error("Failed to close SOAP connection", e);
				throw new RuntimeException("Failed to close SOAP connection for triple store at "
						+ this.getItqlEndpointURL(), e);
			}
		}
		return result;
	}

	@Override
	public Map<?, ?> sendSPARQL(String query) {
		return sendSPARQL(query, "json");
	}

	@Override
	public Map<?, ?> sendSPARQL(String query, String format) {
		return sendSPARQL(query, format, 3);
	}

	public Map<?, ?> sendSPARQL(String query, String format, int retries) {
		PostMethod post = null;
		try {
			String postUrl = this.getSparqlEndpointURL();
			if (format != null) {
				postUrl += "?format=" + format;
			}
			post = new PostMethod(postUrl);
			post.setRequestHeader("Content-Type", "application/sparql-query");
			post.addParameter("query", query);

			int statusCode = httpClient.executeMethod(post);
			if (statusCode != HttpStatus.SC_OK) {
				throw new RuntimeException("SPARQL POST method failed: " + post.getStatusLine());
			} else {
				log.debug("SPARQL POST method succeeded: " + post.getStatusLine());
				byte[] resultBytes = post.getResponseBody();
				log.debug(new String(resultBytes, "utf-8"));
				if ("json".equals(format)) {
					return (Map<?, ?>) mapper.readValue(new ByteArrayInputStream(resultBytes), Object.class);
				} else {
					Map<String, String> resultMap = new HashMap<String, String>();
					String resultString = new String(resultBytes, "utf-8");
					resultMap.put("results", resultString);
					return resultMap;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (post != null)
				post.releaseConnection();
		}
	}

	public void setItqlEndpointURL(String baseURL) {
		this.itqlEndpointURL = baseURL;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public void setServerModelUri(String serverModelUri) {
		this.serverModelUri = serverModelUri;
		this.resourceIndexModelUri = serverModelUri + "ri";
		this.inferredRIModelUri = serverModelUri + "ri_inferred";
		this.inferenceRulesModelUri = serverModelUri + "krules";
	}

	/**
	 * @param query
	 *           an ITQL command
	 * @return the message returned by Mulgara
	 * @throws RemoteException
	 *            for communication failure
	 */
	public String storeCommand(String query) {
		String result = null;
		String response = this.sendTQL(query);
		if (response != null) {
			StringReader sr = new StringReader(response);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
			XMLEventReader r = null;
			try {
				boolean inMessage = false;
				StringBuffer message = new StringBuffer();
				r = factory.createXMLEventReader(sr);
				while (r.hasNext()) {
					XMLEvent e = r.nextEvent();
					if (e.isStartElement()) {
						StartElement s = e.asStartElement();
						if ("message".equals(s.getName().getLocalPart())) {
							inMessage = true;
						}
					} else if (e.isEndElement()) {
						EndElement end = e.asEndElement();
						if ("message".equals(end.getName().getLocalPart())) {
							inMessage = false;
						}
					} else if (inMessage && e.isCharacters()) {
						message.append(e.asCharacters().getData());
					}
				}
				result = message.toString();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (Exception ignored) {
						log.error(ignored);
					}
				}

			}
			sr.close();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.services.TripleStoreService#fetch(edu.unc.lib.dl.services .PID)
	 */
	public PID verify(PID key) {
		PID result = null;
		String query = null;
		query = String.format("select $pid $state from <%1$s> where $pid <http://mulgara.org/mulgara#is> <%2$s>"
				+ " and $pid <%3$s> $state;", this.getResourceIndexModelUri(), key.getURI(),
				ContentModelHelper.FedoraProperty.state);
		List<PID> response = this.lookupDigitalObjects(query);
		if (!response.isEmpty()) {
			if (response.size() > 1) {
				throw new IllegalRepositoryStateException(
						"The repository is in an illegal state, multiple objects share a pid.", response);
			} else {
				result = response.get(0);
			}
		}
		return result;
	}

	public boolean isSourceData(PID pid, String datastreamID) {
		String query = String.format("select $pid $ds from <%1$s>" + " where $pid <%3$s> $ds"
				+ " and $pid <http://mulgara.org/mulgara#is> <%2$s>" + " and $ds <http://mulgara.org/mulgara#is> <%4$s>;",
				this.getResourceIndexModelUri(), pid.getURI(), ContentModelHelper.CDRProperty.sourceData, pid.getURI()
						+ "/" + datastreamID);
		List<List<String>> response = this.lookupStrings(query);
		if (!response.isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean allowIndexing(PID pid) {
		String query = String.format("select ?pid from <%1$s> where {" + "?pid <%2$s> 'yes' "
				+ "filter (?pid = <%3$s>) }", this.getResourceIndexModelUri(),
				ContentModelHelper.CDRProperty.allowIndexing.getURI(), pid.getURI());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List response = (List<Map>) ((Map) sendSPARQL(query).get("results")).get("bindings");

		if (!response.isEmpty()) {
			return true;
		}
		return false;
	}

	public List<PID> fetchChildContainers(PID key) {
		String query = String.format("select $child from <%1$s> where ( <%3$s> <%2$s> $child and $child <%4$s> <%5$s>);",
				this.getResourceIndexModelUri(), ContentModelHelper.Relationship.contains.getURI(), key.getURI(),
				ContentModelHelper.FedoraProperty.hasModel, ContentModelHelper.Model.CONTAINER);
		return this.lookupDigitalObjects(query);
	}

	public List<String> fetchAllCollectionPaths() {
		// TODO needs many CDRs one Fedora fix
		List<String> result = new ArrayList<String>(256);

		PID collections = this.fetchCollectionsObject();

		// construct path from contains relationships
		StringBuffer query = new StringBuffer();
		query.append("select $p $sp $c $sc from <%1$s>").append(" where walk(<%2$s> <%3$s> $p ")
				.append("and $p <%3$s> $c) and $p <%4$s> $sp ").append("and $c <%4$s> $sc ")
				.append("and $p <%5$s> <%6$s> ").append("and $c <%5$s> <%6$s>;");

		String q = String.format(query.toString(), this.getResourceIndexModelUri(), collections.getURI(),
				ContentModelHelper.Relationship.contains.getURI(), ContentModelHelper.CDRProperty.slug,
				ContentModelHelper.FedoraProperty.hasModel, ContentModelHelper.Model.CONTAINER);

		List<List<String>> response = this.lookupStrings(q);

		if (!response.isEmpty()) {
			Map<String, String> pathsMap = new HashMap<String, String>();
			for (List<String> solution : response) {

				StringBuffer sb = new StringBuffer(256);

				String parent = pathsMap.get(solution.get(0));
				if (parent == null) {
					pathsMap.put(solution.get(0), "/" + solution.get(1)); // add
					// parent
					// path
					// to
					// repository
					parent = solution.get(1);
				}

				sb.append(parent).append("/");
				sb.append(solution.get(3));
				pathsMap.put(solution.get(2), sb.toString()); // add full child
				// path to
				// repository
			}

			result.addAll(pathsMap.values());
			Collections.sort(result);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#lookupLabels(java.util.List)
	 */
	public String lookupLabel(PID pid) {
		String result = null;
		List<List<String>> label = this.queryResourceIndex(String.format(
				"select $label from <%1$s> where <%2$s> <%3$s> $label;", this.getResourceIndexModelUri(), pid.getURI(),
				ContentModelHelper.FedoraProperty.label.getURI()));
		if (!label.isEmpty() && !label.get(0).isEmpty()) {
			result = label.get(0).get(0);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#lookupSlug(edu.unc.lib.dl .fedora.PID)
	 */
	public String lookupSlug(PID pid) {
		String result = null;
		List<List<String>> res = this.queryResourceIndex(String.format(
				"select $slug from <%1$s> where <%2$s> <%3$s> $slug;", this.getResourceIndexModelUri(), pid.getURI(),
				ContentModelHelper.CDRProperty.slug.getURI()));
		if (!res.isEmpty() && !res.get(0).isEmpty()) {
			result = res.get(0).get(0);
		}
		return result;
	}

	@Override
	public String lookupSourceMimeType(PID pid) {
		String result = null;
		List<List<String>> res = this.queryResourceIndex(String.format(
				"select $mimeType from <%1$s> where <%2$s> <%3$s> $mimeType;", this.getResourceIndexModelUri(),
				pid.getURI(), ContentModelHelper.CDRProperty.hasSourceMimeType.getURI()));
		if (!res.isEmpty() && !res.get(0).isEmpty()) {
			result = res.get(0).get(0);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#fetchChildPathInfo(edu.unc .lib.dl.fedora.PID)
	 */
	@Override
	public List<PathInfo> fetchChildPathInfo(PID parent) {
		String parentPath = this.lookupRepositoryPath(parent);

		List<PathInfo> result = new ArrayList<PathInfo>();

		String query = String
				.format(
						"select $slug $child $label from <%1$s> where <%2$s> <%3$s> $child and $child <%4$s> $slug and $child <%5$s> $label;",
						this.getResourceIndexModelUri(), parent.getURI(), ContentModelHelper.Relationship.contains.getURI(),
						ContentModelHelper.CDRProperty.slug.getURI(), ContentModelHelper.FedoraProperty.label);

		List<List<String>> res = this.lookupStrings(query);
		for (List<String> list : res) {
			String slug = list.get(0);
			StringBuilder sb = new StringBuilder(parentPath);
			sb.append('/');
			sb.append(slug);
			PathInfo p = new PathInfo();
			p.setSlug(slug);
			p.setPid(new PID(list.get(1)));
			p.setPath(sb.toString());
			p.setLabel(list.get(2));
			result.add(p);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#lookupPermissions (edu.unc.lib.dl.fedora.PID)
	 */
	@Override
	public Map<String, List<String>> lookupPermissions(PID pid) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		// construct path from contains relationships
		StringBuffer query = new StringBuffer();
		query.append("select $permission $subject from <%1$s>")
				.append(" where <%2$s> $permission $subject")
				.append(" and (")
				.append("       $permission <mulgara:is> <%3$s>")
				// inheritPermissions
				.append("       or $permission <mulgara:is> <%4$s>")
				// metadataCreate
				.append("       or $permission <mulgara:is> <%5$s>").append("       or $permission <mulgara:is> <%6$s>")
				.append("       or $permission <mulgara:is> <%7$s>")
				.append("       or $permission <mulgara:is> <%8$s>")
				// originalCreate
				.append("       or $permission <mulgara:is> <%9$s>").append("       or $permission <mulgara:is> <%10$s>")
				.append("       or $permission <mulgara:is> <%11$s>").append("       or $permission <mulgara:is> <%12$s>")
				// derivativeCreate
				.append("       or $permission <mulgara:is> <%13$s>").append("       or $permission <mulgara:is> <%14$s>")
				.append("       or $permission <mulgara:is> <%15$s> )").append(";");
		String q = String.format(query.toString(), this.getResourceIndexModelUri(), pid.getURI(),
				ContentModelHelper.CDRProperty.inheritPermissions.getURI(),
				ContentModelHelper.CDRProperty.permitMetadataCreate.getURI(),
				ContentModelHelper.CDRProperty.permitMetadataRead.getURI(),
				ContentModelHelper.CDRProperty.permitMetadataUpdate.getURI(),
				ContentModelHelper.CDRProperty.permitMetadataDelete.getURI(),
				ContentModelHelper.CDRProperty.permitOriginalsCreate.getURI(),
				ContentModelHelper.CDRProperty.permitOriginalsRead.getURI(),
				ContentModelHelper.CDRProperty.permitOriginalsUpdate.getURI(),
				ContentModelHelper.CDRProperty.permitOriginalsDelete.getURI(),
				ContentModelHelper.CDRProperty.permitDerivativesCreate.getURI(),
				ContentModelHelper.CDRProperty.permitDerivativesRead.getURI(),
				ContentModelHelper.CDRProperty.permitDerivativesUpdate.getURI(),
				ContentModelHelper.CDRProperty.permitDerivativesDelete.getURI());

		List<List<String>> response = this.lookupStrings(q);
		if (!response.isEmpty()) {
			for (List<String> solution : response) {
				String permKey = solution.get(0);
				String subjectValue = solution.get(1);
				if (!result.containsKey(permKey)) {
					result.put(permKey, new ArrayList<String>());
				}
				result.get(permKey).add(subjectValue);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#fetchAllTriples(PID pid) (edu.unc.lib.dl.fedora.PID)
	 */
	@Override
	public Map<String, List<String>> fetchAllTriples(PID pid) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		// construct path from contains relationships
		StringBuffer query = new StringBuffer();
		query.append("select $permission $subject from <%1$s>").append(" where <%2$s> $permission $subject").append(";");
		String q = String.format(query.toString(), this.getResourceIndexModelUri(), pid.getURI());

		List<List<String>> response = this.lookupStrings(q);
		if (!response.isEmpty()) {
			for (List<String> solution : response) {
				String permKey = solution.get(0);
				String subjectValue = solution.get(1);
				if (!result.containsKey(permKey)) {
					result.put(permKey, new ArrayList<String>());
				}
				result.get(permKey).add(subjectValue);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#lookupSinglePermission (edu.unc.lib.dl.fedora.PID, String)
	 */
	@Override
	public Map<String, List<String>> lookupSinglePermission(PID pid, String permission) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		// construct path from contains relationships
		StringBuffer query = new StringBuffer();
		query.append("select $permission $subject from <%1$s>").append(" where <%2$s> $permission $subject")
				.append(" and (").append("       $permission <mulgara:is> <%3$s>") // inheritPermissions
				.append("       or $permission <mulgara:is> <%4$s> )").append(";");
		String q = String.format(query.toString(), this.getResourceIndexModelUri(), pid.getURI(),
				ContentModelHelper.CDRProperty.inheritPermissions.getURI(), permission);

		List<List<String>> response = this.lookupStrings(q);
		if (!response.isEmpty()) {
			for (List<String> solution : response) {
				String permKey = solution.get(0);
				String subjectValue = solution.get(1);
				if (!result.containsKey(permKey)) {
					result.put(permKey, new ArrayList<String>());
				}
				result.get(permKey).add(subjectValue);
			}
		}
		return result;
	}

	/*
	 * Used for access control
	 */
	public List<PID> lookupRepositoryAncestorPids(PID key) {

		// TODO needs many CDRs one Fedora fix
		List<PID> result = new ArrayList<PID>();

		// construct path from contains relationships
		StringBuffer query = new StringBuffer();
		query.append("select $p <%2$s> $c from <%1$s>").append(" where walk( $p <%2$s> <%3$s> and $p <%2$s> $c);");
		String q = String.format(query.toString(), this.getResourceIndexModelUri(),
				ContentModelHelper.Relationship.contains.getURI(), key.getURI());

		List<List<String>> response = this.lookupStrings(q);
		if (!response.isEmpty()) {
			// build a map of key:parent to val:child
			Map<String, String> parent2child = new HashMap<String, String>();
			for (List<String> solution : response) {
				parent2child.put(solution.get(0), solution.get(2));
			}

			// follow REPOSITORY through all children, building path in order
			for (String step = ContentModelHelper.Administrative_PID.REPOSITORY.getPID().getURI(); parent2child
					.containsKey(step); step = parent2child.get(step)) {
				result.add(new PID(step));
			}
		}
		return result;
	}

	public String getSparqlEndpointURL() {
		return sparqlEndpointURL;
	}

	public void setSparqlEndpointURL(String sparqlEndpointURL) {
		this.sparqlEndpointURL = sparqlEndpointURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#getSourceData(edu.unc.lib.dl.fedora.PID)
	 */
	@Override
	public List<String> getSourceData(PID pid) {
		List<String> result = new ArrayList<String>();
		String query = String.format("select $ds from <%1$s>" + " where $pid <%3$s> $ds"
				+ " and $pid <http://mulgara.org/mulgara#is> <%2$s>;", this.getResourceIndexModelUri(), pid.getURI(),
				ContentModelHelper.CDRProperty.sourceData.getURI());
		List<List<String>> response = this.lookupStrings(query);
		if (!response.isEmpty()) {
			for (List<String> entry : response) {
				result.add(entry.get(0));
			}
			log.debug("found " + result.size() + " source datastreams");
		}
		return result;
	}

	@Override
	public List<String> listDisseminators(PID pid){
		List<String> result = new ArrayList<String>();
		String query = String.format("select $ds from <%1$s>" + " where $pid <%3$s> $ds"
				+ " and $pid <http://mulgara.org/mulgara#is> <%2$s>;", this.getResourceIndexModelUri(), pid.getURI(),
				ContentModelHelper.FedoraProperty.disseminates.getURI());
		List<List<String>> response = this.lookupStrings(query);
		if (!response.isEmpty()) {
			for (List<String> entry : response) {
				result.add(entry.get(0));
			}
		}
		return result;
	}
	
	@Override
	public boolean isOrphaned(PID key) {
		PID collections = fetchCollectionsObject();
		StringBuffer query = new StringBuffer();
		query.append("select $p from <%1$s>").append(" where walk( $p <%2$s> <%3$s> and $p <%2$s> $c) ");
		query.append(" and <%4$s> <%2$s> $c;");
		String q = String.format(query.toString(), this.getResourceIndexModelUri(),
				ContentModelHelper.Relationship.contains.getURI(), key.getURI(), collections.getURI());

		List<List<String>> response = this.lookupStrings(q);
		return response.isEmpty() || response.get(0).isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#fetchParentCollection(edu.unc.lib.dl.fedora.PID)
	 * 
	 * select $p from <#ri> where walk( $p <http://cdr.unc.edu/definitions/1.0/base-model.xml#contains>
	 * <info:fedora/uuid:4e349cbf-dda4-4d14-94eb-c2c27a59c06a> and $p
	 * <http://cdr.unc.edu/definitions/1.0/base-model.xml#contains> $c) and $p <fedora-model:hasModel>
	 * <info:fedora/cdr-model:Collection>;
	 */
	@Override
	public PID fetchParentCollection(PID key) {
		PID result = null;
		StringBuffer query = new StringBuffer();
		query.append("select $p from <%1$s>").append(" where walk( $p <%2$s> <%3$s> and $p <%2$s> $c) ");
		query.append(" and $p <%4$s> <%5$s>;");
		String q = String.format(query.toString(), this.getResourceIndexModelUri(),
				ContentModelHelper.Relationship.contains.getURI(), key.getURI(),
				ContentModelHelper.FedoraProperty.hasModel.getURI(), ContentModelHelper.Model.COLLECTION.getURI());

		List<List<String>> response = this.lookupStrings(q);
		if (!response.isEmpty() && !response.get(0).isEmpty()) {
			String pid = response.get(0).get(0);
			result = new PID(pid);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#getSurrogateData(edu.unc.lib.dl.fedora.PID)
	 */
	@Override
	public List<String> getSurrogateData(PID pid) {
		List<String> result = new ArrayList<String>();
		String query = String.format("select $ds from <%1$s>" + " where $pid <%3$s> $ds"
				+ " and ($pid <http://mulgara.org/mulgara#is> <%2$s> or <%2$s> <%4$s> $pid);",
				this.getResourceIndexModelUri(), pid.getURI(), ContentModelHelper.CDRProperty.sourceData.getURI(),
				ContentModelHelper.CDRProperty.hasSurrogate.getURI());
		List<List<String>> response = this.lookupStrings(query);
		if (!response.isEmpty()) {
			for (List<String> entry : response) {
				result.add(entry.get(0));
			}
		}
		log.debug("found " + result.size() + " source/surrogate datastreams for " + pid.getPid());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.dl.util.TripleStoreQueryService#fetchPIDsSurrogateFor(edu.unc.lib.dl.fedora.PID)
	 */
	@Override
	public List<PID> fetchPIDsSurrogateFor(PID pid) {
		List<PID> result = new ArrayList<PID>();
		String query = String.format("select $pid from <%1$s> where $pid <%3$s> <%2$s>;",
				this.getResourceIndexModelUri(), pid.getURI(), ContentModelHelper.CDRProperty.hasSurrogate.getURI());
		List<List<String>> response = this.lookupStrings(query);
		if (!response.isEmpty()) {
			for (List<String> entry : response) {
				result.add(new PID(entry.get(0)));
			}
		}
		log.debug("found " + result.size() + " pid that have this one as a surrogate: " + pid.getPid());
		return result;
	}
}
