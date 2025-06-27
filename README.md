# google-drive-streaming-link-generator

> This Java utility converts any Google Drive sharing link into a direct video streaming link. It simplifies the process of embedding or playing videos from Google Drive by generating a clean, streamable video URL from any shared file link.

## Built With

- Java
- JUnit
- Git

## Getting Started [![Build Status](https://github.com/R4Ajeti/google-drive-streaming-link-generator/actions/workflows/build.yml/badge.svg)](https://github.com/R4Ajeti/google-drive-streaming-link-generator/actions)

**Running the project**  
Clone the repository to your local machine:

```bash
git clone https://github.com/R4Ajeti/google-drive-streaming-link-generator
cd google-drive-streaming-link-generator
```

Compile the Java class:

```bash
javac src/main/java/DriveLinkConverter.java
```

Run the program:

```bash
java -cp src/main/java DriveLinkConverter "https://drive.google.com/file/d/1AbCdEfGhIjKlMn/view?usp=sharing"
```

Replace the example URL with your actual Google Drive sharing link.

**Running tests**

Compile and run tests using JUnit (ensure JUnit is on your classpath):

```bash
javac -cp .:junit-platform-console-standalone.jar src/test/java/DriveLinkConverterTest.java
java -jar junit-platform-console-standalone.jar -cp src/test/java --scan-classpath
```

## Authors

👤 **Rinor Ajeti**

- Github: [@r4ajeti](https://github.com/r4ajeti)
- Email: [Gmail](mailto:r4ajeti@gmail.com)
- Twitter: [@r4ajeti](https://twitter.com/r4ajeti)
- Linkedin: [linkedin](https://linkedin.com/in/r4ajeti)

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

Feel free to check the [issues page](../../issues).

## Show your support

Give a ⭐️ if you like this project!

## 📝 License

This project is [MIT](https://opensource.org/license/mit/) licensed.

:¨·.·¨: :¨·.·¨: ·. ƮϦαɳk Ψөu .·
