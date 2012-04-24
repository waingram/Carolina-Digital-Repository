package edu.unc.lib.dl.cdr.sword.server.util;

import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.junit.Assert;
import org.junit.Test;
import org.swordapp.server.AuthCredentials;
import org.swordapp.server.DepositReceipt;
import org.swordapp.server.UriRegistry;

import edu.unc.lib.dl.cdr.sword.server.SwordConfigurationImpl;
import edu.unc.lib.dl.cdr.sword.server.managers.ContainerManagerImpl;
import edu.unc.lib.dl.cdr.sword.server.util.DepositReportingUtil;
import edu.unc.lib.dl.fedora.AccessClient;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.fedora.types.MIMETypedStream;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.util.TripleStoreQueryService;

import static org.mockito.Mockito.*;

public class DepositReportingUtilTest extends Assert {

	private SwordConfigurationImpl config;
	
	public DepositReportingUtilTest(){
		config = new SwordConfigurationImpl();
		config.setBasePath("http://localhost");
		config.setSwordPath("http://localhost/sword");
	}
	
	@Test
	public void retrieveDepositReceiptWithMODSTest() throws Exception {
		PID pid = new PID("uuid:test");
		
		TripleStoreQueryService tripleStoreQueryService = mock(TripleStoreQueryService.class);
		
		Map<String,String> disseminations = new HashMap<String,String>();
		disseminations.put(pid.getURI() + "/" + ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName(), "text/xml");
		disseminations.put(pid.getURI() + "/" + ContentModelHelper.Datastream.DATA_FILE.getName(), "image/jpg");
		
		when(tripleStoreQueryService.fetchDisseminatorMimetypes(any(PID.class))).thenReturn(disseminations);
		
		RandomAccessFile modsFile = new RandomAccessFile("src/test/resources/modsDocument.xml", "r");
		byte[] modsBytes = new byte[(int)modsFile.length()];
		modsFile.read(modsBytes);
		MIMETypedStream mimeStream = new MIMETypedStream();
		mimeStream.setStream(modsBytes);
		
		AccessClient accessClient = mock(AccessClient.class);
		when(accessClient.getDatastreamDissemination(any(PID.class), eq(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()), anyString())).thenReturn(mimeStream);
		
		DepositReportingUtil depositReportingUtil = new DepositReportingUtil();
		depositReportingUtil.setAccessClient(accessClient);
		depositReportingUtil.setTripleStoreQueryService(tripleStoreQueryService);
		
		DepositReceipt receipt = depositReportingUtil.retrieveDepositReceipt(pid, config);
		
		Entry receiptEntry = receipt.getAbderaEntry();
		
		assertEquals("There must be one mods extension", 1, receiptEntry.getExtensions().size());
		assertEquals("mods", receiptEntry.getExtensions().get(0).getQName().getLocalPart());
		
		List<Link> links = receiptEntry.getLinks(UriRegistry.REL_DERIVED_RESOURCE);
		assertEquals(2, links.size());
		
		// Check derived resources.  Order can't be guaranteed since its going into a hashmap, so check both orders
		if (links.get(0).getAttributeValue("href").equals(config.getSwordPath() + SwordConfigurationImpl.EDIT_MEDIA_PATH + "/" + pid.getPid() + "/" + ContentModelHelper.Datastream.DATA_FILE.getName())){
			assertEquals("image/jpg", links.get(0).getAttributeValue("type"));
			assertEquals("text/xml", links.get(1).getAttributeValue("type"));
			assertEquals("http://localhost/sword/em/uuid:test/" + ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName(), links.get(1).getAttributeValue("href"));
		} else {
			assertEquals("image/jpg", links.get(1).getAttributeValue("type"));
			assertEquals("http://localhost/sword/em/uuid:test/" + ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName(), links.get(1).getAttributeValue("href"));
			assertEquals("text/xml", links.get(0).getAttributeValue("type"));
			assertEquals("http://localhost/sword/em/uuid:test/" + ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName(), links.get(0).getAttributeValue("href"));
		}
		
		links = receiptEntry.getLinks(UriRegistry.REL_STATEMENT);
		assertEquals(1, links.size());
		assertEquals(config.getSwordPath() + SwordConfigurationImpl.STATE_PATH + "/" + pid.getPid(), links.get(0).getAttributeValue("href"));
		
		receiptEntry.writeTo(System.out);
	}
	
	@Test
	public void retrieveDepositReceiptWithDCTest() throws Exception {
		PID pid = new PID("uuid:test");
		
		TripleStoreQueryService tripleStoreQueryService = mock(TripleStoreQueryService.class);
		
		Map<String,String> disseminations = new HashMap<String,String>();
		disseminations.put(pid.getURI() + "/" + ContentModelHelper.Datastream.DC.getName(), "text/xml");
		disseminations.put(pid.getURI() + "/" + ContentModelHelper.Datastream.DATA_FILE.getName(), "image/jpg");
		
		when(tripleStoreQueryService.fetchDisseminatorMimetypes(any(PID.class))).thenReturn(disseminations);
		
		RandomAccessFile modsFile = new RandomAccessFile("src/test/resources/dcDocument.xml", "r");
		byte[] modsBytes = new byte[(int)modsFile.length()];
		modsFile.read(modsBytes);
		MIMETypedStream mimeStream = new MIMETypedStream();
		mimeStream.setStream(modsBytes);
		
		AccessClient accessClient = mock(AccessClient.class);
		when(accessClient.getDatastreamDissemination(any(PID.class), eq(ContentModelHelper.Datastream.DC.getName()), anyString())).thenReturn(mimeStream);
		
		DepositReportingUtil depositReportingUtil = new DepositReportingUtil();
		depositReportingUtil.setAccessClient(accessClient);
		depositReportingUtil.setTripleStoreQueryService(tripleStoreQueryService);
		
		DepositReceipt receipt = depositReportingUtil.retrieveDepositReceipt(pid, config);
		
		Entry receiptEntry = receipt.getAbderaEntry();
		
		assertEquals("There must be 20 DC extensions", 20, receiptEntry.getExtensions().size());
		for (Element extension: receiptEntry.getExtensions()){
			assertEquals(UriRegistry.DC_NAMESPACE, extension.getQName().getNamespaceURI());
		}
	}
}
