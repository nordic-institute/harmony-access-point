<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" indent="yes"/>

    <!-- This is external stylesheet parameter set in pom.xml plugin configuration
    to include the licenses in the notice file. The value of this parameter is a
    comma separated list of licenses as exampled: Apache-2.0,MIT.
    Make sure to include the license files in the license/third-party-licenses/${license}.txt
    -->
    <xsl:param name="IncludeLicenses"/>

    <!-- This is a variable to store the newline character -->
    <xsl:variable name='newline'><xsl:text>
</xsl:text>
    </xsl:variable>
    <!-- This is a variable to store the URI of the licenses folder -->
    <xsl:variable name="licensesFolderURI"
                  select="resolve-uri('.',base-uri())"/>

    <!--Main template to match the root element of the XML file -->
    <xsl:template match="/licenseSummary">
        <!-- add the Copyright of the application -->
        <xsl:text>Copyright 2017, 2024 European Union

Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
You may not use this work except in compliance with the Licence.
You may obtain a copy of the Licence at:

[https://joinup.ec.europa.eu/software/page/eupl](https://joinup.ec.europa.eu/software/page/eupl)

Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Licence for the specific language governing permissions and limitations under the Licence.

This product includes dynamically linked software developed by third parties which is provided under their respective licences:
</xsl:text>
        <!-- apply the templates to the dependencies -->
        <xsl:apply-templates select="dependencies/dependency"/>
        <!-- include the licenses in the notice file -->
        <xsl:call-template name="allLicenses" />
    </xsl:template>


<!-- This template will be called with a list of dependencies to be included -->
    <xsl:template match="dependency">
        <xsl:value-of
                select="concat( $newline,$newline,
                '***', groupId, ':', artifactId, ':', version, '*** ',
                $newline)"/>

        <xsl:apply-templates select="licenses/license"/>
        <xsl:if test="not(inceptionYear)">  - Copyright (c) 2024</xsl:if>
        <xsl:apply-templates select="inceptionYear"/>
        <xsl:apply-templates select="organization"/>
        <xsl:apply-templates select="developers/developer"/>
    </xsl:template>

<!-- This template will be called with a list of licenses to be included -->
    <xsl:template match="license">
        <xsl:value-of select="concat(
        '- License: ', name, $newline,
        '- Url: ', url, $newline,
        '- File: license/third-party-licenses/', name, '.txt', $newline,
        '- Copyright:',$newline
        )"/>
        <xsl:apply-templates select="inceptionYear"/>
    </xsl:template>
    <xsl:template match="inceptionYear">
        <xsl:value-of select="concat(
        '  - Copyright (c) ', . , ' - 2024'
        )"/>
    </xsl:template>
    <!-- This template will be called with a list of organizations to be included -->
    <xsl:template match="organization">
        <xsl:value-of select="concat(
        ' ', name, ' (', url , ')'
        )"/>
    </xsl:template>
    <!-- This template will be called with a list of developers to be included -->
    <xsl:template match="developer">
        <xsl:value-of select="concat(
        ', ', name
        )"/>
    </xsl:template>

    <!-- This template will be called with a list of licenses to be included
    in the notice file -->
    <xsl:template name="allLicenses">
        <xsl:variable name="tokenizedSample"
                      select="tokenize($IncludeLicenses,',')"/>
        <xsl:for-each select="$tokenizedSample">
            <!-- start license block -->
            <xsl:value-of select="concat( $newline, $newline,
            '# License: ',  current()
            ,$newline)"/>

            <!-- include the license file -->
            <xsl:value-of select="concat($newline,'```',$newline)"/>
            <xsl:copy-of select="unparsed-text(
    concat($licensesFolderURI, '../../license/third-party-licenses/', current(), '.txt')
    )"/>
            <!-- end license block -->
            <xsl:value-of select="concat($newline,'```', $newline,$newline)"/>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
