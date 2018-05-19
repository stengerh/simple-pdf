# Simple PDF Writer

A simple Java library for creating simple PDFs.

[![Build Status](https://travis-ci.org/stengerh/simple-pdf.svg?branch=master)](https://travis-ci.org/stengerh/simple-pdf) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.stengerh.simplepdf%3Asimple-pdf&metric=alert_status)](https://sonarcloud.io/dashboard/index/com.github.stengerh.simplepdf:simple-pdf)

## Features

* Single page with multiple lines of text
  * No automatic line breaks though
  * Page layout s A4 portrait
* Font is Times-Roman 18pt
* Document information metadata
* ISO/IEC 8859-1 encoding for printed text and metadata
 
 ## Usage
 
 ````.java
import com.github.stengerh.simplepdf.SimplePDFWriter;

...

OutputStream out = ...;
try (SimplePDFWriter pdfWriter = SimplePDFWriter.builder()
        .title("Simple PDF")
        .author("Me")
        .creationDate(OffsetDateTime.now())
        .build(out)) {
    pdfWriter.println("Hello world!");
}
````
 
 ## Requirements
 
 * Java 8
 * Runtime dependencies: none
 * Test dependencies:
   * JUnit 5
 
 ## Download
 
 See Github releases.
 
 No Maven repository available yet.
 
 ## Links
 
 * [PDF Reference](https://www.adobe.com/devnet/pdf/pdf_reference.html)
 * [Hand-coded PDF tutorial](http://brendanzagaeski.appspot.com/0005.html)
 