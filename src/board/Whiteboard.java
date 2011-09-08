package board;

import org.slf4j.*;

import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.update.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.compose.*;

import com.hp.hpl.jena.sparql.core.*;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.util.*;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.modify.request.*;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;
import com.hp.hpl.jena.vocabulary.OWL;

import java.net.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;

import de.fuberlin.wiwiss.d2rq.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import board.vocabulary.*;

import hub.*;

import comm.*;

/**
 * The main class implementing all the behaviour of a typical
 * {@link Board}.  May be run within a thread.
 */
public class Whiteboard implements Board, Runnable {
  private static Logger logger = LoggerFactory.getLogger (Whiteboard.class);

  /**
   *
   */
  com.hp.hpl.jena.reasoner.Reasoner reasoner;

  /**
   * Contains the public world knowledge (except sensor data).
   */
  public Model worldModel;

  /**
   * Write access to the {@link #worldModel}.
   */
  private GraphStore worldStore;

  /**
   * Contains only sensor data (i.e. in a fixed format described where?).
   */
  public Model sensorValues;

  /**
   * Write access to the {@link #sensorValues}.
   */
  private GraphStore sensorStore;

  /**
   * Contains historic sensor data.
   */
  public Model historyValues;

  /**
   * Write access to the {@link #historyValues}.
   */
  private GraphStore historyStore;

  /**
   * Contains the basic MATe ontology.
   */
  public MateOntology mateOntology;

  /**
   *
   */
  com.hp.hpl.jena.reasoner.Reasoner mateReasoner;

  /**
   * Contains the extended parts for sensors and their values.
   */
  public MateOntology sensorOntology;

  /**
   *
   */
  com.hp.hpl.jena.reasoner.Reasoner sensorReasoner;

  /**
   * Default prefixes for convenience and smaller text.
   * @see ModelFactory#getDefaultModelPrefixes
   */
  public PrefixMapping prefixes;

  /**
   * All registered clients (which includes reasoners).
   */
  public List<Client> clients;

  /**
   * Per client model instance for indirecting new values.
   */
  public Map<Client, Model> privateModels;

  /**
   * Write access for each model in {@link #privateModels}.
   */
  public Map<Client, GraphStore> privateStores;

  /**
   * Registered set of combiners, indexed by the RDF type URI.
   */
  private Map<String, Combiner> combiners;

  /**
   *
   */
  public Model legacyModel;  

  public Whiteboard (Options options) throws FileNotFoundException {
    reset (options);
  }

