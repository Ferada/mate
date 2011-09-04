package hub;

import org.slf4j.*;

import java.io.*;
import java.util.*;

import static java.util.Arrays.asList;

import joptsimple.*;

/**
 * Encapsulates all options in a type-safe and convenient singleton.
 */
public class Options {
  private static Logger logger = LoggerFactory.getLogger (Options.class);

  private static Options instance;

  public OptionParser parser;

  private ArgumentAcceptingOptionSpec<File> confOpt;
  private ArgumentAcceptingOptionSpec<String> xmppServerOpt;
  private ArgumentAcceptingOptionSpec<String> xmppUsernameOpt;
  private ArgumentAcceptingOptionSpec<String> xmppPasswordOpt;
  private ArgumentAcceptingOptionSpec<String> jdbcDriverOpt;
  private ArgumentAcceptingOptionSpec<String> jdbcUrlOpt;
  private ArgumentAcceptingOptionSpec<String> jdbcUsernameOpt;
  private ArgumentAcceptingOptionSpec<String> jdbcPasswordOpt;
  private ArgumentAcceptingOptionSpec<String> mailOpt;
  private ArgumentAcceptingOptionSpec<String> smsOpt;

  private Map<ArgumentAcceptingOptionSpec, String> map;

  private Properties properties;
  private OptionSet set;

  public boolean help;
  public boolean version;

  public String xmppServer;
  public String xmppUsername;
  public String xmppPassword;
  public String jdbcDriver;
  public String jdbcUrl;
  public String jdbcUsername;
  public String jdbcPassword;
  public boolean mail;
  public boolean sms;

  private Options () {
    map = new HashMap<ArgumentAcceptingOptionSpec, String> ();
    makeParser ();
  }

  private void makeParser () {
    parser = new OptionParser () {
	{
	  acceptsAll (asList ("h", "?", "help"), "display this help and exit");
	  acceptsAll (asList ("V", "version"), "output version information and exit");

	  confOpt = acceptsAll (asList ("c", "conf"), "configuration file")
	    .withRequiredArg ().ofType (File.class).defaultsTo (new File ("config.xml"));

	  map.put (xmppServerOpt = acceptsAll (asList ("xmpp.server"), "XMPP server name")
		   .withRequiredArg (),
		   "xmpp.server");

	  map.put (xmppUsernameOpt = acceptsAll (asList ("xmpp.username"), "XMPP user name")
		   .withRequiredArg (),
		   "xmpp.username");

	  map.put (xmppPasswordOpt = acceptsAll (asList ("xmpp.password"), "XMPP password")
		   .withRequiredArg (),
		   "xmpp.password");

	  map.put (jdbcDriverOpt = acceptsAll (asList ("jdbc.driver"), "database driver")
		   .withRequiredArg (),
		   "jdbc.driver");

	  map.put (jdbcUrlOpt = acceptsAll (asList ("jdbc.url"), "database connection")
		   .withRequiredArg (),
		   "jdbc.url");

	  map.put (jdbcUsernameOpt = acceptsAll (asList ("jdbc.username"), "database user")
		   .withRequiredArg (),
		   "jdbc.username");

	  map.put (jdbcPasswordOpt = acceptsAll (asList ("jdbc.password"), "database password")
		   .withRequiredArg (),
		   "jdbc.password");

	  /* TODO: this should split into mail reception and sending as well as for sms */
	  map.put (mailOpt = acceptsAll (asList ("mail"), "enable mail reception")
		   .withOptionalArg (),
		   "mail");

	  map.put (smsOpt = acceptsAll (asList ("sms"), "enable sms reception")
		   .withOptionalArg (),
		   "sms");
	}
      };
  }

  private String set (ArgumentAcceptingOptionSpec<String> spec) {
    String name = map.get (spec);
    if (!set.has (spec)) {
      String value = properties.getProperty (name);
      spec.defaultsTo (value);
    }
    String result = set.valueOf (spec);
    logger.trace (name + " = " + result);
    return result;
  }

  private void parseArguments (String args[]) throws Exception {
    set = parser.parse (args);

    FileInputStream file = null;
    try {
      file = new FileInputStream (confOpt.value (set));
      properties = new Properties ();
      properties.loadFromXML (file);
    }
    catch (Exception e) {
      logger.error ("couldn't load configuration file: " + e);
      throw e;
    }
    finally {
      if (file != null) file.close ();
    }

    help = set.has ("help");
    version = set.has ("version");

    if (help || version) return;

    xmppServer = set (xmppServerOpt);
    xmppUsername = set (xmppUsernameOpt);
    xmppPassword = set (xmppPasswordOpt);
      
    jdbcDriver = set (jdbcDriverOpt);
    jdbcUrl = set (jdbcUrlOpt);
    jdbcUsername = set (jdbcUsernameOpt);
    jdbcPassword = set (jdbcPasswordOpt);

    mail = Boolean.valueOf (set (mailOpt));
    sms = Boolean.valueOf (set (smsOpt));
  }

  /**
   * Parses the command line arguments and constructs the singleton of
   * this class.  Returns early (i.e. doesn't parse the configuration
   * file) if <code>"help"</code> or <code>"version"</code> parameters
   * were used.
   */
  public static Options parse (String args[]) throws Exception {
    instance = new Options ();
    instance.parseArguments (args);
    return instance;
  }

  /**
   * Returns the singleton of this class.  If you call this before
   * {@link #parse} was called, you will get null as a result.
   */
  public static Options getInstance () {
    return instance;
  }
}
