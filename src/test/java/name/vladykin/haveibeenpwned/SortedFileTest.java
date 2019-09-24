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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class SortedFileTest {

    @TempDir
    static Path dir;

    @ParameterizedTest(name = "{index} {0} {1}")
    @MethodSource("testCases")
    void successfulSearch(List<String> lines, LineSeparator lineSeparator) throws IOException {
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

    @ParameterizedTest(name = "{index} {0} {1} {2}")
    @MethodSource("negativeCases")
    void failedSearch(List<String> lines, LineSeparator lineSeparator, List<String> toSearch) throws IOException {
        Path file = createTestFile(lines, lineSeparator);
        SortedFile sortedFile = new SortedFile(file);

        for (String textToSearch : toSearch) {
            Optional<SortedFile.Line> found = sortedFile.binarySearch(textToSearch);
            assertNull(found.orElse(null));
        }
    }



    private Path createTestFile(List<String> lines, LineSeparator lineSeparator) throws IOException {
        Path file = dir.resolve("foo.txt");
        String linesJoined = lines.stream().map(s -> s + lineSeparator.value()).collect(Collectors.joining(""));
        Files.write(file, linesJoined.getBytes(StandardCharsets.US_ASCII));
        return file;
    }

    private static Stream<Arguments> testCases() {
        List<List<String>> lineContents = asList(
                singletonList("foobar"),
                singletonList("a"),
                asList("a", "b"),
                asList("a", "b", "c"),
                asList("a", "b", "c", "d"),
                asList("a", "aa"),
                asList("a", "aa", "aaa"),
                asList("a", "aa", "aaa", "aaaa"),
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

    private static Stream<Arguments> negativeCases() {
        List<List<List<String>>> lineContents = asList(
                asList(emptyList(), asList("a", "foobar", "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz")),
                asList(singletonList("foobar"), asList("foo", "bar", "fooba", "oobar", "zz")),
                asList(singletonList("a"), asList("aa", "aaa", "b", "0")),
                asList(asList("a", "b"), asList("aa", "aaa", "B", "c", "0")),
                asList(asList("a", "b", "c"), asList("aa", "aaa", "B", "bb", "C", "d", "0")),
                asList(asList("a", "b", "c", "d"), asList("aa", "aaa", "B", "bb", "C", "dd", "0")),
                asList(asList("a", "aa"), asList("aaa", "AA", "AAA", "bb")),
                asList(asList("a", "aa", "aaa"), asList("aaaa", "AA", "AAA", "bb")),
                asList(asList("a", "aa", "aaa", "aaaa"), asList("aaaaa", "AA", "AAA", "bb"))
        );
        Stream.Builder<Arguments> streamBuilder = Stream.builder();
        for (List<List<String>> lineContent : lineContents) {
            for (LineSeparator lineSeparator : LineSeparator.values()) {
                streamBuilder.add(Arguments.of(lineContent.get(0), lineSeparator, lineContent.get(1)));
            }
        }
        return streamBuilder.build();
    }

    private static List<String> manyShortLines() {
        return IntStream.range(0, 10000).mapToObj(i -> String.format("%04d", i)).collect(Collectors.toList());
    }

    private static List<String> oneLongLine() {
        return singletonList(IntStream.range(0, 10000).mapToObj(i -> "x").collect(Collectors.joining("")));
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
