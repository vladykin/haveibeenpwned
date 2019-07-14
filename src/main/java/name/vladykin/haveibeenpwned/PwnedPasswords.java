package name.vladykin.haveibeenpwned;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

public final class PwnedPasswords implements Closeable {

    private final SortedFile file;

    public PwnedPasswords(Path path) throws IOException {
        this.file = new SortedFile(path);
    }

    public boolean isPwned(Password password) throws IOException {
        return file.binarySearch(
                password.sha1(),
                PwnedPasswords::extractHash,
                String.CASE_INSENSITIVE_ORDER).isPresent();
    }

    @Override
    public void close() throws IOException {
        file.close();
    }


    private static String extractHash(String s) {
        int colonOffset = s.indexOf(':');
        return colonOffset < 0 ? s : s.substring(0, colonOffset);
    };
}
