package org.bibalex.eol.utils;

import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;

public class CommonTerms {
    public static final Term languageTerm = TermFactory.instance().findTerm(TermURIs.languageURI);
    public static final Term agentTerm = TermFactory.instance().findTerm(TermURIs.agentURI);
    public static final Term identifierTerm = TermFactory.instance().findTerm(TermURIs.identifierURI);
    public static final Term mediaTerm = TermFactory.instance().findTerm(TermURIs.mediaURI);
    public static final Term typeTerm = TermFactory.instance().findTerm(TermURIs.termTypeURI);
    public static final Term titleTerm = TermFactory.instance().findTerm(TermURIs.titleURI);
    public static final Term descriptionTerm = TermFactory.instance().findTerm(TermURIs.descriptionURI);
    public static final Term accessURITerm = TermFactory.instance().findTerm(TermURIs.accessURI);
    public static final Term agentIDTerm = TermFactory.instance().findTerm(TermURIs.agentIDURI);
    public static final Term referenceTerm = TermFactory.instance().findTerm(TermURIs.EOLReferenceURI);
    public static final Term referenceIDTerm = TermFactory.instance().findTerm(TermURIs.referenceIDURI);
    public static final Term eolPageTerm = TermFactory.instance().findTerm(TermURIs.eolPageTerm);
    public static final Term modifiedDateTerm = TermFactory.instance().findTerm(TermURIs.modifiedDateURI);
    public static final Term associationIDTerm = TermFactory.instance().findTerm(TermURIs.associationIDURI);
    public static final Term bibliographicCitationTerm = TermFactory.instance().findTerm(TermURIs.bibliographicCitationURI);
    public static final Term sourceTerm = TermFactory.instance().findTerm(TermURIs.sourceURI);
    public static final Term contributorTerm = TermFactory.instance().findTerm(TermURIs.contributorURI);
    public static final Term occurrenceTerm = TermFactory.instance().findTerm(TermURIs.occurrenceURI);
    public static final Term associationTerm = TermFactory.instance().findTerm(TermURIs.associationURI);
    public static final Term furtherInformationURL = TermFactory.instance().findTerm(TermURIs.furtherInformationURL);
    public static final Term parentId = TermFactory.instance().findTerm(TermURIs.furtherInformationURL);
    public static final Term occurrenceID = TermFactory.instance().findTerm(TermURIs.occurrenceID);

    public static final Term domainTerm = TermFactory.instance().findTerm(TermURIs.domainURI);
    public static final Term kingdomTerm = TermFactory.instance().findTerm(TermURIs.kingdomURI);
    public static final Term phylumTerm = TermFactory.instance().findTerm(TermURIs.phylumURI);
    public static final Term classTerm = TermFactory.instance().findTerm(TermURIs.classURI);
    public static final Term cohortTerm = TermFactory.instance().findTerm(TermURIs.cohortURI);
    public static final Term divisionTerm = TermFactory.instance().findTerm(TermURIs.divisionURI);
    public static final Term orderTerm = TermFactory.instance().findTerm(TermURIs.orderURI);
    public static final Term familyTerm = TermFactory.instance().findTerm(TermURIs.familyURI);
    public static final Term tribeTerm = TermFactory.instance().findTerm(TermURIs.tribeURI);
    public static final Term genusTerm  = TermFactory.instance().findTerm(TermURIs.genusURI);
    public static final Term speciesTerm  = TermFactory.instance().findTerm(TermURIs.speciesURI);
    public static final Term varietyTerm  = TermFactory.instance().findTerm(TermURIs.varietyURI);
    public static final Term formTerm  = TermFactory.instance().findTerm(TermURIs.formURI);
    public static final Term generatedAutoIdTerm = TermFactory.instance().findTerm(TermURIs.generated_auto_id);
}
