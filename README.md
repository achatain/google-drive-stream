Google Drive Stream
=
[https://github.com/achatain/google-drive-stream](https://github.com/achatain/google-drive-stream)

Provides Java Streams for the Google Drive API :sparkles:

```java
Drive client = new Drive(...);

new GoogleDriveStream(client)
        .files()
        .filter(file -> file.getSize() > 4096L)
        .forEach(file -> log.info(file.getName()));
```
