package edu.unc.lib.dl.cdr.sword.server.util;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.parser.Parser;
import org.apache.log4j.Logger;
import org.swordapp.server.DepositReceipt;
import org.swordapp.server.OriginalDeposit;

import edu.unc.lib.dl.cdr.sword.server.SwordConfigurationImpl;
import edu.unc.lib.dl.fedora.AccessClient;
import edu.unc.lib.dl.fedora.FedoraException;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.fedora.types.MIMETypedStream;
import edu.unc.lib.dl.services.IngestResult;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.util.DateTimeUtil;
import edu.unc.lib.dl.util.TripleStoreQueryService;
import edu.unc.lib.dl.util.ContentModelHelper.Datastream;

public class DepositReportingUtil {
	private static Logger log = Logger.getLogger(DepositReportingUtil.class);

	private TripleStoreQueryService tripleStoreQueryService;
	private AccessClient accessClient;

	public static class OriginalDepositPair {
		public String originalDepositURI;
		public String mimetype;

		public OriginalDepositPair(String originalDepositURI, String mimetype) {
			this.originalDepositURI = originalDepositURI;
			this.mimetype = mimetype;
		}
	}

	public OriginalDepositPair getOriginalDeposit(PID pid, SwordConfigurationImpl config) {
		List<String> originalDeposits = tripleStoreQueryService.fetchBySubjectAndPredicate(pid,
				ContentModelHelper.Relationship.originalDeposit.toString());

		if (originalDeposits.size() == 0)
			return null;

		PID depositPID = new PID(originalDeposits.get(0));

		Map<String, List<String>> depositTriples = tripleStoreQueryService.fetchAllTriples(depositPID);

		// Get originalDeposit URI
		List<String> values = depositTriples.get(ContentModelHelper.FedoraProperty.disseminates.toString());
		if (values != null) {
			for (String dissemination : values) {
				if (dissemination.endsWith("/" + Datastream.DATA_MANIFEST.getName())) {
					return new OriginalDepositPair(config.getSwordPath() + SwordConfigurationImpl.EDIT_MEDIA_PATH + "/"
							+ depositPID.getPid() + "/" + Datastream.DATA_MANIFEST.getName(), "text/xml");
				}
			}
		}

		// Use the objects datafile as its original deposit URI if there was no manifest
		return new OriginalDepositPair(config.getSwordPath() + SwordConfigurationImpl.EDIT_MEDIA_PATH + "/"
				+ pid.getPid() + "/" + Datastream.DATA_FILE.getName(), tripleStoreQueryService.lookupSourceMimeType(pid));
	}

	public List<OriginalDeposit> getOriginalDeposits(PID pid, SwordConfigurationImpl config) {
		List<OriginalDeposit> results = new ArrayList<OriginalDeposit>();

		List<String> originalDeposits = tripleStoreQueryService.fetchBySubjectAndPredicate(pid,
				ContentModelHelper.Relationship.originalDeposit.toString());

		Date depositedOn = null;
		String depositedBy = null;
		String depositedOnBehalfOf = null;
		String originalDepositURI = null;
		String mimetype = null;
		List<String> packageTypes = null;
		List<String> values = null;
		for (String originalDeposit : originalDeposits) {
			PID depositPID = new PID(originalDeposit);
			Map<String, List<String>> depositTriples = tripleStoreQueryService.fetchAllTriples(depositPID);

			// Get originalDeposit URI
			values = depositTriples.get(ContentModelHelper.FedoraProperty.disseminates.toString());
			if (values != null) {
				for (String dissemination : values) {
					if (dissemination.endsWith("/" + Datastream.DATA_MANIFEST.getName())) {
						originalDepositURI = config.getSwordPath() + SwordConfigurationImpl.EDIT_MEDIA_PATH + "/"
								+ depositPID.getPid() + "/" + Datastream.DATA_MANIFEST.getName();
						mimetype = "text/xml";
						break;
					}
				}
			}

			// Use the objects datafile as its original deposit URI if there was no manifest
			if (originalDepositURI == null) {
				originalDepositURI = config.getSwordPath() + SwordConfigurationImpl.EDIT_MEDIA_PATH + "/" + pid.getPid()
						+ "/" + Datastream.DATA_FILE.getName();
				mimetype = tripleStoreQueryService.lookupSourceMimeType(pid);
			}

			// Get depositedOn value
			values = depositTriples.get(ContentModelHelper.FedoraProperty.createdDate.toString());
			if (values != null && values.size() > 0) {
				String depositedOnString = depositTriples.get(ContentModelHelper.FedoraProperty.createdDate.toString())
						.get(0);
				try {
					depositedOn = DateTimeUtil.parseUTCToDate(depositedOnString);
				} catch (ParseException e) {
					log.error("Could not parse deposited on", e);
				}
			}

			// Get package types
			packageTypes = depositTriples.get(ContentModelHelper.CDRProperty.depositPackageType.toString());
			values = depositTriples.get(ContentModelHelper.CDRProperty.depositPackageSubType.toString());
			if (values != null && values.size() > 0) {
				if (packageTypes == null)
					packageTypes = new ArrayList<String>();
				packageTypes.addAll(values);
			}

			// Get deposited by
			values = depositTriples.get(ContentModelHelper.Relationship.depositedBy.toString());
			if (values != null && values.size() > 0) {
				PID depositedByPID = new PID(values.get(0));
				depositedBy = tripleStoreQueryService.fetchFirstBySubjectAndPredicate(depositedByPID,
						ContentModelHelper.CDRProperty.onyen.toString());
			}

			// Get on behalf of
			values = depositTriples.get(ContentModelHelper.CDRProperty.depositedOnBehalfOf.toString());
			if (values != null && values.size() > 0) {
				depositedOnBehalfOf = values.get(0);
			}

			OriginalDeposit deposit = new OriginalDeposit(originalDepositURI, packageTypes, depositedOn, depositedBy,
					depositedOnBehalfOf);
			deposit.setMediaType(mimetype);
			results.add(deposit);
		}
		return results;
	}
	
