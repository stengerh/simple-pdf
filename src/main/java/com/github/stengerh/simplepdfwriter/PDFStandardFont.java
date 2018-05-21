package com.github.stengerh.simplepdfwriter;

public enum PDFStandardFont {
    TIMES_ROMAN("Times-Roman"),
    TIMES_BOLD("Times-Bold"),
    TIMES_ITALIC("Times-Italic"),
    TIMES_BOLD_ITALIC("Times-BoldItalic"),
    HELVETICA("Helvetica"),
    HELVETICA_BOLD("Helvetica-Bold"),
    HELVETICA_OBLIQUE("Helvetica-Oblique"),
    HELVETICA_BOLD_OBLIQUE("Helvetica-BoldOblique"),
    COURIER("Courier"),
    COURIER_BOLD("Courier-Bold"),
    COURIER_OBLIQUE("Courier-Oblique"),
    COURIER_BOLD_OBLIQUE("Courier-BoldOblique"),
    SYMBOL("Symbol"),
    ZAPF_DINGBATS("ZapfDingbats");

    private final String postScriptName;

    PDFStandardFont(String postScriptName) {
        this.postScriptName = postScriptName;
    }

    public String getPostScriptName() {
        return postScriptName;
    }
}
