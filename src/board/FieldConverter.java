package board;

import org.slf4j.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.datatypes.xsd.*;

import com.hp.hpl.jena.sparql.core.*;

import com.hp.hpl.jena.vocabulary.RDF;

import board.vocabulary.*;

import comm.*;
import hub.*;

import java.util.*;
import java.net.*;

/**
 * Converts between {@link AttributeFields} objects and RDF strings;
 * also kind of implements the {@link reasoner.StatusReasoner#getStatus}
 * method for {@link Whiteboard} integration.
 */
public class FieldConverter {
  private static Logger logger = LoggerFactory.getLogger (FieldConverter.class);

  /**
   * Translates from {@link AttributeFields} to MATe ontology enumeration
   * strings.
   */
  private static Resource getAttributeProperty (AttributeFields attribute) {
    switch (attribute) {
    case ACTIVITY:
      return Mate.resource ("activity");
    case INTERRUPTIBILITY:
      return Mate.resource ("interruptible");
    default:
      return null;
    }
  }

  /**
   * Translates from {@link AttributeFields} to MATe ontology class names.
   */
  private static Resource getAttributeType (AttributeFields attribute) {
    switch (attribute) {
    case ACTIVITY:
      return Mate.resource ("ActivityValue");
    case INTERRUPTIBILITY:
      return Mate.resource ("InterruptibilityValue");
    default:
      return null;
    }
  }

  /**
   * Translates to {@link AttributeFields} from MATe ontology enumeration
   * strings.
   */
  private static String translateArchaic (AttributeFields attribute, String string) {
    if (string != null && attribute == AttributeFields.INTERRUPTIBILITY) {
      if (string.equals ("uninterruptible"))
	return "0";
      if (string.equals ("interruptible"))
	return "1";
      if (string.equals ("maybeInterruptible"))
	return "2";
    }
    if (string != null && attribute == AttributeFields.ACTIVITY) {
      if (string.equals ("shortBreak"))
	return "break_short";
      if (string.equals ("longBreak"))
	return "break_long";
    }
    return string;
  }

  /**
   * Extracts information from the {@link Whiteboard} following the
   * {@link reasoner.StatusReasoner#getStatus} semantics.
   */
  public static String getStatus (Whiteboard whiteboard, String userId,
				  AttributeFields attribute) {
    logger.trace ("getstatus " + userId + ", " + attribute);

    final String queryString = "PREFIX rdf: <" + RDF.getURI () + "> "
      + "PREFIX mate: <" + Mate.prefix + "> "
      + "SELECT ?marker ?value ?type ?property ?user "
      + "WHERE {"
      + "GRAPH <" + Mate.uri + "/graphs#world> {"
      + "?marker rdf:type ?type . "
      + "?marker ?property ?value . "
      + "?marker mate:userID ?user . "
      + "}"
      + "}";
    Query query = QueryFactory.create (queryString);
    query.setPrefixMapping (whiteboard.prefixes);

    logger.trace ("query = " + query);

    QuerySolutionMap bindings = new QuerySolutionMap ();
    Resource type = getAttributeType (attribute);
    Resource property = getAttributeProperty (attribute);
    if (type == null || property == null) {
      logger.error ("can't translate attribute '" + attribute + "' into RDF terms, aborting");
      return null;
    }

    Literal user = ResourceFactory.createTypedLiteral (userId, XSDDatatype.XSDstring);

    logger.trace ("type = " + type + ", property = " + property + ", user = " + user);

    bindings.add ("type", type);
    bindings.add ("property", property);
    bindings.add ("user", user);

    String result = null;

    synchronized (whiteboard) {
      QueryExecution exec = whiteboard.query (query);
      exec.setInitialBinding (bindings);

      logger.trace ("exec = " + exec);

      try {
	ResultSet results = exec.execSelect ();
	while (results.hasNext ()) {
	  QuerySolution solution = results.next ();

	  logger.trace ("solution " + solution);

	  RDFNode value = solution.get ("value");
	  if (value.isLiteral ())
	    result = value.asLiteral ().getValue ().toString ();
	  else if (value.isURIResource ())
	    result = value.asResource ().getLocalName ();
	  else
	    logger.error ("value binding was an anonymous node, ignoring");
	}
      }
      finally {
	exec.close ();
      }
    }

    if (result == null) {
      switch (attribute) {
      case ACTIVITY:
	result = "unknown";
	break;
      case INTERRUPTIBILITY:
	result = "interruptible";
	break;
      }
    }

    return translateArchaic (attribute, result);
  }
}
