package edu.unc.lib.dl.cdr.sword.server.managers;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.swordapp.server.AuthCredentials;
import org.swordapp.server.Deposit;
import org.swordapp.server.DepositReceipt;
import org.swordapp.server.MediaResource;
import org.swordapp.server.MediaResourceManager;
import org.swordapp.server.SwordAuthException;
import org.swordapp.server.SwordConfiguration;
import org.swordapp.server.SwordError;
import org.swordapp.server.SwordServerException;

import edu.unc.lib.dl.agents.PersonAgent;
import edu.unc.lib.dl.cdr.sword.server.MethodAwareInputStream;
import edu.unc.lib.dl.cdr.sword.server.SwordConfigurationImpl;
import edu.unc.lib.dl.fedora.AccessControlRole;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.util.ContentModelHelper.Datastream;

public class MediaResourceManagerImpl extends AbstractFedoraManager implements MediaResourceManager {
	private static Logger log = Logger.getLogger(MediaResourceManagerImpl.class);

	private String fedoraPath;
	
	@Override
	public MediaResource getMediaResourceRepresentation(String uri, Map<String, String> accept, AuthCredentials auth,
			SwordConfiguration config) throws SwordError, SwordServerException, SwordAuthException {
		
		log.debug("Retrieving media resource representation for " + uri);
		
		PID targetPID = extractPID(uri, SwordConfigurationImpl.EDIT_MEDIA_PATH + "/");
		PID basePID = null;
		String pidString = targetPID.getPid();
		String datastreamString = null;
		int index = pidString.indexOf("/");
		if (index != -1 && index < pidString.length() - 1){
			datastreamString = pidString.substring(index + 1);
			basePID = new PID(pidString.substring(0, index));
		} else {
			basePID = targetPID;
		}
		
		SwordConfigurationImpl configImpl = (SwordConfigurationImpl)config;
		
		Datastream datastream = Datastream.getDatastream(datastreamString);
		
		PersonAgent user = agentFactory.findPersonByOnyen(auth.getUsername(), false);
		if (user == null){
			log.debug("Unable to find a user matching the submitted username credentials, " + auth.getUsername());
			throw new SwordAuthException("Unable to find a user matching the submitted username credentials, " + auth.getUsername());
		}
		
		//Get the users group
		List<String> groupList = this.getGroups(auth, configImpl);
		
		if (!accessControlUtils.hasAccess(basePID, groupList, AccessControlRole.patron.getUri().toString())){
			log.debug("Insufficient privileges to get media resource for " + targetPID.getPid());
			throw new SwordAuthException("Insufficient privileges to get media resource for " + targetPID.getPid());
		}
		
		if (datastream == null)
			throw new SwordServerException("Media representations other than those of datastreams are not currently supported");
		
		HttpClient client = new HttpClient();

		UsernamePasswordCredentials cred = new UsernamePasswordCredentials(accessClient.getUsername(),
				accessClient.getPassword());
		client.getState().setCredentials(new AuthScope(null, 443), cred);
		client.getState().setCredentials(new AuthScope(null, 80), cred);
		
		GetMethod method = new GetMethod(fedoraPath + "/objects/" + basePID.getPid() + "/datastreams/" + datastream + "/content");
		
		InputStream inputStream = null;
		String mimeType = null;
		String lastModified = null;
		
		try {
			method.setDoAuthentication(true);
			client.executeMethod(method);
			if (method.getStatusCode() == HttpStatus.SC_OK) {
				Map<String, List<String>> dsTriples = tripleStoreQueryService.fetchAllTriples(targetPID);
				List<String> dsRowList = dsTriples.get(ContentModelHelper.FedoraProperty.mimeType.getURI().toString());
				if (dsRowList.size() > 0)
					mimeType = dsRowList.get(0);
				dsRowList = dsTriples.get(ContentModelHelper.FedoraProperty.lastModifiedDate.getURI().toString());
				if (dsRowList.size() > 0)
					lastModified = dsRowList.get(0);
				inputStream = new MethodAwareInputStream(method);
			} else if (method.getStatusCode() == 500){
				throw new SwordServerException("Failed to retrieve " + targetPID.getPid() + ": " + method.getStatusLine().toString());
			} else if (method.getStatusCode() == HttpStatus.SC_NOT_FOUND){
				throw new SwordError("Object " + targetPID.getPid() + " could not be found.");
			}
		} catch (HttpException e) {
			throw new SwordServerException("An exception occurred while attempting to retrieve " + targetPID.getPid(), e);
		} catch (IOException e) {
			throw new SwordServerException("An exception occurred while attempting to retrieve " + targetPID.getPid(), e);
		}
		
		MediaResource resource = new MediaResource(inputStream, mimeType, null, true);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date lastModifiedDate;
		try {
			lastModifiedDate = formatter.parse(lastModified);
			resource.setLastModified(lastModifiedDate);
		} catch (ParseException e) {
			log.error("Unable to set last modified date for " + uri, e);
		}
		
		return resource;
	}

	@Override
	public DepositReceipt replaceMediaResource(String uri, Deposit deposit, AuthCredentials auth,
			SwordConfiguration config) throws SwordError, SwordServerException, SwordAuthException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteMediaResource(String uri, AuthCredentials auth, SwordConfiguration config) throws SwordError,
			SwordServerException, SwordAuthException {
		// TODO Auto-generated method stub

	}

	@Override
	public DepositReceipt addResource(String uri, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
			throws SwordError, SwordServerException, SwordAuthException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFedoraPath() {
		return fedoraPath;
	}

	public void setFedoraPath(String fedoraPath) {
		this.fedoraPath = fedoraPath;
	}

}
