package name.vladykin.haveibeenpwned;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java -jar haveibeenpwned.jar <path/to/pwned-passwords-sha1-ordered-by-hash> <path/to/your/credentials/file>");
            System.exit(1);
        }

        Path hashedPasswordFile = Paths.get(args[0]);
        Path credentialsFile = Paths.get(args[1]);
        try (PwnedPasswords pwned = new PwnedPasswords(hashedPasswordFile)) {
            for (Credentials myCreds : new KeepassCsv(credentialsFile).readAll()) {
                if (pwned.isPwned(myCreds.password())) {
                    System.out.printf("%s\t%s%n", myCreds.displayName(), myCreds.password().raw());
                }
            }
        }
    }
}