  /**
   * Resets the board to a known (clean/empty?) state.  May fail if
   * ontology or example files were not found.
   */
  public void reset (Options options) throws FileNotFoundException {
    prefixes = PrefixMapping.Factory.create ();
    prefixes.setNsPrefix ("rdf", RDF.getURI ());
    prefixes.setNsPrefix ("xsd", XSD.getURI ());
    prefixes.setNsPrefix ("owl", OWL.getURI ());
    prefixes.setNsPrefix ("mate", Mate.prefix);
    prefixes.setNsPrefix ("sensors", Sensors.prefix);
    ModelFactory.setDefaultModelPrefixes (prefixes);

    worldModel = ModelFactory.createDefaultModel ();
    sensorValues = ModelFactory.createDefaultModel ();
    historyValues = ModelFactory.createDefaultModel ();

    sensorValues.setNsPrefixes (worldModel);
    historyValues.setNsPrefixes (worldModel);

    worldStore = GraphStoreFactory.create (worldModel);
    sensorStore = GraphStoreFactory.create (sensorValues);
    historyStore = GraphStoreFactory.create (historyValues);

    // logger.info ("cache models = " + OntDocumentManager.getInstance ().getCacheModels ());
    // OntDocumentManager.getInstance ().setCacheModels (true);

    /* TODO: this could be done better, i.e. set the paths for the file
       manager and then only request the documents by their URI.  so if
       we can actually get them from the URI, we might do so instead of
       having local copies.
       i.e.:
       FileManager.get ().addAltEntry (Mate.prefix..., "localMate.owl");
       ...
       mateOntology = FileManaget.get ().loadModel (Mate.prefix ...)
    */
    FileManager manager = FileManager.get ();
    Iterator<Locator> it = manager.locators ();
    while (it.hasNext ()) {
      logger.info ("locator " + it.next ().getName ());
    }    

    mateOntology = loadOntology ("mate.n3", "N3");
    /* maps the mateOntology object to the URI, */
    /* TODO: except this doesn't work as expected, i.e. the model is probably copied */
    OntDocumentManager.getInstance ().addModel (Mate.uri, mateOntology.model, true);
    /* ... so this ontology can properly import its classes.  this creates
       duplicate classes for the mate ontology, but that is no problem atm */
    sensorOntology = loadOntology ("sensors.n3", "N3");
    sensorOntology.addBaseOntology (mateOntology);

    /* and here the cached model from above isn't shown */
    // {
    //   Iterator<String> it = OntDocumentManager.getInstance ().listDocuments ();
    //   while (it.hasNext ()) {
    // 	logger.info ("document " + it);
    //   }
    // }

    // logger.info ("mate ontology at " + Integer.toHexString (mateOntology.model.hashCode ()));
    // logger.info ("sensor ontology at " + Integer.toHexString (sensorOntology.model.hashCode ()));
    // logger.info ("=? " + (mateOntology.model == sensorOntology.model));

    // Iterator<OntModel> it = mateOntology.model.listSubModels (true);
    // while (it.hasNext ()) {
    //   logger.info ("mate imported model " + Integer.toHexString (it.next ().hashCode ()));
    // }
    // it = sensorOntology.model.listSubModels (true);
    // while (it.hasNext ()) {
    //   logger.info ("sensor imported model " + Integer.toHexString (it.next ().hashCode ()));
    // }

    clients = new ArrayList<Client> ();
    privateModels = new HashMap<Client, Model> ();
    privateStores = new HashMap<Client, GraphStore> ();

    combiners = new HashMap<String, Combiner> ();

    String string = Options.getInstance ().reasoner;
    if (string.equals ("micro"))
      reasoner = ReasonerRegistry.getOWLMicroReasoner ();
    else if (string.equals ("mini"))
      reasoner = ReasonerRegistry.getOWLMiniReasoner ();
    else if (string.equals ("full"))
      reasoner = ReasonerRegistry.getOWLReasoner ();
    /* well, this is redundant */
    else if (string.equals ("none"))
       reasoner = null;

    if (reasoner != null) {
      mateReasoner = reasoner.bindSchema (mateOntology.model);
      sensorReasoner = reasoner.bindSchema (sensorOntology.model);
    }

    org.apache.log4j.Logger.getLogger ("de.fuberlin.wiwiss.d2rq").setLevel(org.apache.log4j.Level.ALL);

    /* Set up the ModelD2RQ using a mapping file */
    legacyModel = new ModelD2RQ ("file:d2rq.n3");

    try {
      runWebServer ();
    }
    catch (Exception e) {
      logger.error ("couldn't run web server: " + e);
      logger.error (writeToString (e));
    }
  }

  /**
   * Loads an RDF file into a model.
   * @param lang Specifies whether to use XML/N3/...
   * @throws FileNotFoundException
   * @return The same model.
   */
  public static Model loadRdf (String pathname, String lang, Model model) throws FileNotFoundException {
    InputStream input = FileManager.get ().open (pathname);
    if (input == null)
      throw new FileNotFoundException ("file \"" + pathname + "\" not found");
    // second parameter is base, we assume there are no relative urls
    return model.read (input, null, lang);
  }

  /**
   * Loads an RDF file into a newly created default model.
   * @see #loadRdf(String, String, Model)
   * @see ModelFactory#createDefaultModel
   */
  public static Model loadRdf (String pathname, String lang) throws FileNotFoundException {
    return loadRdf (pathname, lang, ModelFactory.createDefaultModel ());
  }

  /**
   * Loads an OWL ontology into a newly created default model.
   * @see #loadRdf(String, String)
   * @see ModelFactory#createOntologyModel
   */
  public static MateOntology loadOntology (String pathname, String lang) throws FileNotFoundException {
    return new MateOntology (loadRdf (pathname, lang, ModelFactory.createDefaultModel()));
  }

  /**
   * Runs the whiteboard event processing cycle.
   */
  public void run () {
    
  }

