// XMLIdException.java - An exception class for xml:id errors
//
// $Id: XMLIdException.java,v 1.1 2004/11/18 19:07:36 ndw Exp $

package com.nwalsh.xmlidfilter;

import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;

/**
 * <p>Encapsulate an xml:id error or warning.</p>
 *
 * <blockquote>
 * <p>Copyright &copy; 2004 Norman Walsh.</p>
 * <p>This program is free software; you can redistribute it and/or
 * modify it in any way shape or form without limitation.
 * It is distributed WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.</p>
 * </blockquote>
 *
 * <p>This exception is a subclass of {@link org.xml.sax.SAXParseException
 * SAXParseException}. The only motivation for this exception class was
 * to provide an easy way for ErrorHandlers to identify xml:id errors.</p>
 *
 * @author Norman Walsh
 * <a href="mailto:ndw@nwalsh.com">ndw@nwalsh.com</a>
 * @version 0.9
 * @see XMLIdFilter
 */
public class XMLIdException extends SAXParseException {
  /**
   * Create a new XMLIdException from a message and a Locator.
   *
   * @param message The error or warning message.
   * @param locator The locator object for the error or warning (may be
   *        null).
   * @see org.xml.sax.Locator
   */
  public XMLIdException (String message, Locator locator) {
    super(message,locator);
  }
}
