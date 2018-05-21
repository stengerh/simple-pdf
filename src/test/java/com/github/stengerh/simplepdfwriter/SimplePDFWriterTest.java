package com.github.stengerh.simplepdfwriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertAll;

class SimplePDFWriterTest {

    private static final Charset CS = Charset.forName("UTF-8");

    private TestInfo testInfo;

    @BeforeEach
    public void setTestInfo(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    @Test
    public void testEmpty() throws IOException {
        run(pdfWriter -> {
            // Empty
        });
    }

    @Test
    public void testOneLine() throws IOException {
        run(pdfWriter -> {
            pdfWriter.println("Hello \\(world)!");
        });
    }

    @Test
    public void testTwoLines() {
        assertAll(
                () -> run(pdfWriter -> {
                    pdfWriter.println("Hello");
                    pdfWriter.println("world!");
                }),
                () -> run(pdfWriter -> {
                    pdfWriter.println("Hello\r\nworld!");
                }),
                () -> run(pdfWriter -> {
                    pdfWriter.println("Hello\nworld!");
                }),
                () -> run(pdfWriter -> {
                    pdfWriter.println("Hello\rworld!");
                })
        );
    }

    @Test
    public void testDocumentInformation() throws IOException {
        run(() -> SimplePDFWriter.builder()
                        .title("My Title")
                        .author("Despicable Me")
                        .creator("")
                        .producer(SimplePDFWriter.class.getSimpleName())
                        .creationDate(OffsetDateTime.parse("2018-05-19T11:52:23+02:00"))
                        .modificationDate(OffsetDateTime.parse("2018-05-19T11:52:23-02:15")),
                pdfWriter -> {
                    pdfWriter.println("Hello");
                    pdfWriter.println("world!");
                }
        );
    }

    @Test
    public void testHelvetica() throws IOException {
        run(() -> SimplePDFWriter.builder()
                        .font(PDFStandardFont.HELVETICA),
                pdfWriter -> {
                    pdfWriter.println("Hello world!");
                }
        );
    }

    @FunctionalInterface
    private interface SimplePDFWriterConsumer {
        void accept(SimplePDFWriter pdfWriter) throws IOException;
    }

    private void run(SimplePDFWriterConsumer pdfWriterConsumer) throws IOException {
        run(SimplePDFWriter::builder, pdfWriterConsumer);
    }

    private void run(Supplier<SimplePDFWriter.Builder> builderSupplier, SimplePDFWriterConsumer pdfWriterConsumer) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (SimplePDFWriter pdfWriter = builderSupplier.get().build(out)) {
            pdfWriterConsumer.accept(pdfWriter);
        }
        String expectedName = testInfo.getTestClass().get().getSimpleName() + "." + testInfo.getTestMethod().get().getName() + ".pdf";
        byte[] expected = getResourceAsBytes(expectedName);
        byte[] actual = out.toByteArray();
        assertBinaryEquals(expected, actual);
    }

    private void assertBinaryEquals(byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionFailedError("file contents are different", new String(expected, CS), new String(actual, CS));
        }
    }

    private byte[] getResourceAsBytes(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(name)) {
            if (in == null) {
                throw new FileNotFoundException(name);
            }
            return readAllBytes(in);
        }
    }

    private byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