  public static void main (String args[]) throws Exception {
    Options options = Options.getInstance ();
    boolean exit = false;
    try {
      options.parse (args);
    }
    catch (Exception e) {
      logger.error (writeToString (e));
    }

    if (exit || options.help) {
      System.out.println ("Usage: board.Whiteboard [OPTION]...");
      options.printHelpOn (System.out);
      return;
    }
    if (options.version) {
      System.out.println ("MATe Whiteboard (c) 2011");
      return;
    }

    Whiteboard board = new Whiteboard (options);

    Thread thread = new Thread (board, "whiteboard event thread");
    thread.start ();

    Model sample1 = loadRdf ("sample1.n3", "N3");
    Model sample2 = loadRdf ("sample2.n3", "N3");
    Model sample3 = loadRdf ("sample3.n3", "N3");
    Model sample4 = loadRdf ("sample4.n3", "N3");

    LoggingClient client = new LoggingClient ();
    LoggingReasoner reasoner = new LoggingReasoner ();

    board.registerClient (client);
    board.registerClient (reasoner);

    board.postSensorUpdate (sample1);
    board.postSensorUpdate (sample2);

    LoggingCombiner combiner = new LoggingCombiner ();
    board.registerCombiner (Mate.resource ("AvailabilityResult").getURI (), combiner);

    board.postWorldUpdate (client, sample3);
    board.postWorldUpdate (reasoner, sample4);

    Query query = QueryFactory.read ("file:query1.sparql");
    QueryExecution exec = board.query (query);
    logger.info ("running query " + query);
    try {
      ResultSet results = exec.execSelect ();
      while (results.hasNext ()) {
    	QuerySolution solution = results.next ();

	logger.trace ("solution " + solution);
      }
    }
    finally {
      exec.close ();
    }

    thread.join ();
  }

  private void runWebServer () throws Exception {
    int port = 8000;

    Server server = new Server (port);
    ServletContextHandler context = new ServletContextHandler (ServletContextHandler.NO_SESSIONS);
    context.setContextPath ("/");
    server.setHandler (context);

    context.addServlet (new ServletHolder (new IndexServlet (this)), "/*");
    context.addServlet (new ServletHolder (new OntologyServlet (mateOntology)), "/mate/*");
    context.addServlet (new ServletHolder (new OntologyServlet (sensorOntology)), "/mate/sensors/*");
    context.addServlet (new ServletHolder (new ModelServlet (worldModel)), "/world/*");
    context.addServlet (new ServletHolder (new ModelServlet (sensorValues)), "/sensors/*");
    context.addServlet (new ServletHolder (new ModelServlet (historyValues)), "/history/*");

    server.start ();

    logger.info ("server is listening on port " + port);
  }

  public void registerClient (Client client) {
    clients.add (client);
  }

  public void unregisterClient (Client client) {
    clients.remove (client);
    privateModels.remove (client);
  }

  public void registerCombiner (String uri, Combiner combiner) {
    combiners.put (uri, combiner);
  }

  public void unregisterCombiner (String uri) {
    combiners.remove (uri);
  }

  private Combiner getCombiner (String uri) {
    return combiners.get (uri);
  }

  private Model getPrivateModel (Client client) {
    Model result = privateModels.get (client);
    if (result == null) {
      result = ModelFactory.createDefaultModel ();
      privateModels.put (client, result);
    }
    return result;
  }

  private GraphStore getPrivateStore (Client client) {
    GraphStore result = privateStores.get (client);
    if (result == null) {
      result = GraphStoreFactory.create (getPrivateModel (client));
      privateStores.put (client, result);
    }
    return result;
  }

  public void registerClientPattern (String pattern, Client client) {

  }

  public void unregisterClientPattern (String pattern, Client client) {

  }

  /**
   * Checks whether a model is consistent with respect to the given
   * ontology.
   */
  public boolean isConsistent (MateOntology ontology, com.hp.hpl.jena.reasoner.Reasoner reasoner, Model model) {
    // consistency checking on every posted model
    InfModel infmodel = ModelFactory.createInfModel (reasoner, model);
    ValidityReport validity = infmodel.validate ();
    if (validity.isClean ())
      return true;
    boolean valid = validity.isValid ();
    if (valid)
      logger.warn ("model is not clean; we treat this as an error though");
    else
      logger.error ("model is inconsistent");
    for (Iterator<ValidityReport.Report> it = validity.getReports (); it.hasNext ();)
      if (valid)
	logger.warn (it.next ().toString ());
      else
	logger.error (it.next ().toString ());
    logger.trace (writeToString (model));
    // return valid;
    return false;
  }

