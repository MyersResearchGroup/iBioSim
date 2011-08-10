/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu.stategraph;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import platu.Options;
import lmoore.TimedState;
import lmoore.zone.Zone;

/**
 *
 * @author ldtwo
 */
public class OutputDOT {
   public static boolean SIMPLE_TRAN_LABELS=true;
   public static boolean SIMPLE_STATES=true;


//    public static String toString(TimedState init,StateMap sg, HashMap<TimedState, TimedState> allStates) {
//        String ret = "digraph sg{\n"
//                + "pack=\"true\""
//                + //"\nrankdir=LR" +
//                "\nratio=\"auto\""
//                + "\nranksep=.75\n";
//        for (TimedState e : allStates.keySet()) {//states
//            String s="shape=plaintext,";
//            if(SIMPLE_STATES)s="shape=ellipse,";
//            if(e.equals(init)&&!SIMPLE_STATES)s="shape=plaintext,color=red2,";
//            else if(e.equals(init)&&SIMPLE_STATES)s="shape=box,color=red2,";
////            else if(e.getType()==StateTypeEnum.COMPLEMENT)s="shape=plaintext,color=greenyellow,style=filled,";
////            else if(e.getType()==StateTypeEnum.DEADLOCK)s="shape=box,color=navy,style=filled,";
////            else if(e.getEnabledSet().length<1&&e.getType()==StateTypeEnum.DISABLING)s="shape=diamond,color=cornflowerblue,style=filled,";
////            else if(e.getType()==StateTypeEnum.ERROR)s="shape=circle,color=crimson,style=filled,";
////            else if(e.getType()==StateTypeEnum.RACING)s="shape=box,color=mediumvioletred,style=filled,";
////            else if(e.getType()==StateTypeEnum.TERMINAL)s="shape=box,color=peru,style=filled,";
//
//            ret += "\t\"" + e.digest() + "\""
//                    + " [" +
//                    s+
//                    "label=<" + (SIMPLE_STATES?(e.hashCode()//ID
//                    ):getHTMLLabel(e)) + ">];\n";
//        }
//        for (Map.Entry<StateIntPair, StateTranList> e : sg.entrySet()) {//trans
//            for (StateTran st : e.getValue()) {
//                ret += "\t\"" + st.start.digest() + "\" -> \"" + st.end.digest()
//                        + "\" [label=\""
//                        +(SIMPLE_TRAN_LABELS?st.lpnTran.getLabel():
//                            st.lpnTran.toString().replace("[", "(")//
//                            .replace("]", ")").replace("{\n", "{")//
//                            .replace("\n}", "}").replace("\n", " ")//
//                            .replace("  ", " "))
//                        + "\"]\n";
//            }
//        }
//
//        return ret + "}";
//    }

