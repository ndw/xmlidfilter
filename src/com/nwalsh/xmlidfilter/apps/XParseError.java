// XParseError.java - An ErrorHandler for XMLIdTest
//
// $Id: XParseError.java,v 1.2 2004/11/18 23:31:07 ndw Exp $

package com.nwalsh.xmlidfilter.apps;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * <p>An ErrorHandler for XMLIdTest.</p>
 *
 * <p>This class is just the error handler for XMLIdTest.</p>
 *
 * @author Norman Walsh
 * <a href="mailto:ndw@nwalsh.com">ndw@nwalsh.com</a>
 *
 * @version 0.9
 */
public class XParseError implements ErrorHandler {
  /** Show errors? */
  private boolean showErrors = true;

  /** Show warnings? */
  private boolean showWarnings = true;

  /** How many messages should be presented? */
  private int maxMessages = 10;

  /** The number of fatal errors seen so far. */
  private int fatalCount = 0;

  /** The number of errors seen so far. */
  private int errorCount = 0;

  /** The number of warnings seen so far. */
  private int warningCount = 0;

  /** The base URI of the running application. */
  private String baseURI = "";

  /** Constructor */
  public XParseError() {
    String dir = System.getProperty("user.dir");
    String file = "";

    if (dir.endsWith("/")) {
      file = "file:" + dir + "file";
    } else {
      file = "file:" + dir + "/" + file;
    }

    try {
      URL url = new URL(file);
      baseURI = url.toString();
    } catch (MalformedURLException mue) {
      // nop;
    }
  }

  /** Return the error count */
  public int getErrorCount() {
    return errorCount;
  }

  /** Return the fatal error count */
  public int getFatalCount() {
    return fatalCount;
  }

  /** Return the warning count */
  public int getWarningCount() {
    return warningCount;
  }

  /** Return the number of messages to display */
  public int getMaxMessages() {
    return maxMessages;
  }

  /** Set the number of messages to display */
  public void setMaxMessages(int max) {
    maxMessages = max;
  }

  /** SAX2 API */
  public void error(SAXParseException exception) {
    if (showErrors) {
      if (errorCount+warningCount < maxMessages) {
	message("Error", exception);
      }
      errorCount++;
    }
  }

  /** SAX2 API */
  public void fatalError(SAXParseException exception) {
    if (showErrors) {
      if (errorCount+warningCount < maxMessages) {
	message("Fatal error", exception);
      }
      errorCount++;
      fatalCount++;
    }
  }

  /** SAX2 API */
  public void warning(SAXParseException exception) {
    if (showWarnings) {
      if ((errorCount+warningCount < maxMessages)) {
	message("Warning", exception);
      }
      warningCount++;
    }
  }

  /** Display a message to the user */
  private void message(String type, SAXParseException exception) {
    String filename = exception.getSystemId();
    if (filename.startsWith(baseURI)) {
      filename = filename.substring(baseURI.length());
    }

    System.out.print(type
		     + ":"
		     + filename
		     + ":"
		     + exception.getLineNumber());

    if (exception.getColumnNumber() > 0) {
      System.out.print(":" + exception.getColumnNumber());
    }

    System.out.println(":" + exception.getMessage());
  }
}
