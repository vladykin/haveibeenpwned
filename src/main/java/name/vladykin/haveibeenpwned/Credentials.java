package name.vladykin.haveibeenpwned;

public final class Credentials {

    private final String displayName;
    private final Password password;

    public Credentials(String displayName, Password password) {
        this.displayName = displayName;
        this.password = password;
    }

    public String displayName() {
        return displayName;
    }

    public Password password() {
        return password;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
