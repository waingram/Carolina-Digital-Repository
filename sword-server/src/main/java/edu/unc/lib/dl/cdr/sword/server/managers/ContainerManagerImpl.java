package edu.unc.lib.dl.cdr.sword.server.managers;

import java.util.List;
import java.util.Map;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.log4j.Logger;
import org.swordapp.server.AuthCredentials;
import org.swordapp.server.ContainerManager;
import org.swordapp.server.Deposit;
import org.swordapp.server.DepositReceipt;
import org.swordapp.server.SwordAuthException;
import org.swordapp.server.SwordConfiguration;
import org.swordapp.server.SwordError;
import org.swordapp.server.SwordServerException;

import edu.unc.lib.dl.agents.Agent;
import edu.unc.lib.dl.agents.PersonAgent;
import edu.unc.lib.dl.cdr.sword.server.SwordConfigurationImpl;
import edu.unc.lib.dl.fedora.AccessControlRole;
import edu.unc.lib.dl.fedora.FedoraException;
import edu.unc.lib.dl.fedora.ManagementClient;
import edu.unc.lib.dl.fedora.NotFoundException;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.ingest.IngestException;
import edu.unc.lib.dl.services.DigitalObjectManager;
import edu.unc.lib.dl.update.AtomPubMetadataUIP;
import edu.unc.lib.dl.update.UIPException;
import edu.unc.lib.dl.update.UIPProcessor;
import edu.unc.lib.dl.update.UpdateException;
import edu.unc.lib.dl.update.UpdateOperation;
import edu.unc.lib.dl.util.ContentModelHelper;

public class ContainerManagerImpl extends AbstractFedoraManager implements ContainerManager {

	private static Logger log = Logger.getLogger(ContainerManagerImpl.class);

	private DigitalObjectManager digitalObjectManager;
	private UIPProcessor uipProcessor;
	private ManagementClient managementClient;

	private DepositReceipt updateMetadata(String editIRI, Deposit deposit, AuthCredentials auth,
			SwordConfiguration config, UpdateOperation operation) throws SwordError, SwordServerException,
			SwordAuthException {
		PID targetPID = extractPID(editIRI, SwordConfigurationImpl.EDIT_PATH + "/");

		PersonAgent depositor = agentFactory.findPersonByOnyen(auth.getUsername(), false);
		if (depositor == null) {
			throw new SwordAuthException("Unable to find a user matching the submitted username credentials, "
					+ auth.getUsername());
		}

		SwordConfigurationImpl configImpl = (SwordConfigurationImpl) config;
		// Get the users group
		List<String> groupList = this.getGroups(auth, configImpl);

		if (!accessControlUtils.hasAccess(targetPID, groupList, AccessControlRole.curator.getUri().toString())) {
			throw new SwordAuthException("Insufficient privileges to update metadata for " + targetPID.getPid());
		}

		AtomPubMetadataUIP uip;
		try {
			uip = new AtomPubMetadataUIP(targetPID, depositor, operation, deposit.getSwordEntry().getEntry());
		} catch (UIPException e) {
			log.warn("An exception occurred while attempting to create metadata UIP for " + targetPID.getPid(), e);
			throw new SwordError("An exception occurred while attempting to create metadata UIP for " + editIRI + "\n"
					+ e.getMessage());
		}

		try {
			uipProcessor.process(uip);
		} catch (UpdateException e) {
			throw new SwordServerException(
					"An exception occurred while attempting to update object " + targetPID.getPid(), e);
		} catch (UIPException e) {
			log.warn("Failed to process UIP for " + targetPID.getPid(), e);
			throw new SwordError("A problem occurred while attempting to perform the requested update operation on "
					+ editIRI + ".\n" + e.getMessage());
		}

		DepositReceipt receipt = new DepositReceipt();
		receipt.setLocation(new IRI(editIRI));
		receipt.setEmpty(true);

		// Update the objects in progress status
		this.setInProgress(targetPID, deposit, receipt);
		
		return receipt;
	}

	@Override
	public DepositReceipt replaceMetadata(String editIRI, Deposit deposit, AuthCredentials auth,
			SwordConfiguration config) throws SwordError, SwordServerException, SwordAuthException {
		return updateMetadata(editIRI, deposit, auth, config, UpdateOperation.REPLACE);
	}

	@Override
	public DepositReceipt replaceMetadataAndMediaResource(String editIRI, Deposit deposit, AuthCredentials auth,
			SwordConfiguration config) throws SwordError, SwordServerException, SwordAuthException {
		throw new SwordServerException("Method not yet supported");
	}

