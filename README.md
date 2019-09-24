# haveibeenpwned

Small program that checks your passwords against the
[haveibeenpwned database](https://haveibeenpwned.com/).

Suitable for paranoids like me :) Instead of sending the passwords
to the internet, looks up the passwords in the database file downloaded locally.
Neither your passwords nor their hashes ever leave your computer.

Is this program better than simple grep? Yes. The reason is performance.
Grepping a single passwords against 20GB file can take seconds or minutes
even on SSD. This program uses efficient binary search and looks up hundreds
of passwords per second.

How to use:
- download file with **SHA-1 hashes ordered by hash** from
  https://haveibeenpwned.com/Passwords
- unpack it
- prepare file with your passwords (only Keepass CSV format is supported now,
  other formats may added later)
- build the program with `mvn package` command
- run it: `java -jar haveibeenpwned.jar <path/to/pwned-passwords-sha1-ordered-by-hash> <path/to/your/credentials/file>`