	/**
	 * Generates a DepositReceipt object for the specified PID.  This represents state of the target, how it has
	 * been unpacked, as well as paths to its individual components and deposit manifest.
	 * @param targetPID
	 * @param config
	 * @return
	 */
	public DepositReceipt retrieveDepositReceipt(PID targetPID, SwordConfigurationImpl config) {
		DepositReceipt receipt = new DepositReceipt();
		return retrieveDepositReceipt(receipt, targetPID, config);
	}
	
	/**
	 * Generates a DepositReceipt object for the derived resources from the provided ingestResult.  If there are
	 * multiple derived resources, then the first one is used as a representative for the rest of the set for IRIs
	 * which cannot be repeated.
	 * @param ingestResult
	 * @param config
	 * @return
	 */
	public DepositReceipt retrieveDepositReceipt(IngestResult ingestResult, SwordConfigurationImpl config) {
		DepositReceipt receipt = new DepositReceipt();
		
		// grab a representative pid from the top level result derived pids, since a lot of the IRI's can only be set once
		PID representativePID = null;

		for (PID resultPID: ingestResult.derivedPIDs){
			if (representativePID == null){
				representativePID = resultPID;
			} else {
				//Skip the edit media iri f or the representative PID since it'll be added later
				receipt.addEditMediaIRI(new IRI(config.getSwordPath() + SwordConfigurationImpl.EDIT_MEDIA_PATH + "/" + resultPID.getPid()));
			}
		}
		return retrieveDepositReceipt(receipt, representativePID, config);
	}
	
	/**
	 * Adds receipt information to the DepositReceipt object for the specified PID. This represents state of the target,
	 * how it has been unpacked, as well as paths to its individual components and deposit manifest.
	 * 
	 * @param receipt
	 * @param targetPID
	 * @param config
	 * @return
	 */
	public DepositReceipt retrieveDepositReceipt(DepositReceipt receipt, PID targetPID, SwordConfigurationImpl config) {
		IRI editIRI = new IRI(config.getSwordPath() + SwordConfigurationImpl.EDIT_PATH + "/" + targetPID.getPid());
		receipt.setEditIRI(editIRI);
		IRI swordEditIRI = new IRI(config.getSwordPath() + SwordConfigurationImpl.EDIT_PATH + "/" + targetPID.getPid());
		receipt.setSwordEditIRI(swordEditIRI);
		receipt.addEditMediaIRI(new IRI(config.getSwordPath() + SwordConfigurationImpl.EDIT_MEDIA_PATH + "/" + targetPID.getPid()));
		
		//Add in original deposit
		OriginalDepositPair originalDeposit = getOriginalDeposit(targetPID, config);
		if (originalDeposit != null)
			receipt.setOriginalDeposit(originalDeposit.originalDepositURI, originalDeposit.mimetype);
		
		// Add in derived resources representing all of the datastreams on this object
		Map<String,String> disseminators = tripleStoreQueryService.fetchDisseminatorMimetypes(targetPID);
		for (Map.Entry<String,String> disseminator: disseminators.entrySet()){
			PID disseminatorPID = new PID(disseminator.getKey());
			receipt.addDerivedResource(config.getSwordPath() + SwordConfigurationImpl.EDIT_MEDIA_PATH + "/" + disseminatorPID.getPid(), disseminator.getValue());
		}
		
		receipt.setSplashUri(config.getBasePath() + "record?id=" + targetPID.getPid());
		
		receipt.setStatementURI("application/atom+xml;type=feed", config.getSwordPath() + SwordConfigurationImpl.STATE_PATH + "/" + targetPID.getPid());
		
		try {
			MIMETypedStream metadataStream = accessClient.getDatastreamDissemination(targetPID, ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName(), null);
			if (metadataStream == null){
				//If there is a DC stream instead, then add all its children
				metadataStream = accessClient.getDatastreamDissemination(targetPID, ContentModelHelper.Datastream.DC.getName(), null);
				if (metadataStream != null) {
					Abdera abdera = new Abdera();
					Parser parser = abdera.getParser();
					Document<Element> entryDoc = parser.parse(new ByteArrayInputStream(metadataStream.getStream()));
					for (Element child: entryDoc.getRoot().getElements()){
						receipt.addDublinCore(child.getQName().getLocalPart(), child.getText());
					}
				}
			} else {
				// Build MODS as an Abdera entry and add it to the receipt entry.
				Abdera abdera = new Abdera();
				Parser parser = abdera.getParser();
				Document<Element> entryDoc = parser.parse(new ByteArrayInputStream(metadataStream.getStream()));
				
				receipt.getWrappedEntry().addExtension(entryDoc.getRoot());
			}
		} catch (FedoraException e) {
			log.error("Error retriving MD_DESCRIPTIVE for object " + targetPID.getPid(), e);
		}
		
		return receipt;
	}
	

	

	public void setTripleStoreQueryService(TripleStoreQueryService tripleStoreQueryService) {
		this.tripleStoreQueryService = tripleStoreQueryService;
	}

	public void setAccessClient(AccessClient accessClient) {
		this.accessClient = accessClient;
	}
}
