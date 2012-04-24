package edu.unc.lib.dl.update;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.Parser;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.unc.lib.dl.agents.PersonAgent;
import edu.unc.lib.dl.fedora.AccessClient;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.fedora.types.MIMETypedStream;
import edu.unc.lib.dl.schematron.SchematronValidator;
import edu.unc.lib.dl.util.AtomPubMetadataParserUtil;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.xml.JDOMNamespaceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/service-context.xml" })
public class AtomDCToMODSFilterTest extends Assert {
	@Resource
	private SchematronValidator schematronValidator;
	private AtomDCToMODSFilter filter;
	
	private static Logger log = Logger.getLogger(AtomDCToMODSFilter.class);
	
	public AtomDCToMODSFilterTest() {
		filter = new AtomDCToMODSFilter();
	}
	
	@PostConstruct
	public void init(){
		filter.setSchematronValidator(schematronValidator);
	}
	
	@Test
	public void addNewMODSFromDCTerms() throws Exception {
		InputStream entryPart = new FileInputStream(new File("src/test/resources/atompub/metadataDC.xml"));
		Abdera abdera = new Abdera();
		Parser parser = abdera.getParser();
		Document<Entry> entryDoc = parser.parse(entryPart);
		Entry entry = entryDoc.getRoot();
		
		AccessClient accessClient = mock(AccessClient.class);
		when(accessClient.getDatastreamDissemination(any(PID.class), anyString(), anyString())).thenReturn(null);
		
		PID pid = new PID("uuid:test");
		PersonAgent user = new PersonAgent("testuser", "testuser");

		AtomPubMetadataUIP uip = new AtomPubMetadataUIP(pid, user, UpdateOperation.ADD, entry);
		
		assertEquals(0, uip.getOriginalData().size());
		assertEquals(0, uip.getModifiedData().size());
		assertEquals(2, uip.getIncomingData().size());
		
		uip.storeOriginalDatastreams(accessClient);
		
		assertFalse(uip.getOriginalData().containsKey(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()));
		assertFalse(uip.getOriginalData().containsKey(AtomPubMetadataParserUtil.ATOM_DC_DATASTREAM));
		assertTrue(uip.getIncomingData().containsKey(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()));
		assertTrue(uip.getIncomingData().containsKey(AtomPubMetadataParserUtil.ATOM_DC_DATASTREAM));
		
		filter.doFilter(uip);
		
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(uip.getModifiedData().get(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()), System.out);
		log.debug(outputter.outputString(uip.getModifiedData().get(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName())));
		
		assertFalse(uip.getOriginalData().containsKey(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()));
		assertFalse(uip.getOriginalData().containsKey(AtomPubMetadataParserUtil.ATOM_DC_DATASTREAM));
		assertTrue(uip.getModifiedData().containsKey(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()));
		assertFalse(uip.getModifiedData().containsKey(AtomPubMetadataParserUtil.ATOM_DC_DATASTREAM));
		assertTrue(uip.getIncomingData().containsKey(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()));
		assertTrue(uip.getIncomingData().containsKey(AtomPubMetadataParserUtil.ATOM_DC_DATASTREAM));
		
		Element modsElement = uip.getModifiedData().get(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName());
		assertTrue(modsElement.getChildren().size() > 0);
	}
	
	@Test
	public void addNewMODSWithDCTerms() throws Exception {
		AtomPubMetadataUIP uip = mock(AtomPubMetadataUIP.class);
		
		@SuppressWarnings("unchecked")
		Map<String,Element> incomingData = mock(Map.class);
		when(incomingData.get(eq(AtomPubMetadataParserUtil.ATOM_DC_DATASTREAM))).thenReturn(new Element("atom_dc"));
		when(incomingData.get(eq(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()))).thenReturn(new Element("mods"));
		when(incomingData.containsKey(eq(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()))).thenReturn(true);
		when(incomingData.containsKey(eq(AtomPubMetadataParserUtil.ATOM_DC_DATASTREAM))).thenReturn(true);
		
		when(uip.getIncomingData()).thenReturn(incomingData);
		
		filter.doFilter(uip);
		
		// No changes should occur since there is also a mods record incoming
		verify(incomingData, times(1)).get(eq(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()));
		verify(uip, never()).getModifiedData();
	}
	
	@Test
	public void replaceMODSWithDCTerms() throws Exception {
		InputStream entryPart = new FileInputStream(new File("src/test/resources/atompub/metadataDC.xml"));
		Abdera abdera = new Abdera();
		Parser parser = abdera.getParser();
		Document<Entry> entryDoc = parser.parse(entryPart);
		Entry entry = entryDoc.getRoot();
		
		AccessClient accessClient = mock(AccessClient.class);
		
		MIMETypedStream modsStream = new MIMETypedStream();
		RandomAccessFile raf = new RandomAccessFile("src/test/resources/testmods.xml", "r");
		byte[] bytes = new byte[(int) raf.length()];
		raf.read(bytes);
		modsStream.setStream(bytes);
		modsStream.setMIMEType("text/xml");
		when(
				accessClient.getDatastreamDissemination(any(PID.class),
						eq(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()), anyString())).thenReturn(modsStream);
		
		PID pid = new PID("uuid:test");
		PersonAgent user = new PersonAgent("testuser", "testuser");

		AtomPubMetadataUIP uip = new AtomPubMetadataUIP(pid, user, UpdateOperation.REPLACE, entry);
		uip.storeOriginalDatastreams(accessClient);
		
		filter.doFilter(uip);
		
		Element dcTitleElement = uip.getIncomingData().get(AtomPubMetadataParserUtil.ATOM_DC_DATASTREAM);
		String dcTitle = dcTitleElement.getChildText("title", JDOMNamespaceUtil.DCTERMS_NS);
		
		Element oldMODSElement = uip.getOriginalData().get(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName());
		String oldMODSTitle = oldMODSElement.getChild("titleInfo", JDOMNamespaceUtil.MODS_V3_NS).getChildText("title", JDOMNamespaceUtil.MODS_V3_NS);
		
		Element modsElement = uip.getModifiedData().get(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName());
		String newMODSTitle = modsElement.getChild("titleInfo", JDOMNamespaceUtil.MODS_V3_NS).getChildText("title", JDOMNamespaceUtil.MODS_V3_NS);
		
		assertEquals("Title", dcTitle);
		assertEquals("Hiring and recruitment practices in academic libraries", oldMODSTitle);
		assertEquals(dcTitle, newMODSTitle);
		
		assertEquals(1, uip.getOriginalData().size());
		assertEquals(1, uip.getModifiedData().size());
		assertEquals(2, uip.getIncomingData().size());
	}
	
	@Test
	public void wrongUIPType() throws UIPException{
		ContentUIP uip = mock(ContentUIP.class);
		
		filter.doFilter(uip);
		
		verify(uip, times(0)).getIncomingData();
		verify(uip, times(0)).getModifiedData();
	}
}