    public static void string2File(File file, String s) {
        FileOutputStream fos = null;


        try {
            byte data[] = s.getBytes();
            file.delete();
            file.createNewFile();
            OutputStream out = null;
            out = new BufferedOutputStream(new FileOutputStream(file, true));
            out.write(data, 0, data.length);
            out.flush();
            out.close();
            fos = new FileOutputStream(file);
            fos.write(s.getBytes());
                fos.flush();
                fos.close();
        }catch(Exception ex){
            try {
                Thread.sleep(100);
                ex.printStackTrace();
                fos.flush();
                fos.close();
//                string2File(file, s);
            } catch (Exception ex1) {
           }
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * <TABLE ALIGN=CENTER>
    <TR><TH>Command-line<BR>parameter</TH><TH>Format</TH></TR>
    <TR><TD ALIGN=CENTER><A NAME=a:bmp HREF=#d:bmp>bmp</A>
    </TD><TD>Windows Bitmap Format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:canon HREF=#d:canon>canon</A>
    <BR><A NAME=a:dot HREF=#d:dot>dot</A>
    <BR><A NAME=a:xdot HREF=#d:xdot>xdot</A>
    </TD><TD>DOT</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:cmap HREF=#d:cmap>cmap</A>
    </TD><TD>Client-side imagemap (deprecated)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:eps HREF=#d:eps>eps</A>
    </TD><TD>Encapsulated PostScript</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:fig HREF=#d:fig>fig</A>
    </TD><TD>FIG</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:gd HREF=#d:gd>gd</A>
    <BR><A NAME=a:gd2 HREF=#d:gd2>gd2</A>
    </TD><TD>GD/GD2 formats</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:gif HREF=#d:gif>gif</A>
    </TD><TD>GIF</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:gtk HREF=#d:gtk>gtk</A>
    </TD><TD>GTK canvas</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:ico HREF=#d:ico>ico</A>
    </TD><TD>Icon Image File Format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:imap HREF=#d:imap>imap</A>
    <BR><A NAME=a:cmapx HREF=#d:cmapx>cmapx</A>
    </TD><TD>Server-side and client-side imagemaps</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:imap_np HREF=#d:imap_np>imap_np</A>
    <BR><A NAME=a:cmapx_np HREF=#d:cmapx_np>cmapx_np</A>
    </TD><TD>Server-side and client-side imagemaps</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:ismap HREF=#d:ismap>ismap</A>
    </TD><TD>Server-side imagemap (deprecated)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:jpg HREF=#d:jpg>jpg</A>
    <BR><A NAME=a:jpeg HREF=#d:jpeg>jpeg</A>
    <BR><A NAME=a:jpe HREF=#d:jpe>jpe</A>
    </TD><TD>JPEG</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:pdf HREF=#d:pdf>pdf</A>
    </TD><TD>Portable Document Format (PDF)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:plain HREF=#d:plain>plain</A>
    <BR><A NAME=a:plain-ext HREF=#d:plain-ext>plain-ext</A>
    </TD><TD>Simple text format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:png HREF=#d:png>png</A>
    </TD><TD>Portable Network Graphics format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:ps HREF=#d:ps>ps</A>
    </TD><TD>PostScript</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:ps2 HREF=#d:ps2>ps2</A>
    </TD><TD>PostScript for PDF</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:svg HREF=#d:svg>svg</A>
    <BR><A NAME=a:svgz HREF=#d:svgz>svgz</A>
    </TD><TD>Scalable Vector Graphics</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:tif HREF=#d:tif>tif</A>
    <BR><A NAME=a:tiff HREF=#d:tiff>tiff</A>
    </TD><TD>TIFF (Tag Image File Format)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:vml HREF=#d:vml>vml</A>
    <BR><A NAME=a:vmlz HREF=#d:vmlz>vmlz</A>
    </TD><TD>Vector Markup Language (VML)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:vrml HREF=#d:vrml>vrml</A>
    </TD><TD>VRML</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:wbmp HREF=#d:wbmp>wbmp</A>
    </TD><TD>Wireless BitMap format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:xlib HREF=#d:xlib>xlib</A>
    </TD><TD>Xlib canvas</TD> </TR>
    </TABLE>
     * @param f
     */
    public static void DOT2Image(String format, File f) {
        try {
            //LAYOUTS:  circo dot fdp neato osage patchwork sfdp twopi
DOT2Image(format, f,"sfdp");
DOT2Image(format, f,"dot");
//DOT2Image(format, f,"fdp");
DOT2Image(format, f,"neato");
//DOT2Image(format, f,"osage");
//DOT2Image(format, f,"patchwork");
//DOT2Image(format, f,"twopi");
//DOT2Image(format, f,"circo");
//            String cmd ="\""+ TimedStateGraph.DOT_PATH+"\" -T" + format + " -Kdot -O \"" + f + "\"";
//            //if (TimedStateGraph.PRINT_LEVEL < 10)
//            {
//                System.out.println(">" + cmd);
//            }
////            Runtime r = Runtime.getRuntime();
////            Process p = r.exec(cmd);
//
//       Process process = new ProcessBuilder(cmd).start();
//       InputStream is = process.getInputStream();
//       InputStreamReader isr = new InputStreamReader(is);
//       BufferedReader br = new BufferedReader(isr);
//       String line;
//
//       while ((line = br.readLine()) != null) {
//         System.out.println(line);
//       }

        } catch (Exception ex) {
            Logger.getLogger(OutputDOT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void DOT2Image(String format, File f,String layout) {
        try {
            String cmd ="\""+ Options.getDotPath() +"\""
                   +" -T" + format + " -K"+layout+" "+f
                   + " -o\"" + f +"_"+layout+"."+format+ "\"";
            //if (TimedStateGraph.PRINT_LEVEL < 10)
            {
                System.out.println(">" + cmd);
            }
//            Runtime r = Runtime.getRuntime();
//            Process p = r.exec(cmd);

      

       Process process = new ProcessBuilder(cmd).start();
       InputStream is = process.getInputStream();
       InputStreamReader isr = new InputStreamReader(is);
       BufferedReader br = new BufferedReader(isr);
       String line;

       while ((line = br.readLine()) != null) {
         System.out.println(line);
       }

        } catch (Exception ex) {
            Logger.getLogger(OutputDOT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void start(File f) {
        try {
            String cmd = "start " + " \"" + f + "\"";
            System.out.println(">" + cmd);
            Runtime r = Runtime.getRuntime();
            r.exec(cmd);

        } catch (IOException ex) {
            Logger.getLogger(OutputDOT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   /**
     * <TABLE ALIGN=CENTER>
    <TR><TH>Command-line<BR>parameter</TH><TH>Format</TH></TR>
    <TR><TD ALIGN=CENTER><A NAME=a:bmp HREF=#d:bmp>bmp</A>
    </TD><TD>Windows Bitmap Format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:canon HREF=#d:canon>canon</A>
    <BR><A NAME=a:dot HREF=#d:dot>dot</A>
    <BR><A NAME=a:xdot HREF=#d:xdot>xdot</A>
    </TD><TD>DOT</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:cmap HREF=#d:cmap>cmap</A>
    </TD><TD>Client-side imagemap (deprecated)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:eps HREF=#d:eps>eps</A>
    </TD><TD>Encapsulated PostScript</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:fig HREF=#d:fig>fig</A>
    </TD><TD>FIG</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:gd HREF=#d:gd>gd</A>
    <BR><A NAME=a:gd2 HREF=#d:gd2>gd2</A>
    </TD><TD>GD/GD2 formats</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:gif HREF=#d:gif>gif</A>
    </TD><TD>GIF</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:gtk HREF=#d:gtk>gtk</A>
    </TD><TD>GTK canvas</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:ico HREF=#d:ico>ico</A>
    </TD><TD>Icon Image File Format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:imap HREF=#d:imap>imap</A>
    <BR><A NAME=a:cmapx HREF=#d:cmapx>cmapx</A>
    </TD><TD>Server-side and client-side imagemaps</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:imap_np HREF=#d:imap_np>imap_np</A>
    <BR><A NAME=a:cmapx_np HREF=#d:cmapx_np>cmapx_np</A>
    </TD><TD>Server-side and client-side imagemaps</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:ismap HREF=#d:ismap>ismap</A>
    </TD><TD>Server-side imagemap (deprecated)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:jpg HREF=#d:jpg>jpg</A>
    <BR><A NAME=a:jpeg HREF=#d:jpeg>jpeg</A>
    <BR><A NAME=a:jpe HREF=#d:jpe>jpe</A>
    </TD><TD>JPEG</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:pdf HREF=#d:pdf>pdf</A>
    </TD><TD>Portable Document Format (PDF)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:plain HREF=#d:plain>plain</A>
    <BR><A NAME=a:plain-ext HREF=#d:plain-ext>plain-ext</A>
    </TD><TD>Simple text format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:png HREF=#d:png>png</A>
    </TD><TD>Portable Network Graphics format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:ps HREF=#d:ps>ps</A>
    </TD><TD>PostScript</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:ps2 HREF=#d:ps2>ps2</A>
    </TD><TD>PostScript for PDF</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:svg HREF=#d:svg>svg</A>
    <BR><A NAME=a:svgz HREF=#d:svgz>svgz</A>
    </TD><TD>Scalable Vector Graphics</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:tif HREF=#d:tif>tif</A>
    <BR><A NAME=a:tiff HREF=#d:tiff>tiff</A>
    </TD><TD>TIFF (Tag Image File Format)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:vml HREF=#d:vml>vml</A>
    <BR><A NAME=a:vmlz HREF=#d:vmlz>vmlz</A>
    </TD><TD>Vector Markup Language (VML)</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:vrml HREF=#d:vrml>vrml</A>
    </TD><TD>VRML</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:wbmp HREF=#d:wbmp>wbmp</A>
    </TD><TD>Wireless BitMap format</TD> </TR>
    <TR><TD ALIGN=CENTER><A NAME=a:xlib HREF=#d:xlib>xlib</A>
    </TD><TD>Xlib canvas</TD> </TR>
    </TABLE>
     * @param f
     */
   

    public static String getHTMLLabel(TimedState s) {
        String ret = "";
//        ret+="<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">";
        ret +=
                //"<TR><TD ROWSPAN=\"3\">" +
                //                "<B><FONT COLOR=\"red\">" +
                //                s.hashCode()+
                //                "</FONT></B>" +
                "<TABLE BORDER=\"1\" CELLBORDER=\"1\" CELLSPACING=\"1\" CELLPADDING=\"1\">"
                //+(TimedStateGraph.TIMED_ANALYSIS? s.getZone().toHTMLTable():"")
                //+ "<TR><TD COLSPAN=\"" +(TimedStateGraph.TIMED_ANALYSIS? (s.getZone().size() + 1):"") + "\">"
                + "M="
                + s.getMarking().toString().trim().replace("[", "&#91;").replace("]", "&#93;").replace("{", "&#123;").replace("}", "&#125;")
                + " <BR/>V="
                + (
                    s.getVector().toString()
                    ).replace("[", "&#91;").replace("]", "&#93;")
                    .replace("{", "&#123;").replace("}", "&#125;")
                + "</TD></TR>"
                + "</TABLE>" //                .toString()//
                //                .replace("\n", "\r")//
                //                .replace("\r\r", "\r")//
                //                .replace("\r     ", "\r")//
                //                .replace("\r    ", "\r")//
                //                .replace("\r ", "\r")//
                //                .replace("\r ", "\r")//
                //                .replace("\r ", "\r")//
                //                .replace("\r", "<BR/>")
                //                //.replace(" ", "&nbsp;")
                //                .replace("[", "&#91;")
                //                .replace("]", "&#93;")
                //                .replace("{", "&#123;")
                //                .replace("}", "&#125;")
                //+"</TD></TR>"
                ;
        /*
         * REFERENCE       DESCRIPTION
        --------------  -----------
        &#00; - &#08;   Unused
        &#09;     Horizontal tab
        &#10;     Line feed
        &#11; - &#12;   Unused
        &#13;     Carriage Return
        &#14; - &#31;   Unused
        &#32;     Space
        &#33;     Exclamation mark
        &#34;     Quotation mark
        &#35;     Number sign
        &#36;     Dollar sign
        &#37;     Percent sign
        &#38;     Ampersand
        &#39;     Apostrophe
        &#40;     Left parenthesis
        &#41;     Right parenthesis
        &#42;     Asterisk
        &#43;     Plus sign
        &#44;     Comma
        &#45;     Hyphen
        &#46;     Period (fullstop)
        &#47;     Solidus (slash)
        &#48; - &#57;   Digits 0-9
        &#58;     Colon
        &#59;     Semi-colon
        &#60;     Less than
        &#61;     Equals sign
        &#62;     Greater than
        &#63;     Question mark
        &#64;     Commercial at
        &#65; - &#90;   Letters A-Z
        &#91;     Left square bracket
        &#92;     Reverse solidus (backslash)
        &#93;     Right square bracket
        &#94;     Caret
        &#95;     Horizontal bar (underscore)
        &#96;     Acute accent
        &#97; - &#122;  Letters a-z
        &#123;   Left curly brace
        &#124;   Vertical bar
        &#125;   Right curly brace
        &#126;   Tilde
        &#127; - &#159; Unused
        &#160;          Non-breaking Space
        &#161;   Inverted exclamation
        &#162;   Cent sign
        &#163;   Pound sterling
        &#164;   General currency sign
        &#165;   Yen sign
        &#166;   Broken vertical bar
        &#167;   Section sign
        &#168;   Umlaut (dieresis)
        &#169;   Copyright
        &#170;   Feminine ordinal
        &#171;   Left angle quote, guillemotleft
        &#172;   Not sign
        &#173;   Soft hyphen
        &#174;   Registered trademark
        &#175;   Macron accent
        &#176;   Degree sign
        &#177;   Plus or minus
        &#178;   Superscript two
        &#179;   Superscript three
        &#180;   Acute accent
        &#181;   Micro sign
        &#182;   Paragraph sign
        &#183;   Middle dot
        &#184;   Cedilla
        &#185;   Superscript one
        &#186;   Masculine ordinal
        &#187;   Right angle quote, guillemotright
        &#188;   Fraction one-fourth
        &#189;   Fraction one-half
        &#190;   Fraction three-fourths
        &#191;   Inverted question mark
        &#192;   Capital A, grave accent
        &#193;   Capital A, acute accent
        &#194;   Capital A, circumflex accent
        &#195;   Capital A, tilde
        &#196;   Capital A, dieresis or umlaut mark
        &#197;   Capital A, ring
        &#198;   Capital AE dipthong (ligature)
        &#199;   Capital C, cedilla
        &#200;   Capital E, grave accent
        &#201;   Capital E, acute accent
        &#202;   Capital E, circumflex accent
        &#203;   Capital E, dieresis or umlaut mark
        &#204;   Capital I, grave accent
        &#205;   Capital I, acute accent
        &#206;   Capital I, circumflex accent
        &#207;   Capital I, dieresis or umlaut mark
        &#208;   Capital Eth, Icelandic
        &#209;   Capital N, tilde
        &#210;   Capital O, grave accent
        &#211;   Capital O, acute accent
        &#212;   Capital O, circumflex accent
        &#213;   Capital O, tilde
        &#214;   Capital O, dieresis or umlaut mark
        &#215;   Multiply sign
        &#216;   Capital O, slash
        &#217;   Capital U, grave accent
        &#218;   Capital U, acute accent
        &#219;   Capital U, circumflex accent
        &#220;   Capital U, dieresis or umlaut mark
        &#221;   Capital Y, acute accent
        &#222;   Capital THORN, Icelandic
        &#223;   Small sharp s, German (sz ligature)
        &#224;   Small a, grave accent
        &#225;   Small a, acute accent
        &#226;   Small a, circumflex accent
        &#227;   Small a, tilde
        &#228;   Small a, dieresis or umlaut mark
        &#229;   Small a, ring
        &#230;   Small ae dipthong (ligature)
        &#231;   Small c, cedilla
        &#232;   Small e, grave accent
        &#233;   Small e, acute accent
        &#234;   Small e, circumflex accent
        &#235;   Small e, dieresis or umlaut mark
        &#236;   Small i, grave accent
        &#237;   Small i, acute accent
        &#238;   Small i, circumflex accent
        &#239;   Small i, dieresis or umlaut mark
        &#240;   Small eth, Icelandic
        &#241;   Small n, tilde
        &#242;   Small o, grave accent
        &#243;   Small o, acute accent
        &#244;   Small o, circumflex accent
        &#245;   Small o, tilde
        &#246;   Small o, dieresis or umlaut mark
        &#247;   Division sign
        &#248;   Small o, slash
        &#249;   Small u, grave accent
        &#250;   Small u, acute accent
        &#251;   Small u, circumflex accent
        &#252;   Small u, dieresis or umlaut mark
        &#253;   Small y, acute accent
        &#254;   Small thorn, Icelandic
        &#255;   Small y, dieresis or umlaut mark
         */
        return ret//+"</TABLE>"
                ;
    }

    public static String getHTMLLabel(String marking, String vector, Zone  zone) {
        String ret = "";
//        ret+="<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">";
        ret +=
                //"<TR><TD ROWSPAN=\"3\">" +
                //                "<B><FONT COLOR=\"red\">" +
                //                s.hashCode()+
                //                "</FONT></B>" +
                "<TABLE BORDER=\"1\" CELLBORDER=\"1\" CELLSPACING=\"1\" CELLPADDING=\"1\">"
                +(Options.getTimingAnalysisFlag()? zone.toHTMLTable():"")
                + "<TR><TD COLSPAN=\"" +(Options.getTimingAnalysisFlag()? (zone.size() + 1):"") + "\">"
                + "M="
                + marking.trim().replace("[", "&#91;").replace("]", "&#93;").replace("{", "&#123;").replace("}", "&#125;")
                + " <BR/>V="
                + (
                   vector
                    ).replace("[", "&#91;").replace("]", "&#93;")
                    .replace("{", "&#123;").replace("}", "&#125;")
                + "</TD></TR>"
                + "</TABLE>" //                .toString()//
                //                .replace("\n", "\r")//
                //                .replace("\r\r", "\r")//
                //                .replace("\r     ", "\r")//
                //                .replace("\r    ", "\r")//
                //                .replace("\r ", "\r")//
                //                .replace("\r ", "\r")//
                //                .replace("\r ", "\r")//
                //                .replace("\r", "<BR/>")
                //                //.replace(" ", "&nbsp;")
                //                .replace("[", "&#91;")
                //                .replace("]", "&#93;")
                //                .replace("{", "&#123;")
                //                .replace("}", "&#125;")
                //+"</TD></TR>"
                ;
        /*
         * REFERENCE       DESCRIPTION
        --------------  -----------
        &#00; - &#08;   Unused
        &#09;     Horizontal tab
        &#10;     Line feed
        &#11; - &#12;   Unused
        &#13;     Carriage Return
        &#14; - &#31;   Unused
        &#32;     Space
        &#33;     Exclamation mark
        &#34;     Quotation mark
        &#35;     Number sign
        &#36;     Dollar sign
        &#37;     Percent sign
        &#38;     Ampersand
        &#39;     Apostrophe
        &#40;     Left parenthesis
        &#41;     Right parenthesis
        &#42;     Asterisk
        &#43;     Plus sign
        &#44;     Comma
        &#45;     Hyphen
        &#46;     Period (fullstop)
        &#47;     Solidus (slash)
        &#48; - &#57;   Digits 0-9
        &#58;     Colon
        &#59;     Semi-colon
        &#60;     Less than
        &#61;     Equals sign
        &#62;     Greater than
        &#63;     Question mark
        &#64;     Commercial at
        &#65; - &#90;   Letters A-Z
        &#91;     Left square bracket
        &#92;     Reverse solidus (backslash)
        &#93;     Right square bracket
        &#94;     Caret
        &#95;     Horizontal bar (underscore)
        &#96;     Acute accent
        &#97; - &#122;  Letters a-z
        &#123;   Left curly brace
        &#124;   Vertical bar
        &#125;   Right curly brace
        &#126;   Tilde
        &#127; - &#159; Unused
        &#160;          Non-breaking Space
        &#161;   Inverted exclamation
        &#162;   Cent sign
        &#163;   Pound sterling
        &#164;   General currency sign
        &#165;   Yen sign
        &#166;   Broken vertical bar
        &#167;   Section sign
        &#168;   Umlaut (dieresis)
        &#169;   Copyright
        &#170;   Feminine ordinal
        &#171;   Left angle quote, guillemotleft
        &#172;   Not sign
        &#173;   Soft hyphen
        &#174;   Registered trademark
        &#175;   Macron accent
        &#176;   Degree sign
        &#177;   Plus or minus
        &#178;   Superscript two
        &#179;   Superscript three
        &#180;   Acute accent
        &#181;   Micro sign
        &#182;   Paragraph sign
        &#183;   Middle dot
        &#184;   Cedilla
        &#185;   Superscript one
        &#186;   Masculine ordinal
        &#187;   Right angle quote, guillemotright
        &#188;   Fraction one-fourth
        &#189;   Fraction one-half
        &#190;   Fraction three-fourths
        &#191;   Inverted question mark
        &#192;   Capital A, grave accent
        &#193;   Capital A, acute accent
        &#194;   Capital A, circumflex accent
        &#195;   Capital A, tilde
        &#196;   Capital A, dieresis or umlaut mark
        &#197;   Capital A, ring
        &#198;   Capital AE dipthong (ligature)
        &#199;   Capital C, cedilla
        &#200;   Capital E, grave accent
        &#201;   Capital E, acute accent
        &#202;   Capital E, circumflex accent
        &#203;   Capital E, dieresis or umlaut mark
        &#204;   Capital I, grave accent
        &#205;   Capital I, acute accent
        &#206;   Capital I, circumflex accent
        &#207;   Capital I, dieresis or umlaut mark
        &#208;   Capital Eth, Icelandic
        &#209;   Capital N, tilde
        &#210;   Capital O, grave accent
        &#211;   Capital O, acute accent
        &#212;   Capital O, circumflex accent
        &#213;   Capital O, tilde
        &#214;   Capital O, dieresis or umlaut mark
        &#215;   Multiply sign
        &#216;   Capital O, slash
        &#217;   Capital U, grave accent
        &#218;   Capital U, acute accent
        &#219;   Capital U, circumflex accent
        &#220;   Capital U, dieresis or umlaut mark
        &#221;   Capital Y, acute accent
        &#222;   Capital THORN, Icelandic
        &#223;   Small sharp s, German (sz ligature)
        &#224;   Small a, grave accent
        &#225;   Small a, acute accent
        &#226;   Small a, circumflex accent
        &#227;   Small a, tilde
        &#228;   Small a, dieresis or umlaut mark
        &#229;   Small a, ring
        &#230;   Small ae dipthong (ligature)
        &#231;   Small c, cedilla
        &#232;   Small e, grave accent
        &#233;   Small e, acute accent
        &#234;   Small e, circumflex accent
        &#235;   Small e, dieresis or umlaut mark
        &#236;   Small i, grave accent
        &#237;   Small i, acute accent
        &#238;   Small i, circumflex accent
        &#239;   Small i, dieresis or umlaut mark
        &#240;   Small eth, Icelandic
        &#241;   Small n, tilde
        &#242;   Small o, grave accent
        &#243;   Small o, acute accent
        &#244;   Small o, circumflex accent
        &#245;   Small o, tilde
        &#246;   Small o, dieresis or umlaut mark
        &#247;   Division sign
        &#248;   Small o, slash
        &#249;   Small u, grave accent
        &#250;   Small u, acute accent
        &#251;   Small u, circumflex accent
        &#252;   Small u, dieresis or umlaut mark
        &#253;   Small y, acute accent
        &#254;   Small thorn, Icelandic
        &#255;   Small y, dieresis or umlaut mark
         */
        return ret//+"</TABLE>"
                ;
    }

}
