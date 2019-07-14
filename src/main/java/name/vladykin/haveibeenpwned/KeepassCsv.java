package name.vladykin.haveibeenpwned;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KeepassCsv {

    private final Path file;
    private final Charset charset;

    public KeepassCsv(Path file) {
        this(file, StandardCharsets.UTF_8);
    }

    public KeepassCsv(Path file, Charset charset) {
        this.file = Objects.requireNonNull(file, "file");
        this.charset = Objects.requireNonNull(charset, "charset");
    }

    public List<Credentials> readAll() throws IOException {
        List<Credentials> list = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(file, charset, CSVFormat.RFC4180.withFirstRecordAsHeader())) {
            for (CSVRecord rec : parser) {
                list.add(new Credentials(
                        rec.get("Group") + " " + rec.get("Title") + " " + rec.get("Username"),
                        new Password(rec.get("Password"))));
            }
        }
        return list;
    }

}
