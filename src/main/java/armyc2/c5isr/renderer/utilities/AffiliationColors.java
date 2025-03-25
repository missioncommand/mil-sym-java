/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.renderer.utilities;


import java.awt.*;

/**
 * Default Affiliation Colors for the symbols
 * 
 */
public class AffiliationColors {

    /// <summary>
		/// Friendly Unit Fill Color.
		/// </summary>
		public static Color FriendlyUnitFillColor = new Color(128,224,255);
		/// <summary>
		/// Hostile Unit Fill Color.
		/// </summary>
		public static Color HostileUnitFillColor = new Color(255,128,128);//new Color(255,130,132);//Color.RED;
		/// <summary>
		/// Neutral Unit Fill Color.
		/// </summary>
		public static Color NeutralUnitFillColor = new Color(170,255,170);//new Color(144,238,144);//Color.GREEN;//new Color(0,255,0);//new Color(144,238,144);//light green//Color.GREEN;new Color(0,226,0);
		/// <summary>
		/// Unknown Unit Fill Color.
		/// </summary>
		public static Color UnknownUnitFillColor = new Color(255,255,128);// new Color(255,255,128);//Color.YELLOW;

		public static Color SuspectUnitFillColor = new Color(255,229,153);

		/// <summary>
		/// Friendly Graphic Fill Color.
		/// </summary>
		public static Color FriendlyGraphicFillColor = new Color(128,224,255);//Crystal Blue //Color.CYAN;
		/// <summary>
		/// Hostile Graphic Fill Color.
		/// </summary>
		public static Color HostileGraphicFillColor = new Color(255,128,128);//salmon
		/// <summary>
		/// Neutral Graphic Fill Color.
		/// </summary>
		public static Color NeutralGraphicFillColor = new Color(170,255,170);//Bamboo Green //new Color(144,238,144);//light green
		/// <summary>
		/// Unknown Graphic Fill Color.
		/// </summary>
		public static Color UnknownGraphicFillColor = new Color(255,255,128);//light yellow  new Color(255,255,224);//light yellow

		public static Color SuspectGraphicFillColor = new Color(255,229,153);

		/// <summary>
		/// Friendly Unit Line Color.
		/// </summary>
		public static Color FriendlyUnitLineColor = Color.BLACK;
		/// <summary>
		/// Hostile Unit Line Color.
		/// </summary>
		public static Color HostileUnitLineColor = Color.BLACK;
		/// <summary>
		/// Neutral Unit Line Color.
		/// </summary>
		public static Color NeutralUnitLineColor = Color.BLACK;
		/// <summary>
		/// Unknown Unit Line Color.
		/// </summary>
		public static Color UnknownUnitLineColor = Color.BLACK;

		public static Color SuspectUnitLineColor = Color.BLACK;

		/// <summary>
		/// Friendly Graphic Line Color.
		/// </summary>
		public static Color FriendlyGraphicLineColor = Color.BLACK;
		/// <summary>
		/// Hostile Graphic Line Color.
		/// </summary>
		public static Color HostileGraphicLineColor = Color.RED;
		/// <summary>
		/// Neutral Graphic Line Color.
		/// </summary>
		public static Color NeutralGraphicLineColor = Color.GREEN;
		/// <summary>
		/// Unknown Graphic Line Color.
		/// </summary>
		public static Color UnknownGraphicLineColor = Color.YELLOW;

		public static Color SuspectGraphicLineColor = new Color(255,188,1);
                
        public static Color WeatherRed = new Color(198,16,33);//0xC61021;// 198,16,33
		public static Color WeatherBlue = new Color(0,0,255);//0x0000FF;// 0,0,255

		public static Color WeatherPurpleDark = new Color(128,0,128);//0x800080;// 128,0,128 Plum Red
		public static Color WeatherPurpleLight = new Color(226,159,255);//0xE29FFF;// 226,159,255 Light Orchid
		
		public static Color WeatherBrownDark = new Color(128,98,16);//0x806210;// 128,98,16 Safari
		public static Color WeatherBrownLight = new Color(210,176,106);//0xD2B06A;// 210,176,106 Khaki

}
