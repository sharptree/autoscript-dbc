# Introduction 
The *autoscript-dbc* project provides DBC support for deploying automation scripts that contain the `configScript` metadata used by the VSCode Maximo Script Deploy extension, which is found [here](https://marketplace.visualstudio.com/items?itemName=sharptree.maximo-script-deploy).

# Getting Started

## Copy Classes and script.dtd
To use the *autoscript-dbc* extension, copy the compiled classes to the `[SMP_HOME]/maximo/tools/maximo/classes` directory, for example `/opt/IBM/SMP/maximo/tools/maximo/classes` (unix) or `C:\IBM\SMP\maximo\tools\maximo\classes` (windows).  

The classes can be found in the `build/classes/java/main` folder of this project after calling the gradle assemble task or can be obtained in the zip and tar files provided under this project's GitHub Releases.

Copy the script.dtd from the project to the [SMP_HOME]/maximo/tools/maximo directory, for example `/opt/IBM/SMP/maximo/tools/maximo` (unix) or `C:\IBM\SMP\maximo\tools\maximo` (windows).

## Update Product XML
The *autoscript-dbc* relies on injecting new DBC statements into the statement processor.  To enable these new statements, add `<calloutclass>io.sharptree.maximo.dbmanage.AutoScriptExtCallout</calloutclass>` to your project's product XML as shown in the example below.  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<product>
    <name>Example Autoscript</name>
    <version>
        <major>1</major>
        <minor>0</minor>
        <modlevel>0</modlevel>
        <patch>1</patch>
        <build>20220314</build>
    </version>
    <dbmaxvarname>AUTOSCRIPTINST</dbmaxvarname>
    <dbscripts>example</dbscripts>
    <dbversion>V1000-1</dbversion>
    <lastdbversion>V1000-00</lastdbversion>
    <calloutclass>io.sharptree.maximo.dbmanage.AutoScriptExtCallout</calloutclass>
</product>
```
## Use add_update_autoscript and remove_autoscript
With the product XML file updated the `add_update_autoscript` and `remove_autoscript` statements are now available.  

The `add_update_autoscript` requires a `path` and a `language` attribute. The `path` attribute is either a relative path from the DBC script file location or an absolute file path. The `language` attribute is either the literal value, `javascript` or `python`.  

Below is an example DBC script using both a relative and absolute path.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE script SYSTEM "script.dtd">
<!--suppress CheckValidXmlInScriptTagBody -->
<script author="Jason VenHuizen" scriptname="V1000_01">
    <description>Example</description>
    <statements>
        <!-- Relative path to python script -->
        <add_update_autoscript path="example.py" language="python"/>

        <!-- Absolute path to python script -->
        <!-- /opt/src/scripts/example.js (unxi) or C:\opt\src\scripts\example.js (windows) -->
        <add_update_autoscript path="/opt/src/scripts/example.js" language="javascript"/>
    </statements>
</script>
```

> Note that it is required that the automation script source files contain the `scriptConfig` variable that is used by the VSCode Maximo Script Deploy extension. If this is not present the script will be unable to deploy. 

The `remove_autoscript` requires the `name` attribute.  This is the name of the automation script to remove from the target system.

Below is an example DBC script that removes an automation script named `EXAMPLESCRIPT`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE script SYSTEM "script.dtd">
<!--suppress CheckValidXmlInScriptTagBody -->
<script author="Jason VenHuizen" scriptname="V1000_01">
    <description>Example</description>
    <statements>
        <remove_autoscript name="EXAMPLESCRIPT"/>
    </statements>
</script>
```

# Build

## Maximo Dependencies
To build the *autoscript-dbc* project, you will need the Maximo `businessobjects.jar` and the Maximo tools classes. The `businessobjects.jar` file can be obtained by unzipping the `maximo.ear` file and copying the file.  

The Maximo tools classes are not provided as a jar file and therefore must be created.  Open a terminal (unix) or command (windows) window and navigate to the [SMP_HOME]/maximo/tools/maximo/classes folder.  Run the following command.
```shell
jar cf maximo-tools.jar *
```
Copy the `businessobjects.jar` and `maximo-tools.jar` to the project's `libs` directory.

## Gradle assembleDist
To build the project run the gradle `assembleDist` task.