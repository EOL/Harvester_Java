package org.bibalex.eol.parser;

import org.bibalex.eol.parser.models.Agent;
import org.bibalex.eol.parser.models.Association;
import org.bibalex.eol.parser.models.MeasurementOrFact;
import org.bibalex.eol.parser.models.Reference;
import org.bibalex.eol.parser.utils.CommonTerms;
import org.bibalex.eol.parser.utils.TermURIs;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;

public class DwcaParser {

    Archive dwca;
    HashMap<String, Reference> referencesMap;
    HashMap<String, Agent> agentsMap;
    HashMap<String, MeasurementOrFact> measurementOrFactHashMap;
    //this is used to save the associations with target occurrence
    HashMap<String, Association> twoSidedAccoiationsMap;
    //this is used to save the associations without target occurrence
    HashMap<String, Association> oneSidedAccoiationsMap;
    private static final Logger logger = LoggerFactory.getLogger(DwcaParser.class);

    public DwcaParser(Archive dwca) {
        this.dwca = dwca;
        referencesMap = new HashMap<>();
        agentsMap = new HashMap<>();
        twoSidedAccoiationsMap = new HashMap<>();
        oneSidedAccoiationsMap = new HashMap<>();
        measurementOrFactHashMap = new HashMap<>();
        loadAllReferences();
        loadAllAgents();
        loadAllMeasurementOrFacts();
        loadAllAssociations();
    }

    private void loadAllReferences() {
        logger.debug("Loading all references with term: " + dwca.getExtension(CommonTerms.referenceTerm));
        if (dwca.getExtension(CommonTerms.referenceTerm) != null) {
            for (Iterator<Record> it = dwca.getExtension(CommonTerms.referenceTerm).iterator(); it.hasNext(); ) {
                Reference reference = parseReference(it.next());
                logger.debug("Adding reference to the map with id: " + reference.getReferenceId());
                referencesMap.put(reference.getReferenceId(), reference);
            }
        }
    }

    private void loadAllAgents() {
        logger.debug("Loading all agents with term: " + dwca.getExtension(CommonTerms.agentTerm));
        if (dwca.getExtension(CommonTerms.agentTerm) != null) {
            for (Iterator<Record> it = dwca.getExtension(CommonTerms.agentTerm).iterator(); it.hasNext(); ) {
                Agent agent = parseAgent(it.next());
                logger.debug("Adding Agent to the map with id: " + agent.getAgentId());
                agentsMap.put(agent.getAgentId(), agent);
            }
        }
    }

    private void loadAllMeasurementOrFacts() {
        logger.debug("Loading all measurement or facts with term: " + dwca.getExtension(DwcTerm.MeasurementOrFact));
        if (dwca.getExtension(DwcTerm.MeasurementOrFact) != null) {
            for (Iterator<Record> it = dwca.getExtension(DwcTerm.MeasurementOrFact).iterator(); it.hasNext(); ) {
                MeasurementOrFact measurementOrFact = parseMeasurementOrFact(it.next());
                logger.debug("Adding Measurement or fact to the map with id: " + measurementOrFact.getMeasurementId());
                measurementOrFactHashMap.put(measurementOrFact.getMeasurementId(), measurementOrFact);
            }
        }
    }

    private void loadAllAssociations() {
        logger.debug("Loading all associations with term: " + dwca.getExtension(CommonTerms.associationTerm));
        if (dwca.getExtension(CommonTerms.associationTerm) != null) {
            for (Iterator<Record> it = dwca.getExtension(CommonTerms.associationTerm).iterator(); it.hasNext(); ) {
                Association association = parseAssociation(it.next());
                if(association.getTargetOccurrenceId() != null && !association.getTargetOccurrenceId().isEmpty()){
                    twoSidedAccoiationsMap.put(association.getOccurrenceId(), association);
                }else{
                    oneSidedAccoiationsMap.put(association.getAssociationId(), association);
                }
            }
        }
    }


    private MeasurementOrFact parseMeasurementOrFact(Record record) {
        //Note: ReferenceId here is put with the other format for reference IDs (Check TODO in parseReferences).
        return new MeasurementOrFact(record.value(DwcTerm.measurementID),
                record.value(DwcTerm.occurrenceID),
                record.value(TermFactory.instance().findTerm(TermURIs.measurementOfTaxonURI)),
                record.value(CommonTerms.associationIDTerm),
                record.value(TermFactory.instance().findTerm(TermURIs.parentMeasurementIDURI)),
                record.value(DwcTerm.measurementType), record.value(DwcTerm.measurementValue),
                record.value(DwcTerm.measurementUnit), record.value(DwcTerm.measurementAccuracy),
                record.value(TermFactory.instance().findTerm(TermURIs.statisticalMethodURI)),
                record.value(DwcTerm.measurementDeterminedDate), record.value(DwcTerm.measurementDeterminedBy),
                record.value(DwcTerm.measurementMethod), record.value(DwcTerm.measurementRemarks),
                record.value(CommonTerms.sourceTerm), record.value(CommonTerms.bibliographicCitationTerm),
                record.value(CommonTerms.contributorTerm), record.value(CommonTerms.referenceIDTerm));
    }

    private Association parseAssociation(Record record) {
        //Note: ReferenceId here is put with the other format for reference IDs (Check TODO in parseReferences).
        return new Association(record.value(CommonTerms.associationIDTerm),
                record.value(DwcTerm.occurrenceID),
                record.value(TermFactory.instance().findTerm(TermURIs.associationType)),
                record.value(TermFactory.instance().findTerm(TermURIs.targetOccurrenceID)),
                record.value(DwcTerm.measurementDeterminedDate), record.value(DwcTerm.measurementDeterminedBy),
                record.value(DwcTerm.measurementMethod), record.value(DwcTerm.measurementRemarks),
                record.value(CommonTerms.sourceTerm), record.value(CommonTerms.bibliographicCitationTerm),
                record.value(CommonTerms.contributorTerm), record.value(CommonTerms.referenceIDTerm));
    }

    private Reference parseReference(Record record) {
        //TODO: ReferenceId term can be replaced according to Jen & Katja's request.
        //TODO: localityOfPublisherURI may be removed upon Jen & Katja's request.
        return new Reference(record.value(CommonTerms.identifierTerm),
                record.value(TermFactory.instance().findTerm(TermURIs.publicationTypeURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.fullReferenceURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.primaryTitleURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.titleURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.pagesURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.pageStartURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.pageEndURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.volumeURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.editionURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.publisherURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.authorsListURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.editorsListURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.createdURI)),
                record.value(CommonTerms.languageTerm),
                record.value(TermFactory.instance().findTerm(TermURIs.uriURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.doiURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.localityOfPublisherURI)));
    }

    private Agent parseAgent(Record record) {
        return new Agent(record.value(CommonTerms.identifierTerm),
                record.value(TermFactory.instance().findTerm(TermURIs.agentNameURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentFirstNameURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentFamilyNameURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentRole)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentEmailURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentHomepageURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentLogoURLURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentProjectURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentOrganizationURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentAccountNameURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentOpenIdURI)));
    }


}
