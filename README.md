# mil-sym-java

About
-----------
mil-sym-java is a well worn set of java libraries that have been used in US Army Mission Command software for years.  In November 2013 Mission Command was given the approval to release and maintain these libraries as public open source.  Eventually work on the 2525C SEC Renderer ended and the project was retired

This is a continuation of that effort and is not currently open source until which time we get the proper approvals in place.
This library aims to support 2525D, 2525E and potentially more future versions.

[JavaDocs](https://missioncommand.github.io/javadoc/2525D/java/index.html)  
[Wiki](https://github.com/missioncommand/mil-sym-java/wiki)

The old 2525C renderer has been retired but the libraries and usage information are still available here:  
[2525C Renderer Overview](https://github.com/missioncommand/mil-sym-java/wiki/2525C-Renderer-Overview)


MIL-STD-2525
-----------
The [MIL-STD-2525] standard defines how to visualize military symbology.  This project provides support for the entire MIL-STD-2525D Change 1 and MIL-STD-2525E.

Project Structure
------------
Renderer: This is the component that can be used in Java applications to generate the entire MIL-STD-2525 symbol for both icons based symbols and geometric symbols such as tactical graphics.  Renderer relies on the jar files generated by Core.

External Libraries in use:  
[jsvg](https://github.com/weisJ/jsvg) 1.3.0 using [MIT License](https://github.com/weisJ/jsvg/blob/master/LICENSE)  
[svgSalamander](https://github.com/blackears/svgSalamander)  1.1.3 using [LGPL](https://github.com/blackears/svgSalamander/blob/master/www/license/license-lgpl.txt) or [BSD](https://github.com/blackears/svgSalamander/blob/master/www/license/license-bsd.txt)  (used for pre-processing files that are used in the renderer. Not used for rendering)  
[Geodesy](https://github.com/mgavaghan/geodesy)  1.1.3 using [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)  
  


Build:
````
./gradlew build
````

Build and install to Maven local:
````
./gradlew build publishToMavenLocal
````

  
    
