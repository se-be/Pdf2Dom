package io.github.se_be.pdf2dom;

import static io.github.se_be.pdf2dom.PDFDomTreeConfig.ignoreResource;
import static io.github.se_be.pdf2dom.TestUtils.getOutputEnabled;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

//import org.apache.commons.codec.binary.Base64;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
//import org.mabb.fontverter.woff.WoffFont;
//import org.mabb.fontverter.woff.WoffParser;

public class TestFonts
{
    private static final String EXTRACT_DIR = "font-extract-dir";

    @TempDir 
    Path folder;

    @Disabled("This test is not meaningful while FontVerter is not enabled and working with fontbox 3")
    @Test
    public void convertPdfWithBareCffFont_outputHtmlHasWoffFontInStyle() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree("/fonts/bare-cff.pdf");
        Element style = html.select("style").get(0);

        assertThat(style.outerHtml(), containsString("@font-face"));
        assertThat(style.outerHtml(), containsString("x-font-woff"));
    }

//    @Test
//    public void convertPdfWithBareCffFont_outputHtmlFontIsReadable() throws Exception
//    {
//        Document html = TestUtils.parseWithPdfDomTree("/fonts/bare-cff.pdf");
//        Element style = html.select("style").get(0);
//
//        Matcher matcher = Pattern.compile("x-font-woff;base64,([^']*)'").matcher(style.outerHtml());
//        assertTrue(matcher.find());
//
//        String base64Data = matcher.group(1);
//        byte[] fontData = Base64.decodeBase64(base64Data);
//        WoffFont font = new WoffParser().parse(fontData);
//
//        assertThat(font.getTables().size(), greaterThan(1));
//    }

    @Test
    public void convertPdfWithBareCffFont_divElementStyleIsUsingFont() throws Exception
    {
        Document html = TestUtils.parseWithPdfDomTree("/fonts/bare-cff.pdf");

        Element div = html.select("div.p").get(0);
        String divStyle = div.attr("style");

        assertThat(divStyle, containsString("font-family:"));
    }

    @Disabled("This test is not meaningful while FontVerter is not enabled and working with fontbox 3")
    @Test
    public void convertPdf_withFontExtractToDirModeSet_thenFontFaceRuleHasUrlToFile() throws Exception
    {
        Document html = convertWithFontSaveToDirMode("/fonts/bare-cff.pdf");
        Element style = html.select("style").get(0);

        assertThat(style.outerHtml(), containsString(EXTRACT_DIR + "/EKCFJL+Omsym2.woff"));
    }

    @Disabled("This test is not meaningful while FontVerter is not enabled and working with fontbox 3")
    @Test
    public void convertPdf_withFontExtractToDirModeSet_thenFontFileExists() throws Exception
    {
        convertWithFontSaveToDirMode("/fonts/bare-cff.pdf");
        File tempFontFile = new File(getFullExtractPath() + "EKCFJL+Omsym2.woff");

        assertTrue(tempFontFile.exists());
    }

    @Test
    public void convertPdf_withIgnoreFontsModeSet_thenNoFontFacesInHtml() throws Exception
    {
        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
        config.setFontHandler(ignoreResource());

        Document html  = TestUtils.parseWithPdfDomTree("/fonts/bare-cff.pdf", config);
        Element style = html.select("style").get(0);

        assertThat(style.outerHtml(), not(containsString("@font-face")));
    }

    private Document convertWithFontSaveToDirMode(String pdf) throws Exception
    {
        File fontDir = getExtractDir();

        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();
        config.setFontHandler(PDFDomTreeConfig.saveToDirectory(fontDir));

        return TestUtils.parseWithPdfDomTree(pdf, config);
    }

    private File getExtractDir() throws IOException
    {
        return getOutputEnabled() ? new File(EXTRACT_DIR) : folder.resolve(EXTRACT_DIR).toFile();
    }

    private String getFullExtractPath() throws IOException
    {
        return getOutputEnabled() ? EXTRACT_DIR + "/" : folder.resolve(EXTRACT_DIR).toAbsolutePath() + "/";
    }
}
