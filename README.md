Android Strings XML <-> CSV converter

Project is made at IntelliJ 2019.

**Features**
Merges all string.xml files into one csv file.
Assumed that file is edited, exported to csv, then converted back to language-specific files at Android project resources.

**Usage**
To create jar, launch
```sh ./_make.sh```

Convert xml strings to csv:
```
java -jar ./kotlin/convertAndroidStrings.jar csv <Project path>/<module name>/src/main/res <path where to save csv>
```

Convert csv to xml strings:
```
java -jar ./kotlin/convertAndroidStrings.jar xml <Project path>/<module name>/src/main/res <path where csv is>
```
