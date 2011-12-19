// XMLIdFilter.java - An XMLFilter that performs xml:id processing.
//
// $Id: XMLIdFilter.java,v 1.2 2004/11/18 23:30:39 ndw Exp $

package com.nwalsh.xmlidfilter;

import java.util.HashSet;
import java.lang.reflect.Method;

import com.nwalsh.xmlidfilter.XMLIdException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.AttributesImpl;

// Use the Xerces XMLChar class to test for legal NCNames
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XML11Char;

/**
 * <p>A SAX2 XMLFilter that performs xml:id processing.</p>
 *
 * <blockquote>
 * <p>Copyright &copy; 2004 Norman Walsh.</p>
 * <p>This program is free software; you can redistribute it and/or
 * modify it in any way shape or form without limitation.
 * It is distributed WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.</p>
 * </blockquote>
 *
 * <p>This class implements a SAX2 XMLFilter that performs xml:id
 * processing. See
 * <a href="http://www.w3.org/TR/xml-id/">xml:id Version 1.0</a>
 * which is a Last Call working draft at the time of this writing.</p>
 *
 * <p>This filter is a complete implementation of xml:id within the
 * constraints imposed by SAX and the caller.</p>
 *
 * <h2>Features</h2>
 *
 * <p>The XMLIdFilter class understands the additional feature
 * "<code>http://xmlidfilter.dev.java.net/xmlid/features/show-warnings</code>".
 * This
 * feature is <code>true</code> by default. If the feature is
 * true, <code>warning()</code> will be called to identify missing
 * features: the lack of an Attributes2 interface or the Apache
 * XML{11}Char class(es).</p>
 *
 * @author Norman Walsh
 * <a href="mailto:ndw@nwalsh.com">ndw@nwalsh.com</a>
 *
 * @version 0.9
 */
public class XMLIdFilter extends XMLFilterImpl {
  /** The warning feature name. */
  protected static final String warningFeature
    = "http://xmlidfilter.dev.java.net/xmlid/features/show-warnings";

  /** Show warnings? */
  protected boolean showWarnings = true;

  /** The ID hash table. */
  protected HashSet<String> idHash = null;

  /** The maximum size of the ID hash table. */
  protected int maxHash = -1;

  /** A copy of the user's locator for calls to error(). */
  protected Locator locator = null;

  /** Have we seen enough of the document to know what version it is? */
  private boolean versionKnown = false;

  /** Are we parsing XML 1.1? */
  protected boolean is11 = false;

  /** Is the Attributes2 interface supported? */
  protected boolean attr2 = false;

  /** Did we find the Apache classes? */
  protected boolean apache = true;

  /** Construct an XML filter with an unlimited ID hash.
   */
  public XMLIdFilter() {
    super();
    maxHash = -1;
  }

  /** Construct an XML filter with an unlimited ID hash.
   *
   * @param reader The XMLReader to use.
   */
  public XMLIdFilter(XMLReader reader) {
    super(reader);
    maxHash = -1;
  }

  /** Construct an XML filter with a limited ID hash.
   *
   * <p>Constructs a filter with an ID hash that will not grow larger
   * than the specified size. If zero is specified, the hash will
   * not be constructed at all. Any negative number is treated as a
   * request for a hash of unbounded size.</p>
   *
   * <p>If no limit is specified, the ID hash table can grow to an
   * arbitrary size, containing one entry for each ID attribute encountered
   * in the document.</p>
   *
   * <p>If zero is specified, the hash will not be construct at all.</p>
   *
   * <p>Any negative number is treated as a request for a hash of unbounded
   * size.</p>
   *
   * @param parent The parent XMLReader.
   * @param maxHash The maximum size of the ID hash.
   */
  public XMLIdFilter(XMLReader parent, int maxHash) {
    super(parent);
    this.maxHash = maxHash;
  }

