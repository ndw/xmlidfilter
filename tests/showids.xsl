<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                exclude-result-prefixes="saxon"
                version="1.0">

  <xsl:output method="text"/>

  <xsl:template match="*">
    <xsl:for-each select="@*">
      <xsl:choose>
	<xsl:when test="id(.)">
	  <xsl:value-of select="."/>
	  <xsl:text> is an ID.</xsl:text>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="."/>
	  <xsl:text> is NOT an ID.</xsl:text>
	</xsl:otherwise>
      </xsl:choose>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="comment()|processing-instruction()|text()">
    <!-- nop -->
  </xsl:template>
</xsl:stylesheet>
