// XMLIdReader.java - An XMLReader that performs xml:id processing.

package com.nwalsh.xmlidfilter;

import com.nwalsh.xmlidfilter.XMLIdFilter;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * <p>A SAX2 XMLReader that performs simple xml:id processing.</p>
 *
 * <blockquote>
 * <p>Copyright &copy; 2004 Norman Walsh.</p>
 * <p>This program is free software; you can redistribute it and/or
 * modify it in any way shape or form without limitation.
 * It is distributed WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.</p>
 * </blockquote>
 *
 * <p>This class implements a SAX2 XMLReader that performs simple xml:id
 * processing.</p>
 *
 * <p>This class can be used, for example as a command line argument
 * to an XSLT processor, to initialize a parser that performs xml:id
 * processing. If there's a better way to do this than by making
 * a little extension to the filter, I don't know what it is.
 * I'm prepared to be a little embarrassed when someone tells me :-)</p>
 *
 * @author Norman Walsh
 * <a href="mailto:ndw@nwalsh.com">ndw@nwalsh.com</a>
 *
 * @version 0.9
 */

public class XMLIdReader extends XMLIdFilter {
  /**
   * <p>Construct a new reader from the JAXP factory.</p>
   *
   * <p>In order to do its job, an XMLIdReader must, in fact, be
   * an XMLFilter. So the only difference between this code and the filter
   * code is that a zero-argument constructor builds a new reader.</p>
   */
  public XMLIdReader() {
    super();
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);

    try {
      SAXParser parser = spf.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      setParent(reader);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
