package org.bibalex.eol.parser.utils;

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
}
