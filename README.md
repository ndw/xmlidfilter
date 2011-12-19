# XMLIdFilter: An XMLFilter for xml:id Processing

The XMLIdFilter class is a SAX XMLFilter that supports
[xml:id Version 1.0](http://www.w3.org/TR/xml-id/).

This filter is a complete implementation of xml:id within the
constraints imposed by SAX and the by the caller.

> Classes and Documentation Copyright &#169; 2004, 2011 Norman Walsh.
>
> This program and its associated documentation are free software;
> you can redistribute it and/or
> modify it in any way shape or form without limitation.
> It is distributed WITHOUT ANY WARRANTY; without even the implied
> warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This documentation is for Version 1.0 of the classes, which implements
the 9 September 2005 Recommendation of xml:id.

## Requirements

* Java JDK 1.4 or later.
* SAX 2.0 or later. If SAX 2.0.1 or later is available, additional attribute type tests will be performed.
* Xerces 2.0 or later for the `org.apache.xerces.util.XMLChar` and `org.apache.xerces.util.XML11Char` classes.

## Limitations

* If the SAX `Attributes` does not support the `isDeclared()` method, the filter cannot distinguish between an undeclared xml:id attribute and an xml:id attribute erroneously declared as CDATA.
* If the SAX `Locator` used does not support the `getXMLVersion()` method, the filter assumes that all xml:id values must be valid XML 1.0 NCNames. (If the method is available, it tests for valid XML 1.1 NCNames when XML 1.1 is used.)
* The filter uses a hash to keep track of ID values that have been seen. If the caller selects a limited hash size, then duplicate ID values may not be detected.

Errors are reported by calling the SAX XMLFilterImpl's
`error()` method with an XMLIdException.

## Notes

* This code is a lightly edited fork of the
  [xmlidfilter](http://java.net/projects/xmlidfilter/) project at
  [Java.net](http://java.net/).
* Relies on `org.apache.xerces.util.XMLChar` and `XML11Char` to test
  the validity of NCNames.
* The Xerces `Locator` implements `getXMLVersion()` without claiming
  to implement the `Locator2` interface, so we go looking for that
  method by reflection.
* If an xml:id attribute is promoted, we have to construct a new set
  of attributes for the element. This is accomplished by constructing
  an instance of `org.xml.sax.helpers.AttributesImpl` directly. This
  means that a subclass that extends `startElement()` will not see the
  results of calling `super().startElement()` reflected in the atts
  passed to it. Is there a better way?
* If the `XMLChar` or `XML11Char` classes aren't available at runtime,
  the filter calls `warning()` once and then proceeds, skipping most
  of the NCName tests; it performs only a very crude test over the
  ASCII range.

## Usage

For detailed usage, consult the API documentation.

### Testing

The distribution includes a test class, `XMLIdTest` that
can be used to try out the filter. Make sure that the classes from
this distribution, SAX, and Xerces are on your class path, then run:

    java com.nwalsh.xmlidfilter.apps.XMLIdTest [options] yourfile.xml

That will parse *yourfile.xml* and report any xml:id errors
that occur as well as the resulting types of all attributes.

#### Options

* `-v` or `-V`: Disable (-v) or enable (-V) validation. False by default.
* `-n` or `-N`: Disable (-n) or enable (-N) namespace awareness. True by default.
* `-w` or `-W`: Disable (-w) or enable (-W) xml:id warnings. True by default.
* `-c` *classname*: Use *classname* as the underlying reader class. If not specified,
the `SAXParserFactory` mechanism will be used to get a parser.

#### Test Cases

A set of test cases is included in the distribution.

### With Another Processor

You can put these classes to work by using the
`com.nwalsh.xmlidfilter.XMLIdReader`. For example,
[Saxon](http://www.saxonica.com/) supports a `-x` command line
argument to specify the class that should be used to read source
documents. If you specify `com.nwalsh.xmlidfilter.XMLIdReader`, the
transformation will be xml:id aware.
