package edu.unc.lib.dl.update;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.jdom.Element;

import edu.unc.lib.dl.util.AtomPubMetadataParserUtil;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.xml.ModsXmlHelper;

public class AtomDCToMODSFilter extends MODSUIPFilter {
	private static Logger log = Logger.getLogger(AtomDCToMODSFilter.class);
	private final String datastreamName = AtomPubMetadataParserUtil.ATOM_DC_DATASTREAM;
	
	public AtomDCToMODSFilter() {
		super();
	}
	
	@Override
	public UpdateInformationPackage doFilter(UpdateInformationPackage uip) throws UIPException {
		// Only run this filter for metadata update requests
		if (uip == null || !(uip instanceof MetadataUIP))
			return uip;

		// Do not apply filter dcterms is populated AND there is no incoming mods
		if (!(uip.getIncomingData().containsKey(datastreamName) || uip.getModifiedData().containsKey(datastreamName))
				|| uip.getIncomingData().get(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName()) != null)
			return uip;
		
		MetadataUIP metadataUIP = (MetadataUIP) uip;
		
		log.debug("Performing AtomDCToMODSFilter on " + uip.getPID().getPid());

		try {
			Element atomDCTerms = metadataUIP.getIncomingData().get(datastreamName);
			
			//Transform the DC to MODS
			Element mods = ModsXmlHelper.transformDCTerms2MODS(atomDCTerms).getRootElement();
			
			Element newModified = null;

			//Use the newly transformed mods as the incoming data, being sent to MD_DESCRIPTIVE
			switch (uip.getOperation()) {
				case REPLACE:
					newModified = performReplace(metadataUIP, ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName(), mods);
					break;
				case ADD:
					newModified = performAdd(metadataUIP, ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName(), mods);
					break;
				case UPDATE:
					// Doing add for update since the schema does not allow a way to indicate a tag should replace another
					newModified = performAdd(metadataUIP, ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName(), mods);
					break;
			}
			
			if (newModified != null) {
				// Validate the new mods before storing
				validate(uip, newModified);
				metadataUIP.getModifiedData().put(ContentModelHelper.Datastream.MD_DESCRIPTIVE.getName(), newModified);
			}
		} catch (TransformerException e) {
			throw new UIPException("Failed to transform DC Terms to MODS", e); 
		}
		return uip;
	}

}
