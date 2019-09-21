package name.vladykin.haveibeenpwned;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class SortedFile implements Closeable {

    // TODO: encoding

    private final RandomAccessFile raf;
    private final Line firstLine;
    private final Line lastLine;

    public SortedFile(Path file) throws IOException {
        this.raf = new RandomAccessFile(file.toFile(), "r");
        this.firstLine = readLineAt(0);
        this.lastLine = readLineAt(raf.length() - 1);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    public Optional<Line> binarySearch(String value, Function<String, String> extractor, Comparator<String> comparator) throws IOException {
        Line windowStart = firstLine;
        Line windowEnd = lastLine;
        while (isBefore(windowStart, windowEnd)) {
            Line windowMiddle = readLineAt(middleOffset(windowStart, windowEnd));
            String middleValue = extractor.apply(windowMiddle.text());
            int compareResult = comparator.compare(value, middleValue);
            if (compareResult > 0) {
                windowStart = readLineAt(windowMiddle.endOffset() + 1);
            } else {
                windowEnd = windowMiddle;
            }
        }

        String startValue = extractor.apply(windowStart.text());
        return comparator.compare(value, startValue) == 0
                ? Optional.of(windowStart)
                : Optional.empty();
    }

    private boolean isBefore(Line a, Line b) {
        return a.endOffset() <= b.startOffset();
    }



    private long middleOffset(Line start, Line end) {
        return start.endOffset() + (end.startOffset() - start.endOffset()) / 2;
    }



    private Line readLineAt(long offset) throws IOException {
        seekToStartOfLine(offset);
        long start = raf.getFilePointer();
        String line = raf.readLine();
        if (line == null) {
            throw new IllegalStateException("readLine returned null after seekToLineStart, impossible!");
        } else {
            long end = raf.getFilePointer();
            return new Line(start, end, line);
        }
    }

    private void seekToStartOfLine(long offset) throws IOException {
        raf.seek(offset);
        int length = 0;
        while (offset > 0) {
            length++;
            offset--;
            raf.seek(offset);
            int c = raf.read();
            if (length > 1 && c == '\n') {
                return;
            }
        }
        raf.seek(offset);
    }


    public static final class Line {
        private final long startOffset;
        private final long endOffset;
        private final String text;

        // package-private for tests
        Line(long startOffset, long endOffset, String text) {
            if (startOffset < 0) throw new IllegalArgumentException("got startOffset < 0");
            if (endOffset < 0) throw new IllegalArgumentException("got endOffset < 0");
            if (endOffset <= startOffset) throw new IllegalArgumentException("got endOffset <= startOffset");
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.text = Objects.requireNonNull(text, "text");
        }

        public long startOffset() {
            return startOffset;
        }

        public long endOffset() {
            return endOffset;
        }

        public String text() {
            return text;
        }

        @Override
        public String toString() {
            return "Line{" + "startOffset=" + startOffset +
                    ", endOffset=" + endOffset +
                    ", text='" + text + "\'}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Line line = (Line) o;
            return startOffset == line.startOffset &&
                    endOffset == line.endOffset &&
                    text.equals(line.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startOffset, endOffset, text);
        }
    }
}