	@Override
	public DepositReceipt addMetadataAndResources(String editIRI, Deposit deposit, AuthCredentials auth,
			SwordConfiguration config) throws SwordError, SwordServerException, SwordAuthException {
		throw new SwordServerException("Method not yet supported");
	}

	@Override
	public DepositReceipt addMetadata(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
			throws SwordError, SwordServerException, SwordAuthException {
		return updateMetadata(editIRI, deposit, auth, config, UpdateOperation.ADD);
	}

	@Override
	public DepositReceipt addResources(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
			throws SwordError, SwordServerException, SwordAuthException {
		// This happens in the MediaResourceManager. This method isn't referenced
		return null;
	}

	/**
	 * Deletes the object identified in editIRI if the submitted auth credentials have delete rights to the targeted
	 * object.
	 */
	@Override
	public void deleteContainer(String editIRI, AuthCredentials auth, SwordConfiguration config) throws SwordError,
			SwordServerException, SwordAuthException {

		Agent user = agentFactory.findPersonByOnyen(auth.getUsername(), false);
		if (user == null) {
			throw new SwordAuthException("Unable to find a user matching the submitted username credentials, "
					+ auth.getUsername());
		}
		// Ignoring on-behalf-of for the moment

		SwordConfigurationImpl configImpl = (SwordConfigurationImpl) config;

		PID targetPID = extractPID(editIRI, SwordConfigurationImpl.EDIT_PATH + "/");

		List<String> groupList = this.getGroups(auth, configImpl);

		if (!accessControlUtils.hasAccess(targetPID, groupList, AccessControlRole.admin.getUri().toString())) {
			throw new SwordAuthException("Insufficient privileges to delete object " + targetPID.getPid());
		}

		try {
			this.digitalObjectManager.delete(targetPID, user, "Deleted by " + user.getName());
		} catch (NotFoundException e) {
			throw new SwordError("Unable to delete the object " + targetPID.getPid()
					+ ".  The object was not found in the repository.");
		} catch (IngestException e) {
			throw new SwordServerException("Failed to delete object " + targetPID.getPid(), e);
		}
	}

	/**
	 * Empty body request with headers. Allows for declaring an item to no longer be in-progress
	 */
	@Override
	public DepositReceipt useHeaders(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
			throws SwordError, SwordServerException, SwordAuthException {

		PID targetPID = extractPID(editIRI, SwordConfigurationImpl.EDIT_PATH + "/");

		DepositReceipt receipt = new DepositReceipt();
		receipt.setLocation(new IRI(editIRI));

		SwordConfigurationImpl configImpl = (SwordConfigurationImpl) config;
		List<String> groupList = this.getGroups(auth, configImpl);

		if (!accessControlUtils.hasAccess(targetPID, groupList, AccessControlRole.curator.getUri().toString())) {
			throw new SwordAuthException("Insufficient privileges to update object headers " + targetPID.getPid());
		}

		this.setInProgress(targetPID, deposit, receipt);

		return receipt;
	}

	/**
	 * Determines if the request is a statement request instead of a deposit receipt request. Does not return a
	 * statement.
	 */
	@Override
	public boolean isStatementRequest(String editIRI, Map<String, String> accept, AuthCredentials auth,
			SwordConfiguration config) throws SwordError, SwordServerException, SwordAuthException {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * After-the-fact deposit receipt retrieval method
	 */
	@Override
	public DepositReceipt getEntry(String editIRI, Map<String, String> accept, AuthCredentials auth,
			SwordConfiguration config) throws SwordServerException, SwordError, SwordAuthException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void setInProgress(PID targetPID, Deposit deposit, DepositReceipt receipt) throws SwordServerException {
		String state = tripleStoreQueryService.fetchState(targetPID);
		if (deposit.isInProgress() != Boolean.parseBoolean(state)) {
			try {
				log.debug("Updating active state of in-progress item");
				managementClient.addLiteralStatement(targetPID, ContentModelHelper.FedoraProperty.Active.toString(),
						"Active", null);
				receipt.setVerboseDescription(targetPID.getPid() + " is " + ((deposit.isInProgress())? "": "not") + " in-progress");
			} catch (FedoraException e) {
				throw new SwordServerException("Failed to update active state for " + targetPID.getPid());
			}
		}
	}

	public void setDigitalObjectManager(DigitalObjectManager digitalObjectManager) {
		this.digitalObjectManager = digitalObjectManager;
	}

	public void setUipProcessor(UIPProcessor uipProcessor) {
		this.uipProcessor = uipProcessor;
	}

	public void setManagementClient(ManagementClient managementClient) {
		this.managementClient = managementClient;
	}
}
