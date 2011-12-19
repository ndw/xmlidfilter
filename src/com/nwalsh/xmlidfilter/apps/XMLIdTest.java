// XMLIdTest.java - A test application for XMLIdFilter
//
// $Id: XMLIdTest.java,v 1.2 2004/11/18 23:31:07 ndw Exp $

package com.nwalsh.xmlidfilter.apps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import com.nwalsh.xmlidfilter.XMLIdFilter;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.*;

/**
 * <p>A test application for XMLIdFilter.</p>
 *
 * <p>This class is just a test application for the XMLIdFilter.</p>
 *
 * @author Norman Walsh
 * <a href="mailto:ndw@nwalsh.com">ndw@nwalsh.com</a>
 *
 * @version 0.9
 */
public class XMLIdTest {
  /** The main entry point */
  public static void main (String[] args)
    throws FileNotFoundException, IOException {

    XMLIdTest f = new XMLIdTest(args);
  }

  public XMLIdTest (String[] args) {
    String  xmlfile     = null;
    String  readerClass = null;
    boolean nsAware     = true;
    boolean validating  = false;
    boolean xmlidWarnings = true;

    for (int i=0; i<args.length; i++) {
      if (args[i].equals("-v")) {
	validating = false;
	continue;
      }

      if (args[i].equals("-V")) {
	validating = true;
	continue;
      }

      if (args[i].equals("-n")) {
	nsAware = false;
	continue;
      }

      if (args[i].equals("-N")) {
	nsAware = true;
	continue;
      }

      if (args[i].equals("-w")) {
	xmlidWarnings = false;
	continue;
      }

      if (args[i].equals("-W")) {
	xmlidWarnings = true;
	continue;
      }

      if (args[i].equals("-c")) {
	++i;
	readerClass = args[i];
      }

      xmlfile = args[i];
    }

    if (xmlfile == null) {
      System.out.print("Usage: ");
      System.out.println("com.nwalsh.xmlidfilter.apps.XMLIdTest [opts] xmlfile");
      System.exit(1);
    }

    XMLReader reader = null;

    try {
      if (readerClass != null) {
	reader = (XMLReader) Class.forName(readerClass).newInstance();
      } else {
	SAXParserFactory spf = SAXParserFactory.newInstance();
	SAXParser parser = spf.newSAXParser();
	reader = parser.getXMLReader();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }

    XMLIdFilter xfilter = new XMLIdFilter(reader);
    TestFilter filter = new TestFilter(xfilter);

    try {
      filter.setFeature("http://xml.org/sax/features/namespaces", nsAware);
    } catch (SAXException e) {
      System.out.println("Failed to set feature: namespaces");
    }

    try {
      filter.setFeature("http://xml.org/sax/features/validation", validating);
    } catch (SAXException e) {
      System.out.println("Failed to set feature: validation");
    }

    try {
      filter.setFeature("http://xmlidfilter.dev.java.net/xmlid/features/show-warnings",
			xmlidWarnings);
    } catch (SAXException e) {
      System.out.println("Failed to set feature: show xml:id warnings");
    }

    XParseError xpe = new XParseError();
    filter.setErrorHandler(xpe);

    String parseType = validating ? "validating" : "well-formed";
    String nsType = nsAware ? "namespace-aware" : "namespace-ignorant";
    System.out.println("Attempting "
		       + parseType
		       + ", "
		       + nsType
		       + " parse of "
		       + xmlfile);

    Date startTime = new Date();

    try {
      filter.parse(xmlfile);
    } catch (SAXException sx) {
      System.out.println("SAX Exception: " + sx);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Date endTime = new Date();

    long millisec = endTime.getTime() - startTime.getTime();
    long secs = 0;
    long mins = 0;
    long hours = 0;

    if (millisec > 1000) {
      secs = millisec / 1000;
      millisec = millisec % 1000;
    }

    if (secs > 60) {
      mins = secs / 60;
      secs = secs % 60;
    }

    if (mins > 60) {
      hours = mins / 60;
      mins = mins % 60;
    }
  }

  private class TestFilter extends XMLFilterImpl {
    public TestFilter(XMLReader reader) {
      super(reader);
    }

    public void startElement(String uri, String localName,
			     String qname, Attributes atts)
      throws SAXException {

      System.out.println(qname + ":");

      // Oh, what the heck, make it pretty...
      int maxNameLen = 0;
      int maxTypeLen = 0;

      for (int pos = 0; pos < atts.getLength(); pos++) {
	if (atts.getQName(pos).length() > maxNameLen) {
	  maxNameLen = atts.getQName(pos).length();
	}
	if (atts.getType(pos).length() > maxTypeLen) {
	  maxTypeLen = atts.getType(pos).length();
	}
      }

      for (int pos = 0; pos < atts.getLength(); pos++) {
	System.out.print("  " + atts.getQName(pos));
	for (int len = atts.getQName(pos).length(); len < maxNameLen; len++) {
	  System.out.print(" ");
	}
	System.out.print(" " + atts.getType(pos));
	for (int len = atts.getType(pos).length(); len < maxTypeLen; len++) {
	  System.out.print(" ");
	}
	System.out.println(" \"" + atts.getValue(pos) + "\"");
      }
    }
  }
}