  /**
   * Extracting a List of resources starting at the given position.
   */
  public static List<Resource> convertRdfList (Resource resource) {
    List<Resource> result = new ArrayList<Resource> ();

    /* this should really throw exceptions if either rdf:first, rdf:rest
       or rdf:nil aren't encountered at their respective positions,
       because it really is malformed data */
    /* also, this should be unit-tested */

    /* we got a nil, so we return an empty list */
    while (!resource.equals (RDF.nil)) {
      Statement statement = resource.getProperty (RDF.first);
      if (statement == null) {
	// TODO: logging
	return result;
      }

      Resource first = statement.getResource ();

      result.add (first);

      statement = resource.getProperty (RDF.rest);
      if (statement == null) {
	// TODO: logging
	return result;
      }
      resource = statement.getResource ();
    }

    return result;
  }

  /**
   * Pushes the item on the RDF list.
   */
  public void pushToRdfList (Resource listHead, Resource item) {
    /* listHead has to be of type */
  }

  private List<Triple> primaryKeyCheckTriples (Var var, Resource marker, MateClass klass) {
    List<Triple> result = new ArrayList<Triple> ();

    boolean hadProperties = false;

    for (Property property : klass.primaryKey) {
      Statement value = marker.getProperty (property);
      if (value != null) {
	result.add (new Triple (var,
				Node.createURI (property.getURI ()),
				value.getObject ().asNode ()));
	hadProperties = true;
      }
    }

    if (!hadProperties)
      throw new RuntimeException ("object didn't have at least one primary key property");

    return result;
  }

  private void addFilters (Var var, Resource marker, MateClass klass, ElementGroup group) {
    for (Property property : klass.primaryKey) {
      Statement value = marker.getProperty (property);
      if (value == null) {
	logger.trace ("new filter for property " + property.getURI ());
	ElementGroup element = new ElementGroup ();
	ElementPathBlock pathBlock = new ElementPathBlock ();
	pathBlock.addTriplePath (new TriplePath (new Triple (var, Node.createURI (property.getURI ()), Var.ANON)));
	element.addElement (pathBlock);
	Expr expr = new E_NotExists (element);
	ElementFilter filter = new ElementFilter (expr);
	group.addElement (filter);
      }
    }
  }

  private void addPrimaryKeyChecks (Var var, Resource marker, MateClass klass, PathBlock pattern, ElementGroup group) {
    for (Triple triple : primaryKeyCheckTriples (var, marker, klass))
      pattern.add (new TriplePath (triple));
    addFilters (var, marker, klass, group);
  }

  private void addPrimaryKeyChecks (Var var, Resource marker, MateClass klass, ElementGroup group) {
    for (Triple triple : primaryKeyCheckTriples (var, marker, klass))
      group.addTriplePattern (triple);
    addFilters (var, marker, klass, group);
  }

  public Model extract (Resource marker, MateClass klass) {
    switch (klass.mode) {
    case EXPLICIT:
      Model result = ModelFactory.createDefaultModel ();
      for (Property property : klass.extractExplicit)
	result.add (marker.listProperties (property));
      return null;
    case ONE_STEP:
      return ModelFactory.createDefaultModel ().add (marker.listProperties ());
    case CLOSURE:
      return Closure.closure (marker, true);
    default:
      throw new IllegalArgumentException ("well, this can't happen, ExtractMode should only have three possible values");
    }
  }

