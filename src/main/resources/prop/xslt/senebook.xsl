<?xml version="1.0" encoding="UTF-8"?>

<!-- Date: 13/110/2012 -->
<!--  Modifica dopo XManager -->
<xsl:stylesheet xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="xhtml xsl">

    <xsl:output method="xml" omit-xml-declaration="no"
                media-type="text/xml" indent="yes" encoding="UTF-8"
                doctype-public="-//W3C//DTD XHTML 1.1//EN"
                doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"/>

    <!-- conservare solo i div class=sxSmall -->
    <!-- conservare body senza attributi -->

    <!-- innesco dalla radice -->
    <xsl:template match="/">
        <!-- <xsl:message>root</xsl:message>   -->
        <xsl:apply-templates select="xhtml:html"/>
    </xsl:template>

    <!-- processa l'html -->
    <xsl:template match="xhtml:html">
        <xsl:element name="html">
            <xsl:attribute name="xml:lang">it</xsl:attribute>
            <xsl:apply-templates select="xhtml:head|xhtml:body"/>
        </xsl:element>
        <!--  <xsl:message>xhtml:html</xsl:message>  -->
    </xsl:template>

    <!-- processa l'head -->
    <xsl:template match="xhtml:head">
        <xsl:element name="head">
            <xsl:apply-templates select="xhtml:meta"/>
            <xsl:element name="title">
                <xsl:value-of select="xhtml:title"/>
            </xsl:element>
            <link href="%stile.css%" rel="stylesheet" type="text/css"/>
            <xsl:apply-templates select="xhtml:script"/>
        </xsl:element>
        <!--  <xsl:message>xhtml:head</xsl:message>  -->
    </xsl:template>

    <!-- ricopia il body -->
    <xsl:template match="xhtml:body">
        <xsl:element name="body">
            <xsl:apply-templates select="descendant::xhtml:div[contains(@class,'xmanager_content')]"/>
        </xsl:element>
        <!--  <xsl:message>xhtml:body</xsl:message>  -->
    </xsl:template>

    <!-- rimuove gli eventuali script -->
    <xsl:template match="xhtml:script">
        <!-- <xsl:message>xhtml:script</xsl:message>  -->
    </xsl:template>

    <!-- rimuove tutti i meta presenti -->
    <xsl:template match="xhtml:meta">
        <!--  <xsl:message>xhtml:meta</xsl:message>  -->
    </xsl:template>

    <!-- identita -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
        <!-- <xsl:message>identita</xsl:message>  -->
    </xsl:template>

    <!-- copia il contenuto di div class=sxSmall -->
    <xsl:template match="xhtml:div[contains(@class,'xmanager_content')]">
        <xsl:element name="div">
            <xsl:attribute name="class">
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </xsl:element>
        <!--  <xsl:message>xhtml:div</xsl:message>  -->
    </xsl:template>

</xsl:stylesheet> 
