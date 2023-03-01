# CapLockHook

A small application to show a user visually what state there caps lock key is in

## Install

### Windows

Ensure you have [NSIS](https://nsis.sourceforge.io/Download) installed on your machine
and that you configure the path to the `makensis.exe` in the pom.xml

```
<execution>
    <id>compile-nsis-script</id>
    <phase>package</phase>
    <goals>
        <goal>exec</goal>
    </goals>
    <configuration>
        <executable>C:\Program Files (x86)\NSIS\makensis.exe</executable>
    </configuration>
</execution>
```

Ensure you have the [SignTool](https://learn.microsoft.com/en-us/windows/win32/seccrypto/signtool) installed to code sign the exe
and that you configure the path to the `signtool.exe` in the pom.xml

```
<execution>
    <id>code-sign</id>
    <phase>package</phase>
    <goals>
        <goal>exec</goal>
    </goals>
    <configuration>
        <executable>C:\Program Files (x86)\Windows Kits\10\bin\10.0.22621.0\x64\signtool.exe</executable>
    </configuration>
</execution>
```

Then run maven package 
```
mvn clean package
```

## Metrics

Metrics are captured for this app, like keypress events and mouseclick
you can view metrics [here](https://analytics.google.com/analytics/web/#/realtime/rt-event/a98191047w355838565p285581166/)