  public List<Model> matching (Model test, Resource marker, MateClass klass) {
    List<Model> result = new ArrayList<Model> ();

    final String queryString = "SELECT ?marker WHERE {}";

    Query query = QueryFactory.create (queryString);
    query.setPrefixMapping (prefixes);

    ElementGroup group = (ElementGroup) query.getQueryPattern ();

    ElementPathBlock block = new ElementPathBlock ();
    group.getElements ().add (block);

    Var var_marker = Var.alloc ("marker");

    PathBlock pattern = block.getPattern ();
    Resource type = klass.base.asResource ();
    pattern.add (new TriplePath (new Triple (var_marker, RDF.Nodes.type, type.asNode ())));

    addPrimaryKeyChecks (Var.alloc ("marker"), marker, klass, pattern, group);

    logger.trace ("matching query = " + query);

    /* construct query according to schema */
    for (Model model : privateModels.values ()) {
      QueryExecution exec = QueryExecutionFactory.create (query, model);
      try {
	ResultSet results = exec.execSelect ();
	while (results.hasNext ()) {
	  QuerySolution solution = results.next ();
	  result.add (extract (solution.get ("marker").asResource (), klass));
	}
      }
      finally {
	exec.close ();
      }
    }

    return result;
  }

  public void postWorldUpdate (Client poster, Model model) {
    if (mateReasoner != null && !isConsistent (mateOntology, mateReasoner, model)) {
      logger.warn ("update isn't consistent with respect to the mate ontology, discarding");
      return;
    }

    postGenericUpdate (mateOntology, poster, model, false, false);

    logger.trace ("world values =");
    logger.trace (writeToString (worldModel));
    logger.trace ("---");

    logger.trace ("private values =");
    for (Client client : clients) {
      logger.trace ("Client " + client + ", <" + client + ">:");
      logger.trace (writeToString (getPrivateModel (client)));
      logger.trace ("---");
    }
    logger.trace ("===");
  }

  public void postSensorUpdate (Model model) {
    if (sensorReasoner != null && !isConsistent (sensorOntology, sensorReasoner, model)) {
      logger.warn ("update isn't consistent with respect to the sensor ontology, discarding");
      return;
    }

    postGenericUpdate (sensorOntology, null, model, true, false);

    logger.trace ("sensor values =");
    logger.trace (writeToString (sensorValues));
    logger.trace ("---");
  }

  /**
   * Does the boring extraction of proper values from the update model
   * in a generic way for both world and sensor updates.
   * @param poster The client who posted the update model.  If sensorUpdate
   * is true, this is disregarded.
   */
  private void postGenericUpdate (MateOntology ontology, Client poster, Model model, boolean sensorUpdate, boolean recursive) {
    /* merge the posted model with the sensor values model, according to the
       defined schema; if there is no such schema, log it and continue */

    final String query = "SELECT ?marker ?type WHERE { ?marker a ?type }";

    /* for every rdf:type Statement in model */
    QueryExecution exec = QueryExecutionFactory.create (query, model);
    try {
      ResultSet results = exec.execSelect ();
      while (results.hasNext ()) {
    	QuerySolution solution = results.next ();

	RDFNode type = solution.get ("type");
	RDFNode marker = solution.get ("marker");

	Resource typeResource = type.asResource ();
	Resource markerResource = marker.asResource ();

	String typeUri = typeResource.getURI ();

	/* get the schema for its type */
	MateClass klass = ontology.getClass (typeUri);
	if (klass == null) {
	  logger.error ("no schema for type " + typeResource.getLocalName () + ", skipping this update");
	  continue;
	}

	if (sensorUpdate && (markerResource.getProperty (Sensors.property ("sensorJID")) == null)) {
	  logger.error ("sensor value is missing 'sensorJID' property, skipping this update");
	  continue;
	}

	/* query the sensor model for matching triples,
	   remove every other connected triple from that node */

	UpdateRequest deleteRequest = UpdateFactory.create ();
	deleteRequest.setPrefixMapping (prefixes);

	UpdateModify modify = new UpdateModify ();
	QuadAcc pattern = modify.getDeleteAcc ();
	deleteRequest.add (modify);

	Var var_s = Var.alloc ("s");
	Var var_p = Var.alloc ("p");
	Var var_o = Var.alloc ("o");

	/* "?s ?p ?o" */
	pattern.addTriple (new Triple (var_s, var_p, var_o));

	/* "?s rdf:type ?type", type from the initial query */
	ElementGroup group = new ElementGroup ();
	group.addTriplePattern (new Triple (var_s,
					    RDF.Nodes.type,
					    type.asNode ()));

	/* for every resource in the primary key, check whether it has
	   the given value: "?s resource value", or, if there is no value,
	   adds an filter expression "FILTER NOT EXISTS { ?s resource ?_ }".
	   this way, objects with (intentionally) missing parts from the
	   primary key are indexed by their remaining properties */
	try {
	  addPrimaryKeyChecks (var_s, markerResource, klass, group);
	}
	catch (Exception e) {
	  logger.error ("model didn't conform to its schema: " + e + ", skipping this update");
	  continue;
	}

	group.addTriplePattern (new Triple (var_s, var_p, var_o));

	modify.setElement (group);

	/* it would be nice to do this in one step, i.e. deleting and inserting the new data */
	/* this doesn't work for whatever reason */
	// QuadAcc insertPattern = modify.getInsertAcc ();
	// StmtIterator it = closure.listStatements ();
	// while (it.hasNext ()) {
	//   insertPattern.addTriple (it.nextStatement ().asTriple ());
	// }

	logger.trace ((recursive ? "recursive" : "nonrecursive") + " deleteRequest " + deleteRequest);

	/* get the closure, i.e. reachable statements */
	Model closure = Closure.closure (markerResource, true);

	logger.trace ("for model = " + writeToString (closure, "N3"));

	boolean result = true;
	if (sensorUpdate)
	  sensorUpdateInner (closure, typeResource, markerResource, typeUri, klass, deleteRequest);
	else
	  /* this might combine stuff and handle it itself, so check the return value */
	  result = worldUpdateInner (closure, poster, typeResource, markerResource, typeUri, klass, deleteRequest, recursive);

	if (result) {
	  /* add history entry */
	  updateHistory (closure, typeResource, markerResource, klass);

	  /* submit the updated sensor datum to matching clients */
	  for (Client client : clients) {
	    // TODO: check whether this client actually wants this message
	    /* beware of faulty clients */
	    try {
	      client.postUpdate (this, closure, klass, markerResource);
	    }
	    catch (Exception e) {
	      logger.error ("faulty client threw an exception: " + e);
	      logger.error (writeToString (e));
	    }
	  }
	}
      }
    }
    finally {
      exec.close ();
    }
  }

