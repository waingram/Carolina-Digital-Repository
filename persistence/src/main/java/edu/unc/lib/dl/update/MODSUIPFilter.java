package edu.unc.lib.dl.update;

import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;
import org.xml.sax.SAXException;

import edu.unc.lib.dl.schematron.SchematronValidator;
import edu.unc.lib.dl.util.ContentModelHelper.Datastream;
import edu.unc.lib.dl.util.PremisEventLogger.Type;
import edu.unc.lib.dl.xml.JDOMNamespaceUtil;

/**
 * Filter which performs update operations on an MD_DESCRIPTIVE MODS datastream and validates it.
 * 
 * @author bbpennel
 * 
 */
public class MODSUIPFilter extends MetadataUIPFilter {
	private static Logger log = Logger.getLogger(MODSUIPFilter.class);

	private final String datastreamName = Datastream.MD_DESCRIPTIVE.getName();
	private SchematronValidator schematronValidator;
	private Validator modsValidator;

	public MODSUIPFilter() {
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		StreamSource modsSource = new StreamSource(getClass().getResourceAsStream("/schemas/mods-3-4.xsd"));
		Schema modsSchema;
		try {
			modsSchema = sf.newSchema(modsSource);
		} catch (SAXException e) {
			throw new RuntimeException("Initialization of MODS UIP Filter ran into an unexpected exception", e);
		}

		modsValidator = modsSchema.newValidator();
	}

	@Override
	public UpdateInformationPackage doFilter(UpdateInformationPackage uip) throws UIPException {
		// Only run this filter for metadata update requests
		if (uip == null || !(uip instanceof MetadataUIP))
			return uip;

		// Do not apply filter unless the mods ds is being targeted.
		if (!(uip.getIncomingData().containsKey(datastreamName) || uip.getModifiedData().containsKey(datastreamName)))
			return uip;

		MetadataUIP metadataUIP = (MetadataUIP) uip;

		log.debug("Performing MODS filter operation " + uip.getOperation().name() + " on " + uip.getPID().getPid());

		Element newModified = null;

		switch (uip.getOperation()) {
			case REPLACE:
				newModified = performReplace(metadataUIP, datastreamName);
				break;
			case ADD:
				newModified = performAdd(metadataUIP, datastreamName);
				break;
			case UPDATE:
				// Doing add for update since the schema does not allow a way to indicate a tag should replace another
				newModified = performAdd(metadataUIP, datastreamName);
				break;
		}

		if (newModified != null) {
			// Validate the new mods before storing
			validate(uip, newModified);
			metadataUIP.getModifiedData().put(datastreamName, newModified);
		}

		return uip;
	}

	protected void validate(UpdateInformationPackage uip, Element mods) throws UIPException {
		Document svrl = schematronValidator.validate(new JDOMSource(mods), "vocabularies-mods");
		String message = "Validation of Controlled Vocabularies in Descriptive Metadata (MODS)";
		Element event = uip.getEventLogger().logEvent(Type.VALIDATION, message, uip.getPID(), "MD_DESCRIPTIVE");
		if (!schematronValidator.hasFailedAssertions(svrl)) {
			uip.getEventLogger().addDetailedOutcome(event, "MODS is valid",
					"The supplied MODS metadata meets CDR vocabulary requirements.", null);
		} else {
			Element detailExtension = svrl.detachRootElement();
			uip.getEventLogger().addDetailedOutcome(event, "MODS is not valid",
					"The supplied MODS metadata does not meet CDR vocabulary requirements.", detailExtension);
			StringBuilder validationOutput = new StringBuilder();
			List<?> failedList = detailExtension.getChildren("failed-assert", JDOMNamespaceUtil.SCHEMATRON_VALIDATION_REPORT_NS);
			for (Object failedObject : failedList) {
				Element failedElement = (Element) failedObject;
				validationOutput.append(
						failedElement.getChildText("text", JDOMNamespaceUtil.SCHEMATRON_VALIDATION_REPORT_NS)).append('\n');
			}
			throw new UIPException("The supplied MODS metadata did not meet requirements.\n  " + validationOutput.toString());
		}
		try {
			modsValidator.validate(new JDOMSource(mods));
		} catch (SAXException e) {
			throw new UIPException("MODS failed to validate to schema", e);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected exception while attempting to validate MODS", e);
		}
	}

	public void setSchematronValidator(SchematronValidator schematronValidator) {
		this.schematronValidator = schematronValidator;
	}
}
