package com.github.stengerh.simplepdfwriter;

import java.io.IOException;
import java.time.OffsetDateTime;


public class Main {
    public static void main(String[] args) throws IOException {
        try (SimplePDFWriter pdfWriter = SimplePDFWriter.builder()
                .author(System.getProperty("user.name"))
                .producer(SimplePDFWriter.class.getSimpleName())
                .creationDate(OffsetDateTime.now())
                .build(System.out)) {
            for (String arg : args) {
                pdfWriter.println(arg);
            }
        }
    }
}