  /**
   * Called for every update instance in postGenericUpdate (if it's not
   * a sensor update.  It simply adds the closure into the sensor value
   * model.
   */
  private void sensorUpdateInner (Model closure, Resource typeResource,
				 Resource markerResource, String typeUri, MateClass klass,
				 UpdateRequest deleteRequest) {
    UpdateProcessor deleteExec = UpdateExecutionFactory.create (deleteRequest, sensorStore);
    deleteExec.execute ();

    /* insert the updated values into global database */
    sensorValues.add (closure);
  }

  /**
   * Called for every update instance in postGenericUpdate (if it's not
   * a sensor update.  In particular it possibly combines the private
   * models of the clients into a single result, which is then inserted
   * into the world model.  This result has to be carefully crafted
   * since previous values are only removed if they matched with the
   * deleteRequest, used in postGenericUpdate.
   */
  private boolean worldUpdateInner (Model closure, Client poster, Resource typeResource,
				    Resource markerResource, String typeUri, MateClass klass,
				    UpdateRequest deleteRequest, boolean recursive) {
    if (!recursive) {
      /* delete old stuff from the private database */
      UpdateProcessor deleteExec = UpdateExecutionFactory.create (deleteRequest, getPrivateStore (poster));
      deleteExec.execute ();

      /* insert the updated values into the private database */
      getPrivateModel (poster).add (closure);

      /* propagate inserted values from the private models into the public world model */
      Combiner combiner = getCombiner (typeUri);
      if (combiner != null) {
	/* if we have a custom module to decide this, execute it here */
	Model result = combiner.combine (this, poster, closure, typeResource, markerResource, klass);
	postGenericUpdate (mateOntology, poster, result, false, true);
	return false;
      }
    }

    /* else just override with the current values */
    /* TODO: can we share this somehow? */
    UpdateProcessor deleteExec = UpdateExecutionFactory.create (deleteRequest, worldStore);
    deleteExec.execute ();
    worldModel.add (closure);
    return true;
  }