  /** Set the document locator, from the SAX2 API. */
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
    super.setDocumentLocator(locator);
  }

  /** Set a feature. */
  public void setFeature(String name, boolean value)
    throws SAXNotRecognizedException, SAXNotSupportedException {

    if (warningFeature.equals(name)) {
      showWarnings = value;
    } else {
      super.setFeature(name, value);
    }
  }

  /** Test the value of a feature. */
  public boolean getFeature(String name)
    throws SAXNotRecognizedException, SAXNotSupportedException {
    if (warningFeature.equals(name)) {
      return showWarnings;
    } else {
      return super.getFeature(name);
    }
  }

  /** Start of the document, from the SAX2 API. */
  public void startDocument()
    throws SAXException {
    if (maxHash != 0) {
      idHash = new HashSet<String>();
    }

    XMLReader reader = getParent();

    try {
      attr2 = reader.getFeature("http://xml.org/sax/features/use-attributes2");
    } catch (Exception e) {
      // Drat. No Attributes2 interface.
      if (showWarnings) {
	warning(new XMLIdException("xml:id: SAX feature "
				   + "http://xml.org/sax/features/use-attributes2 "
				   + "unavailable; "
				   + "skipping CDATA attribute type test",
				   locator));
      }
      attr2 = false;
    }

    versionKnown = false;
    super.startDocument();
  }

  /** End of the document, from the SAX2 API. */
  public void endDocument()
    throws SAXException {
    super.endDocument();
    idHash = null;
  }

  /** Start of an element, from the SAX2 API.
   *
   * <p>If the attributes include an xml:id attribute, it will be
   * promoted to have type ID. Note that this can result in an element
   * with multiple attributes of type ID.</p>
   *
   * <p>If an xml:id attribute is promoted, a new
   * org.xml.sax.helpers.AttributesImpl will be instantiated to
   * hold the (revised) attributes.</p>
   *
   * <p>See org.xml.sax.ContentHandler.startElement().</p>
   */
  @Override
  public void startElement(String uri, String localName,
			   String qname, Attributes atts)
    throws SAXException {

    if (!versionKnown) {
      String version = null;
      if (locator != null) {
	// This is a total hack. It seems to be necessary because the
	// underlying Xerces implementation supports the getXMLVersion method
	// without implementing the Locator2 interface. What's up with that?
	try {
	  Method getXMLVersion = null;
	  getXMLVersion = locator.getClass().getMethod("getXMLVersion",
						       new Class[]{});

	  if (getXMLVersion != null) {
	    version = (String) getXMLVersion.invoke(locator, (Object[]) null);
	  }
	} catch (Exception e) {
	  // You lose. No version information for you.
	}
      }

      is11 = "1.1".equals(version);
      versionKnown = true;
    }

    int xmlIdIndex = atts.getIndex("xml:id");

    // Look for *other* attributes of type ID
    if (maxHash != 0) {
      for (int pos = 0;
	   pos < atts.getLength()
	     && (maxHash < 0 || idHash.size() < maxHash);
	   pos++) {
	if (pos != xmlIdIndex) { // xml:id attributes are handled below
	  if ("ID".equals(atts.getType(pos))) {
	    checkDupId(atts.getValue(pos));
	  }
	}
      }
    }

    if (xmlIdIndex >= 0) {
      String type = atts.getType(xmlIdIndex);
      String id = atts.getValue(xmlIdIndex);

      checkDupId(id);

      boolean declared = false;

      if (attr2) {
	// Let's see if we can find the isDeclared method, without having
	// access to org.xml.sax.ext.Attributes2 at compile time. This
	// allows the XMLIdFilter to be compiled with JDK 1.3 or 1.4 even
	// with older versions of SAX
	//
	// How inefficient is this?
	//
	try {
	  Method isDeclared = null;
	  isDeclared
	    = atts.getClass().getMethod("isDeclared",
					new Class[] {
					  Class.forName("java.lang.String") } );

	  declared
	    = (Boolean) isDeclared.invoke(atts,
              (Object) "xml:id");

	} catch (Exception e) {
	  // nop
	}
      }

      if ("ID".equals(type)) {
	// Assume the parser did all the work for us
      } else {
	// n.b. We can't always tell the difference between an undeclared
	// xml:id attribute and an xml:id attribute declared with the
	// (incorrect) type "CDATA". Oh, well.
	if (declared || !"CDATA".equals(type)) {
	  xmlIdError("invalid declared type", type);
	}

	AttributesImpl newAtts = new AttributesImpl(atts);
	xmlIdIndex = newAtts.getIndex("xml:id");

	newAtts.setType(xmlIdIndex, "ID");

	// The ID might have leading and trailing spaces, get rid of them
	id = trimSpaces(id);

	// Maybe doubled spaces?
	if (id.contains("  ")) {
	  int pos = id.indexOf("  "); // two spaces
	  while (pos >= 0) {
	    id = id.substring(0, pos+1) + trimSpaces(id.substring(pos+2));
	    pos = id.indexOf("  ");
	  }
	}

	newAtts.setValue(xmlIdIndex, id);
	checkNCName(id);

	atts = newAtts;
      }
    }

    super.startElement(uri, localName, qname, atts);
  }

  /* Remove leading and trailing spaces.
   *
   * <p>Can't use String.trim() because it fiddles with control chars too.</p>
   *
   * @param str the input string
   * @return the input string with leading and trailing space characters removed
   */
  protected String trimSpaces(String str) {
    while (str.indexOf(' ') == 0) {
      str = str.substring(1);
    }

    while (str.lastIndexOf(' ') == str.length()-1) {
      str = str.substring(0, str.length()-1);
    }

    return str;
  }

  /** Check and update the ID hash.
   *
   * <p>If the id already occurs in the hash, raise an xml:id error.
   * Otherwise, just add the id to the hash.</p>
   *
   * @param id the id to test.
   * @throws org.xml.sax.SAXException
   */
  protected void checkDupId(String id) throws SAXException {
    if (maxHash != 0) {
      if (idHash.contains(id)) {
	    xmlIdError("duplicate ID value", id);
      } else {
	    if (maxHash < 0 || idHash.size() < maxHash) {
	        idHash.add(id);
	    }
      }
    }
  }

  /** Check that the value is a legal NCName.
   *
   * <p>If the id is not an NCName, raise an xml:id error.
   *
   * @param id the id to test.
   */
  protected void checkNCName(String id) throws SAXException {
    char[] name = id.toCharArray();
    boolean ok = false;

    if (apache) {
      if (is11) {
	try {
	  ok = XML11Char.isXML11NCNameStart(name[0]);
	  for (int pos = 1; pos < name.length && ok; pos++) {
	    ok = ok && XML11Char.isXML11NCName(name[pos]);
	  }
	} catch (NoClassDefFoundError ndfe) {
	  apache = false;
	  if (showWarnings) {
	    warning(new XMLIdException("xml:id: "
				       + "org.apache.xerces.util.XML11Char "
				       + "unavailable; skipping NCName tests",
				       locator));
	  }
	  ok = isASCIIName(id);
	}
      } else {
	try {
	  ok = XMLChar.isNCNameStart(name[0]);
	  for (int pos = 1; ok && (pos < name.length); pos++) {
	    ok = ok && XMLChar.isNCName(name[pos]);
	  }
	} catch (NoClassDefFoundError ndfe) {
	  apache = false;
	  if (showWarnings) {
	    warning(new XMLIdException("xml:id: "
				       + "org.apache.xerces.util.XMLChar "
				       + "unavailable; skipping NCName tests",
				       locator));
	  }
	  ok = isASCIIName(id);
	}
      }
    } else {
      ok = isASCIIName(id);
    }

    if (!ok) {
      xmlIdError("invalid ID value", id);
    }
  }

  /** Check if the ID value is an invalid ASCII name.
   *
   * <p>This method is a fallback, it is only used if the processor
   * cannot find the appropriate Apache XML{11}Char method. It will
   * report IDs that use invalid ASCII characters, but it will not
   * report IDs that use invalid characters above the ASCII range.</p>
   *
   * @param id the id to test.
   * @return true iff the id is an ASCII name
   */
  protected boolean isASCIIName(String id) {
    char[] name = id.toCharArray();

    for (int pos = 0; pos < name.length; pos++) {
      System.err.println("pos: "+pos+": " + Integer.toHexString(name[pos]));
      if ((pos == 0 && (name[pos] >= 0x30 && name[pos] <= 0x39))
	  || name[pos] < 0x2D
	  || (name[pos] > 0x2E && name[pos] < 0x30)
	  || (name[pos] > 0x39 && name[pos] < 0x3A)
	  || (name[pos] > 0x3A && name[pos] < 0x41)
	  || (name[pos] > 0x5A && name[pos] < 0x5F)
	  || (name[pos] > 0x5F && name[pos] < 0x61)
	  || (name[pos] > 0x7A && name[pos] < 0xC0)) {
	return false;
      }
    }

    return true;
  }

  /** Format and raise an xml:id error.
   *
   * <p>This form is used when the error should include the ID value
   * (for example, if there is an invalid character in the ID or if
   * it's a duplicate).</p>
   *
   * <p>The <code>id</code> parameter is abused in the case where the
   * xml:id is of the wrong type. The incorrect type is passed instead
   * of the id.</p>
   *
   * @param msg The message
   * @param id The error value
   * @throws org.xml.sax.SAXException
   */
  protected void xmlIdError (String msg, String id) throws SAXException {
    super.warning(new XMLIdException("xml:id: "
				     + msg + ": "
				     + "\"" + id + "\"",
				     locator));
  }
}
