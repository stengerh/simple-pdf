package com.github.stengerh.simplepdfwriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SimplePDFWriter implements AutoCloseable {
    // PDFDocEncoding is actually a superset of ISO-8859-1, but unfortunately not available in Java.
    private static final Charset PDFDocEncoding = Charset.forName("ISO-8859-1");
    private static final Charset WinAnsiEncoding = Charset.forName("windows-1252");
    private static final double A4_WIDTH_CM = 21.0;
    private static final double A4_HEIGHT_CM = 29.7;

    private final CountingOutputStream out;
    private final Map<Integer, Long> xref = new HashMap<>();
    private final long streamStart;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendLiteral("D:")
            .appendValue(ChronoField.YEAR, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter(Locale.ENGLISH);

    private boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private SimplePDFWriter(OutputStream out, Builder builder) throws IOException {
        this.out = new CountingOutputStream(out);
        this.streamStart = writePrefix(builder);
    }

    @Override
    public void close() throws IOException {
        writePostfix();
        out.close();
    }

    private long writePrefix(Builder builder) throws IOException {
        long streamStart;
        try (Writer writer = new OutputStreamWriter(this.out, PDFDocEncoding)) {
            writer.write("%PDF-1.7\n");
            writer.write("%Â¥Â±Ã«\n");

            writer.flush();
            xref.put(1, this.out.getWritten());
            writer.write("1 0 obj <<\n");
            if (isNotEmpty(builder.title)) {
                writer.write("/Title (" + builder.title + ")\n");
            }
            if (isNotEmpty(builder.author)) {
                writer.write("/Author (" + builder.author + ")\n");
            }
            if (isNotEmpty(builder.subject)) {
                writer.write("/Subject (" + builder.subject + ")\n");
            }
            if (isNotEmpty(builder.keywords)) {
                writer.write("/Keywords (" + builder.keywords + ")\n");
            }
            if (isNotEmpty(builder.creator)) {
                writer.write("/Creator (" + builder.creator + ")\n");
            }
            if (isNotEmpty(builder.producer)) {
                writer.write("/Producer (" + builder.producer + ")\n");
            }
            if (builder.creationDate != null) {
                writer.write("/CreationDate (" + formatDateTime(builder.creationDate) + ")\n");
            }
            if (builder.modificationDate != null) {
                writer.write("/ModDate (" + formatDateTime(builder.modificationDate) + ")\n");
            }
            writer.write(">>\n");
            writer.write("endobj\n");

            writer.flush();
            xref.put(2, this.out.getWritten());
            writer.write("2 0 obj <<\n");
            writer.write("/Type /Catalog\n");
            writer.write("/Pages 3 0 R\n");
            writer.write(">>\n");
            writer.write("endobj\n");

            writer.flush();
            xref.put(3, this.out.getWritten());
            writer.write("3 0 obj <<\n");
            writer.write("/Type /Pages\n");
            writer.write("/Kids [\n");
            writer.write("4 0 R\n");
            writer.write("]\n");
            writer.write("/Count 1\n");
            writer.write(">>\n");
            writer.write("endobj\n");

            writer.flush();
            xref.put(4, this.out.getWritten());
            writer.write("4 0 obj <<\n");
            writer.write("/Type /Page\n");
            writer.write("/Parent 3 0 R\n");
            writer.write("/Resources <<\n");
            writer.write("/Font <<\n");
            writer.write("/F1 <<\n");
            writer.write("/Type /Font\n");
            writer.write("/Subtype /Type1\n");
            PDFStandardFont font = (builder.font != null) ? builder.font : PDFStandardFont.TIMES_ROMAN;
            writer.write("/BaseFont /" + font.getPostScriptName() + "\n");
            writer.write("/Encoding /WinAnsiEncoding\n");
            writer.write(">>\n");
            writer.write(">>\n");
            writer.write(">>\n");
            int llx = 0;
            int lly = 0;
            int urx = Math.toIntExact(Math.round(A4_WIDTH_CM / 2.54 * 72));
            int ury = Math.toIntExact(Math.round(A4_HEIGHT_CM / 2.54 * 72));
            writer.write("/MediaBox [" + llx + " " + lly + " " + urx + " " + ury + "]\n");
            writer.write("/Contents 5 0 R\n");
            writer.write(">>\n");
            writer.write("endobj\n");

            writer.flush();
            xref.put(5, this.out.getWritten());
            writer.write("5 0 obj <<\n");
            writer.write("/Length 6 0 R\n");
            writer.write(">>\n");
            writer.write("stream\n");
            writer.flush();
            streamStart = this.out.getWritten();
            writer.write("BT\n");
            int fontSize = 18;
            writer.write("/F1 " + fontSize + " Tf\n");
            int margin = 28;
            int lineHeight = fontSize * 3 / 2;
            writer.write("" + margin + " " + (ury - margin + lineHeight - fontSize) + " Td\n");
            writer.write("" + lineHeight + " TL\n");
        }
        return streamStart;
    }

    public void println(String s) throws IOException {
        String[] lines = s.split("(\\r?\\n)|\\r");
        for (String line : lines) {
            try (Writer writer = new OutputStreamWriter(out, PDFDocEncoding)) {
                writer.write("(");
            }
            try (Writer writer = new OutputStreamWriter(out, WinAnsiEncoding)) {
                writer.write(line.replaceAll("[()\\\\]", "\\\\$0"));
            }
            try (Writer writer = new OutputStreamWriter(out, PDFDocEncoding)) {
                writer.write(") '\n");
            }
        }
    }

    private void writePostfix() throws IOException {
        try (Writer writer = new OutputStreamWriter(out, PDFDocEncoding)) {
            writer.write("ET");
            writer.flush();
            long streamEnd = out.getWritten();
            writer.write("\n");
            writer.write("endstream\n");
            writer.write("endobj\n");

            writer.flush();
            xref.put(6, out.getWritten());
            writer.write("6 0 obj\n");
            writer.write(String.format("%d\n", streamEnd - streamStart));
            writer.write("endobj\n");

            writer.flush();
            long xrefOffset = out.getWritten();
            writer.write("xref\n");
            writer.write("0 " + (1 + xref.size()) + "\n");
            writer.write("0000000000 65535 f \n");
            for (int obj = 1; obj <= xref.size(); obj += 1) {
                writer.write(String.format("%010d %05d n \n", xref.get(obj), 0));
            }
            writer.write("trailer <<\n");
            writer.write("/Root 2 0 R\n");
            writer.write("/Info 1 0 R\n");
            writer.write("/Size " + (1 + xref.size()) + "\n");
            writer.write(">>\n");
            writer.write("startxref\n");
            writer.write("" + xrefOffset + "\n");
            writer.write("%%EOF");
            writer.flush();
        }
    }

    private String formatDateTime(OffsetDateTime dateTime) {
        int offsetSeconds = dateTime.getOffset().getTotalSeconds();
        int offsetHours = Math.abs((offsetSeconds / 3600) % 100);
        int offsetSecondOfHour = Math.abs((offsetSeconds / 60) % 60);
        char sign = offsetSeconds < 0 ? '-' : '+';
        return String.format(Locale.ENGLISH, "%s%c%02d'%02d",
                dateTime.format(DATE_TIME_FORMATTER),
                sign, offsetHours, offsetSecondOfHour);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String title;
        private String author;
        private String subject;
        private String keywords;
        private String creator;
        private String producer;
        private OffsetDateTime creationDate;
        private OffsetDateTime modificationDate;
        private PDFStandardFont font;

        public SimplePDFWriter build(OutputStream out) throws IOException {
            return new SimplePDFWriter(out, this);
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public Builder creator(String creator) {
            this.creator = creator;
            return this;
        }

        public Builder producer(String producer) {
            this.producer = producer;
            return this;
        }

        public Builder creationDate(OffsetDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder modificationDate(OffsetDateTime modificationDate) {
            this.modificationDate = modificationDate;
            return this;
        }

        public Builder font(PDFStandardFont font) {
            this.font = font;
            return this;
        }
    }

    private static class CountingOutputStream extends OutputStream {

        private final OutputStream out;
        private final Object lock = new Object();
        private long written = 0;

        private CountingOutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            synchronized (lock) {
                out.write(b);
                written += 1;
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            synchronized (lock) {
                out.write(b, off, len);
                written += len;
            }
        }

        public long getWritten() {
            synchronized (lock) {
                return written;
            }
        }

    }
}