  /**
   * Adds a new entry to the matching history entry.  Currently doesn't
   * limit the length of the list, so maybe that should be checked.
   */
  private void updateHistory (Model closure, Resource typeResource, Resource markerResource, MateClass klass) {
    final String queryString = "PREFIX mate: <" + Mate.prefix + "> " +
      "SELECT ?entry WHERE { ?entry a mate:HistoryEntry }";

    Query query = QueryFactory.create (queryString);
    query.setPrefixMapping (prefixes);

    ElementGroup group = (ElementGroup) query.getQueryPattern ();

    ElementPathBlock block = (ElementPathBlock) group.getElements ().get (0);
    PathBlock pattern = block.getPattern ();

    Var var_entry = Var.alloc ("entry");
    /* add "?entry mate:historyType ?type", but more readable than with initial bindings */
    pattern.add (new TriplePath (new Triple (var_entry, Node.createURI (Mate.historyType.getURI ()), typeResource.asNode ())));
    addPrimaryKeyChecks (var_entry, markerResource, klass, pattern, group);

    logger.trace ("matching query = " + query);

    /* construct query according to schema */

    /* TODO: prettier please? i.e. somehow use a template or something? */
    Resource entry, list;
    QueryExecution exec = QueryExecutionFactory.create (query, historyValues);
    try {
      ResultSet results = exec.execSelect ();
      if (results.hasNext ()) {
	QuerySolution solution = results.next ();
	entry = solution.get ("entry").asResource ();

	logger.trace ("entry = " + entry);

	Statement entries = entry.getProperty (Mate.historyEntries);
	list = entries.getObject ().asResource ();

	historyValues.add (closure);

	Resource newList = historyValues.createResource ();

	historyValues.add (historyValues.createStatement (newList,
							  RDF.type,
							  RDF.List));
	historyValues.add (historyValues.createStatement (newList,
							  RDF.first,
							  markerResource));
	historyValues.add (historyValues.createStatement (newList,
							  RDF.rest,
							  list));
	/* replace the first list with the new one */
	entries.changeObject (newList);
      }
      else {
	/* got no history entry, create one */
	entry = historyValues.createResource ();
	historyValues.add (historyValues.createStatement (entry,
							  RDF.type,
							  Mate.HistoryEntry));
	historyValues.add (historyValues.createStatement (entry,
							  Mate.historyType,
							  typeResource));
	
	for (Property property : klass.primaryKey) {
	  Statement value = markerResource.getProperty (property);
	  if (value != null)
	    historyValues.add (historyValues.createStatement (entry,
							      property,
							      value.getObject ()));
	}

	list = historyValues.createResource ();
	historyValues.add (historyValues.createStatement (entry,
							  Mate.historyEntries,
							  list));
	historyValues.add (historyValues.createStatement (list,
							  RDF.type,
							  RDF.List));
	historyValues.add (historyValues.createStatement (list,
							  RDF.first,
							  markerResource));
	historyValues.add (closure);
	historyValues.add (historyValues.createStatement (list,
							  RDF.rest,
							  RDF.nil));
      }
    }
    finally {
      exec.close ();
    }

    logger.trace ("history values =");
    logger.trace (writeToString (historyValues));
    logger.trace ("---");
  }

  public QueryExecution query (Query query) {
    // /* default graph goes over the union of these graphs */
    // Graph graphs[] = {worldModel.getGraph (), sensorValues.getGraph (), historyValues.getGraph ()};
    // MultiUnion union = new MultiUnion (graphs);
    // /* but updates only go to world */
    // union.setBaseGraph (graphs[0]);

    /* wrong, because it would return a new independent model */
    // Model union = worldModel.union (sensorValues).union (historyValues);

    DataSource source = DatasetFactory.create ();
    // source.setDefaultModel (ModelFactory.createModelForGraph (union));
    source.setDefaultModel (ModelFactory.createDefaultModel ());
    source.addNamedModel ("http://www.imis.uni-luebeck.de/mate/graphs#world", worldModel);
    source.addNamedModel ("http://www.imis.uni-luebeck.de/mate/graphs#sensor", sensorValues);
    source.addNamedModel ("http://www.imis.uni-luebeck.de/mate/graphs#history", historyValues);

    return QueryExecutionFactory.create (query, source);
  }

  public QueryExecution query (String query) {
    return query (QueryFactory.create (query));
  }

