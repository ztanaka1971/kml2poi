# kml2poi
Convert google map KML to SANYO navigation system's POI format

(1) What's this ?

This is g very simple Groovy script to convert Google Map's KML to POI. This is only tested with HONDA GATHERS VXM-108CS (OEM product of SANYO Gorilla). 

(2) Tested environment

This script is only tested with following environment, but it should work this other environment as far as Groovy 1.8.1 or later works.

Windows 7 32bit
Groovy 1.8.1
Java(TM) SE Runtime Environment (build 1.6.0_26-b03)

(3) Usage

1. Install Groovy.
2. Convert KMV with following command. Converted XML will be output to standard output. Redirect the standard output to file for saving.

　　groovy kml2poi.groovy <KML File 1> <KML File 2>, ....

Only simple error handling is implemented. So if you encountered exception, please check source code to find out what's wrong.

NOTE: Filename should be in English. Don't use DBCS (Chinese character).

(4) What this script does

1. Convert latitudinal gradient: degree to hour:minute:second
2. Conversion of geodetic longitude: KML --> TOKYO geodetic longitude
3. Copy following information.
    - Name of map
    - Name of place
    - Latitudinal gradient
    - Created date & time (will be current time)

(5) Special thanks

I exploited following program for conversion of geodetic longitude.

WGS2TKY2.pl
http://homepage3.nifty.com/Nowral/index.html



