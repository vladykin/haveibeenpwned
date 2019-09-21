package name.vladykin.haveibeenpwned;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class SortedFileTest {

    @TempDir
    static Path dir;

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("testCases")
    void binarySearchTest(String fileContent, String textToSearch, SortedFile.Line expectedResult) throws IOException {
        Path file = dir.resolve("foo.txt");
        Files.write(file, fileContent.getBytes());
        SortedFile sortedFile = new SortedFile(file);
        Optional<SortedFile.Line> found = sortedFile.binarySearch(textToSearch, Function.identity(), Comparator.naturalOrder());
        assertEquals(expectedResult, found.orElse(null));
    }

    private static Object[][] testCases() {
        return new Object[][] {
                {"a\nb\nc\n", "a", new SortedFile.Line(0, 2, "a")},
                {"a\nb\nc\n", "b", new SortedFile.Line(2, 4, "b")},
                {"a\nb\nc\n", "c", new SortedFile.Line(4, 6, "c")},
                {"a\nb\nc\n", "d", null}
        };
    }
}