  public static String writeToString (Model model) {
    return writeToString (model, "N3");
  }

  public static String writeToString (Model model, String language) {
    StringWriter writer = new StringWriter ();
    model.write (writer, language);
    return writer.toString ();
  }

  public static String writeToString (Throwable t) {
    StringWriter writer = new StringWriter ();
    t.printStackTrace (new PrintWriter (writer));
    return writer.toString ();
  }

  /**
   * Takes a {@link DeviceMateMessage} and handles it depending on its
   * sub-type (i.e. we only do stuff on {@link StatusMessage} objects).
   */
  public synchronized void processMessage (DeviceMateMessage message) {
    if (message instanceof CommMessage) {
      CommMessage comm = (CommMessage) message;
      logger.warn ("got CommMessage " + comm + ", ignoring");
      return;
    }
    else if (message instanceof ResponseMessage) {
      ResponseMessage response = (ResponseMessage) message;
      logger.warn ("got ResponseMessage " + response + ", ignoring");
      return;
    }
    else if (!(message instanceof StatusMessage)) {
      logger.warn ("got unknown sub-type of DeviceMateMessage " + message + ", ignoring");
      return;
    }

    StatusMessage status = (StatusMessage) message;
    Request request = status.getRequest ();

    logger.info ("got StatusMessage " + status);
    logger.info ("from " + message.getSubjectDevice () + " to " + message.getObjectDevice ());

    if (!(status.getMode ().equals (StatusMode.PUSH) &&
	  request.getRequestType ().equals (RequestType.DATA))) {
      logger.warn ("mode not 'push' or request-type not 'data', ignoring");
      return;
    }
	
    Model model = MessageConverter.convert (status);
    /* TODO: we _need_ a client for each different sensor, else it is
       useless regarding combining update values ...
       maybe get it from a hashtable and generate them on demand? */
    if (model != null)
      postSensorUpdate (model);
  }

  public String toString () {
    return "Whiteboard";
  }

  public static URI parseXmppUri (String string) {
    try {
      URI uri = new URI (string);
      String scheme = uri.getScheme ();
      if (scheme == null) {
	uri = new URI ("xmpp", uri.getSchemeSpecificPart (), uri.getFragment ());
      }
      else if (!scheme.equals ("xmpp"))
	throw new URISyntaxException (string, "scheme isn't 'xmpp'");

      /* TODO: it'd be nice to have an actual XMPPURI class so we can do
	 more stuff on it, but that'll have to wait */
      // String nodeid = null, host = null, resid = null, query = null, fragment = null;

      // String split = uri.getSchemeSpecificPart ();
      // String parts[];

      // parts = split.split ("#");

      // if (parts.length == 2) {
      // 	fragment = parts[1];
      // 	split = parts[0];
      // }
      // else if (parts.length > 2)
      // 	throw new URISyntaxException (string, "more than one '#' separator");

      // parts = split.split ("\\?");

      // if (parts.length == 2) {
      // 	query = parts[1];
      // 	split = parts[0];
      // }
      // else if (parts.length > 2)
      // 	throw new URISyntaxException (string, "more than one '?' separator");      

      // parts = split.split ("/");

      // if (parts.length == 2) {
      // 	resid = parts[1];
      // 	split = parts[0];
      // }
      // else if (parts.length > 2)
      // 	throw new URISyntaxException (string, "more than one '/' separator");

      // parts = split.split ("@");

      // if (parts.length == 1)
      // 	host = parts[0];
      // else if (parts.length == 2) {
      // 	nodeid = parts[0];
      // 	host = parts[1];
      // }
      // else
      // 	throw new URISyntaxException (string, "more than one '@' separator");
      
      // logger.info ("scheme = " + scheme);
      // logger.info ("nodeid = " + nodeid);
      // logger.info ("host = " + host);
      // logger.info ("resid = " + resid);
      // logger.info ("query = " + query);
      // logger.info ("fragment = " + fragment);
      
      // return new URI (scheme, nodeid, host, -1, (resid == null) ? null : ("/" + resid), query, fragment);

      return uri;
    }
    catch (URISyntaxException e) {
      System.out.println ("couldn't parse URI '" + string + "': " + e);
      throw new RuntimeException (e);
    }
  }
}
