package org.javalite.activeweb;


import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

public class HTML {
    private static final HtmlCompressor compressor = new HtmlCompressor();

    static {
        compressor.setPreserveLineBreaks(false);
    }

    public static String compress(String html) {
        return compressor.compress(html);
    }
}
