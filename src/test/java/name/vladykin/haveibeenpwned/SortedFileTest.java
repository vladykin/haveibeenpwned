package name.vladykin.haveibeenpwned;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SortedFileTest {

    @TempDir
    static Path dir;

    @ParameterizedTest(name = "{index} {0} {1}")
    @MethodSource("testCases")
    void binarySearchTest(List<String> lines, LineSeparator lineSeparator) throws IOException {
        Path file = createTestFile(lines, lineSeparator);
        SortedFile sortedFile = new SortedFile(file);

        int currentOffset = 0;
        for (String textToSearch : lines) {
            int expectedStart = currentOffset;
            int expectedEnd = expectedStart + textToSearch.length() + lineSeparator.value().length();
            Optional<SortedFile.Line> found = sortedFile.binarySearch(textToSearch);
            assertEquals(new SortedFile.Line(expectedStart, expectedEnd, textToSearch), found.orElse(null));
            currentOffset = expectedEnd;
        }
    }

    private Path createTestFile(List<String> lines, LineSeparator lineSeparator) throws IOException {
        Path file = dir.resolve("foo.txt");
        String linesJoined = lines.stream().map(s -> s + lineSeparator.value()).collect(Collectors.joining(""));
        Files.write(file, linesJoined.getBytes(StandardCharsets.US_ASCII));
        return file;
    }

    private static Stream<Arguments> testCases() {
        List<List<String>> lineContents = Arrays.asList(
                Collections.singletonList("foobar"),
                Collections.singletonList("a"),
                Arrays.asList("a", "b"),
                Arrays.asList("a", "b", "c"),
                Arrays.asList("a", "b", "c", "d"),
                Arrays.asList("a", "aa"),
                Arrays.asList("a", "aa", "aaa"),
                Arrays.asList("a", "aa", "aaa", "aaaa"),
                manyShortLines(),
                oneLongLine()
        );
        Stream.Builder<Arguments> streamBuilder = Stream.builder();
        for (List<String> lineContent : lineContents) {
            for (LineSeparator lineSeparator : LineSeparator.values()) {
                streamBuilder.add(Arguments.of(lineContent, lineSeparator));
            }
        }
        return streamBuilder.build();
    }

    private static List<String> manyShortLines() {
        return IntStream.range(0, 10000).mapToObj(i -> String.format("%04d", i)).collect(Collectors.toList());
    }

    private static List<String> oneLongLine() {
        return Collections.singletonList(IntStream.range(0, 10000).mapToObj(i -> "x").collect(Collectors.joining("")));
    }

    private enum LineSeparator {
        LF, CRLF;

        String value() {
            switch (this) {
                case LF: return "\n";
                case CRLF: return "\r\n";
                default: throw new IllegalStateException();
            }
        }
    }
}
