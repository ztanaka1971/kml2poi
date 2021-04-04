// TAB = 4
//------------------------------------------------------------------
//             CONVERT GOOGLE KML TO SANYO POI FORMAT
//
// Supported navigation system : VXM-108CS
//------------------------------------------------------------------
import groovy.xml.MarkupBuilder
import org.custommonkey.xmlunit.*
import java.text.SimpleDateFormat

////////////////////////////////////////////////////
//                      Method
////////////////////////////////////////////////////
// 楕円体座標 -> 直交座標
def llh2xyz(b,l,h,a,e2) {
	rd  = Math.PI / 180;      // [ラジアン/度]

	b *= rd;
	l *= rd;
	sb = Math.sin(b);
	cb = Math.cos(b);
	rn = a / Math.sqrt(1-e2*sb*sb);

	x = (rn+h) * cb * Math.cos(l);
	y = (rn+h) * cb * Math.sin(l);
	z = (rn*(1-e2)+h) * sb;

	return [x, y, z];
}

// 直交座標 -> 楕円体座標
def xyz2llh(double x, double y, double z, double a, double e2) {
	rd  = Math.PI / 180;      // [ラジアン/度]

	bda = Math.sqrt(1-e2); // b/a

	p = Math.sqrt(x*x+y*y);
	t = Math.atan2(z, p*bda);
	st = Math.sin(t);
	ct = Math.cos(t);
	b = Math.atan2(z+e2*a/bda*st*st*st, p-e2*a*ct*ct*ct);
	l = Math.atan2(y, x);

	sb = Math.sin(b);
	rn = a / Math.sqrt(1-e2*sb*sb);
	h = p / Math.cos(b) - rn;

	return [b/rd, l/rd, h];
}

// WGS84 -> TOKYO
def wgs2tokyo(double b, double l, double h) {
	// 変換元
	// (WGS 84)
	double a = 6378137.0; // 6378137; // 赤道半径
	double f = 1 / 298.257223563; // 1 / 298.257223; // 扁平率
	double e2 = 2*f - f*f; // 第1離心率

	// 変換先
	// (Tokyo)
	double a_ = 6378137.0 - 739.845; // 6377397.155;
	double f_ = 1/298.257223563 - 0.000010037483; // 1 / 299.152813;
	double e2_ = 2*f_ - f_*f_;

	// 並行移動量 [m]
	// e.g. x_ = x + dx etc.

	double dx = +128; // -148;
	double dy = -481; // +507;
	double dz = -664; // +681;

	xyz = llh2xyz(b, l, h, a, e2);
	x = xyz[0];
	y = xyz[1];
	z = xyz[2];

	blh = xyz2llh(x+dx, y+dy, z+dz, a_, e2_);
	return blh;
}

// Method to Convert DEG to DMS
def convertDegToDms(n) {
	lat = n.toDouble()
	lat_h = lat.intValue()
	lat = (lat - lat_h) * 60
	lat_m = lat.intValue()
	lat_s = (lat - lat_m) * 60
	BigDecimal lat_sb = new BigDecimal(String.valueOf(lat_s));
	lat_s = lat_sb.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue();

	return lat_h + "," + lat_m + "," + lat_s
}

////////////////////////////////////////////////////
//                      Main
////////////////////////////////////////////////////

// Check Command Line Arguments
if (args.size() == 0) {
	System.err.println("Error: Wrong arguments");
	System.exit(255);
}

// Current Date
d = new Date();  
df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
DATE_TEXT = df.format(d);

// Default Folder Name
FOLDER_NAME="変換済み"

// For POI Output
def poi_xml = new MarkupBuilder(useDoubleQuotes: true)
//poi_xml.encoding = "Shift_JIS"
println "<?xml version=\"1.0\" encoding=\"Shift_JIS\"?>"

// Convert KML to POI
poi_xml.poix_ex() {

	// Process All Files
	for (FILE_NAME in this.args ) {
		// Open KML file
		def kml = new XmlParser().parse(FILE_NAME)

		// Parse KLM
		FOLDER_NAME = kml.Document.name.text()

		kml.Document.Placemark.each {
			// Name of the Placemark
			nb_text = it.name.text()

			// Description of the Placemark
			desc_text = it.description.text()

			// Lat / Lon of the Placemark
			cord = it.Point.coordinates.text().split(",")
			tokyo = wgs2tokyo(cord[1].toDouble(), cord[0].toDouble(), 0);
			cord_lon = convertDegToDms(tokyo[1])
			cord_lat = convertDegToDms(tokyo[0])

			// Build POI
			poix {
				format {
					datum('tokyo')
					unit('dms')
					time(DATE_TEXT)
				}
				poi {
					point {
						pos {
							lon(cord_lon)
							lat(cord_lat)
							name (style:"formal") {
								nb(nb_text)
							}
							db_use {
								db_name("")
								db_index("")
							}
						}
					}
					name (style:"formal") {
						nb(nb_text)
					}
					contact(href:"tel:", "tel:")
					note(attr:"address", "")
					note(attr:"comment", desc_text)
					note(attr:"orvis-sys", "")
					note(attr:"orvis-dir", "")
					note(attr:"orvis-type", "")
					category(FOLDER_NAME)
					mate(href:"", "")
				}
			// poix
			}
		// Placemark
		}
	// for
	}
// poi_xml.poix_ex()
}
