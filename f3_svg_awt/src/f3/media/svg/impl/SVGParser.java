package f3.media.svg.impl;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

/**
 * Entry point for parsing SVG files for Android.
 * Use one of the various static methods for parsing SVGs by resource, asset or input stream.
 * Optionally, a single color can be searched and replaced in the SVG while parsing.
 * You can also parse an svg path directly.
 *
 * @see #getSVGFromResource(android.content.res.Resources, int)
 * @see #getSVGFromAsset(android.content.res.AssetManager, String)
 * @see #getSVGFromString(String)
 * @see #getSVGFromInputStream(java.io.InputStream)
 * @see #parsePath(String)
 *
 * @author Larva Labs, LLC
 */
public class SVGParser {



    static final String TAG = "SVGAndroid";

    /**
     * Parse SVG data from an input stream.
     * @param svgData the input stream, with SVG XML data in UTF-8 character encoding.
     * @return the parsed SVG.
     * @throws SVGParseException if there is an error while parsing.
     */
    public static SVG getSVGFromInputStream(Picture picture, 
                                            InputStream svgData) throws SVGParseException {
        return SVGParser.parse(picture, svgData, 0, 0, false);
    }

    /**
     * Parses a single SVG path and returns it as a <code>android.graphics.Path</code> object.
     * An example path is <code>M250,150L150,350L350,350Z</code>, which draws a triangle.
     *
     * @param pathString the SVG path, see the specification <a href="http://www.w3.org/TR/SVG/paths.html">here</a>.
     */
    public static Path parsePath(String pathString) {
        Path p = new Path();
        return doPath(p, pathString);
    }

    private static SVG parse(Picture picture, InputStream in, Integer searchColor, Integer replaceColor, boolean whiteMode) throws SVGParseException {
//        Util.debug("Parsing SVG...");
        try {
            long start = System.currentTimeMillis();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            spf.setNamespaceAware(true);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            SVGHandler handler = new SVGHandler(picture);
            handler.setColorSwap(searchColor, replaceColor);
            handler.setWhiteMode(whiteMode);
            xr.setContentHandler(handler);
            xr.parse(new InputSource(in));
//        Util.debug("Parsing complete in " + (System.currentTimeMillis() - start) + " millis.");
            SVG result = new SVG(picture, handler.bounds);
            // Skip bounds if it was an empty pic
            if (!Float.isInfinite(handler.limits.top)) {
                result.setLimits(handler.limits);
            }
            return result;
        } catch (Exception e) {
            throw new SVGParseException(e);
        }
    }

    private static NumberParse parseNumbers(String s) {
        //Util.debug("Parsing numbers from: '" + s + "'");
        int n = s.length();
        int p = 0;
        ArrayList<Float> numbers = new ArrayList<Float>();
        boolean skipChar = false;
        for (int i = 1; i < n; i++) {
            if (skipChar) {
                skipChar = false;
                continue;
            }
            char c = s.charAt(i);
            switch (c) {
                // This ends the parsing, as we are on the next element
                case 'M':
                case 'm':
                case 'Z':
                case 'z':
                case 'L':
                case 'l':
                case 'H':
                case 'h':
                case 'V':
                case 'v':
                case 'C':
                case 'c':
                case 'S':
                case 's':
                case 'Q':
                case 'q':
                case 'T':
                case 't':
                case 'a':
                case 'A':
                case ')': {
                    String str = s.substring(p, i);
                    if (str.trim().length() > 0) {
                        //Util.debug("  Last: " + str);
                        Float f = Float.parseFloat(str);
                        numbers.add(f);
                    }
                    p = i;
                    return new NumberParse(numbers, p);
                }
                case '\n':
                case '\t':
                case ' ':
                case ',':
                    /*case '-':*/ {
                    String str = s.substring(p, i);
                    // Just keep moving if multiple whitespace
                    if (str.trim().length() > 0) {
                        //Util.debug("  Next: " + str);
                        Float f = Float.parseFloat(str);
                        numbers.add(f);
                        if (c == '-') {
                            p = i;
                        } else {
                            p = i + 1;
                            skipChar = true;
                        }
                    } else {
                        p++;
                    }
                    break;
                }
            }
        }
        String last = s.substring(p);
        if (last.length() > 0) {
            //Util.debug("  Last: " + last);
            try {
                numbers.add(Float.parseFloat(last));
            } catch (NumberFormatException nfe) {
                // Just white-space, forget it
            }
            p = s.length();
        }
        return new NumberParse(numbers, p);
    }

    private static Matrix parseTransform(String s) {
        if (s == null) {
            return null;
        }
        if (s.startsWith("matrix(")) {
            NumberParse np = parseNumbers(s.substring("matrix(".length()));
            if (np.numbers.size() == 6) {
                Matrix matrix = new Matrix();
                matrix.setValues(new float[]{
                        // Row 1
                        np.numbers.get(0),
                        np.numbers.get(2),
                        np.numbers.get(4),
                        // Row 2
                        np.numbers.get(1),
                        np.numbers.get(3),
                        np.numbers.get(5),
                        // Row 3
                        0,
                        0,
                        1,
                });
                return matrix;
            }
        } else if (s.startsWith("translate(")) {
            NumberParse np = parseNumbers(s.substring("translate(".length()));
            if (np.numbers.size() > 0) {
                float tx = np.numbers.get(0);
                float ty = tx;
                if (np.numbers.size() > 1) {
                    ty = np.numbers.get(1);
                }
                Matrix matrix = new Matrix();
                matrix.postTranslate(tx, ty);
                return matrix;
            }
        } else if (s.startsWith("scale(")) {
            NumberParse np = parseNumbers(s.substring("scale(".length()));
            if (np.numbers.size() > 0) {
                float sx = np.numbers.get(0);
                float sy = sx;
                if (np.numbers.size() > 1) {
                    sy = np.numbers.get(1);
                }
                Matrix matrix = new Matrix();
                matrix.postScale(sx, sy);
                return matrix;
            }
        } else if (s.startsWith("skewX(")) {
            NumberParse np = parseNumbers(s.substring("skewX(".length()));
            if (np.numbers.size() > 0) {
                float angle = np.numbers.get(0);
                Matrix matrix = new Matrix();
                matrix.postSkew((float) Math.tan(angle), 0);
                return matrix;
            }
        } else if (s.startsWith("skewY(")) {
            NumberParse np = parseNumbers(s.substring("skewY(".length()));
            if (np.numbers.size() > 0) {
                float angle = np.numbers.get(0);
                Matrix matrix = new Matrix();
                matrix.postSkew(0, (float) Math.tan(angle));
                return matrix;
            }
        } else if (s.startsWith("rotate(")) {
            NumberParse np = parseNumbers(s.substring("rotate(".length()));
            if (np.numbers.size() > 0) {
                float angle = np.numbers.get(0);
                float cx = 0;
                float cy = 0;
                if (np.numbers.size() > 2) {
                    cx = np.numbers.get(1);
                    cy = np.numbers.get(2);
                }
                Matrix matrix = new Matrix();
                matrix.postTranslate(cx, cy);
                matrix.postRotate(angle);
                matrix.postTranslate(-cx, -cy);
                return matrix;
            }
        }
        return null;
    }

    /**
     * This is where the hard-to-parse paths are handled.
     * Uppercase rules are absolute positions, lowercase are relative.
     * Types of path rules:
     * <p/>
     * <ol>
     * <li>M/m - (x y)+ - Move to (without drawing)
     * <li>Z/z - (no params) - Close path (back to starting point)
     * <li>L/l - (x y)+ - Line to
     * <li>H/h - x+ - Horizontal ine to
     * <li>V/v - y+ - Vertical line to
     * <li>C/c - (x1 y1 x2 y2 x y)+ - Cubic bezier to
     * <li>S/s - (x2 y2 x y)+ - Smooth cubic bezier to (shorthand that assumes the x2, y2 from previous C/S is the x1, y1 of this bezier)
     * <li>Q/q - (x1 y1 x y)+ - Quadratic bezier to
     * <li>T/t - (x y)+ - Smooth quadratic bezier to (assumes previous control point is "reflection" of last one w.r.t. to current point)
     * </ol>
     * <p/>
     * Numbers are separate by whitespace, comma or nothing at all (!) if they are self-delimiting, (ie. begin with a - sign)
     *
     * @param s the path string from the XML
     */
    private static Path doPath(Path p, String s) {
        if (s == null || s.length() == 0) {
            return p;
        }
        int n = s.length();
        ParserHelper ph = new ParserHelper(s, 0);
        ph.skipWhitespace();

        float lastX = 0;
        float lastY = 0;
        float lastX1 = 0;
        float lastY1 = 0;
        char lastCmd = 'M';
        while (ph.pos < n) {
            char cmd = s.charAt(ph.pos);
            //Util.debug("* Commands remaining: '" + path + "'.");
            if ("MmZzLlHhVvqQcCSsAa".indexOf(cmd) >= 0) {
                ph.advance();
            } else {
                cmd = lastCmd;
            }
            lastCmd = cmd;
            boolean wasCurve = false;
            switch (cmd) {
                case 'M':
                case 'm': {
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'm') {
                        p.rMoveTo(x, y);
                        lastX += x;
                        lastY += y;
                    } else {
                        p.moveTo(x, y);
                        lastX = x;
                        lastY = y;
                    }
                    break;
                }
                case 'Z':
                case 'z': {
                    p.close();
                    break;
                }
                case 'L':
                case 'l': {
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'l') {
                        p.rLineTo(x, y);
                        lastX += x;
                        lastY += y;
                    } else {
                        p.lineTo(x, y);
                        lastX = x;
                        lastY = y;
                    }
                    break;
                }
                case 'H':
                case 'h': {
                    float x = ph.nextFloat();
                    if (cmd == 'h') {
                        p.rLineTo(x, 0);
                        lastX += x;
                    } else {
                        p.lineTo(x, lastY);
                        lastX = x;
                    }
                    break;
                }
                case 'V':
                case 'v': {
                    float y = ph.nextFloat();
                    if (cmd == 'v') {
                        p.rLineTo(0, y);
                        lastY += y;
                    } else {
                        p.lineTo(lastX, y);
                        lastY = y;
                    }
                    break;
                }
                case 'Q':
                case 'q': {
                    float x1 = ph.nextFloat();
                    float y1 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'q') {
                        x1 += lastX;
                        x += lastX;
                        y1 += lastY;
                        y += lastY;
                    }
                    p.quadTo(x1, y1, x, y);
                    lastX1 = x1;
                    lastY1 = y1;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'C':
                case 'c': {
                    wasCurve = true;
                    float x1 = ph.nextFloat();
                    float y1 = ph.nextFloat();
                    float x2 = ph.nextFloat();
                    float y2 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'c') {
                        x1 += lastX;
                        x2 += lastX;
                        x += lastX;
                        y1 += lastY;
                        y2 += lastY;
                        y += lastY;
                    }
                    p.cubicTo(x1, y1, x2, y2, x, y);
                    lastX1 = x2;
                    lastY1 = y2;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'S':
                case 's': {
                    wasCurve = true;
                    float x2 = ph.nextFloat();
                    float y2 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 's') {
                        x2 += lastX;
                        x += lastX;
                        y2 += lastY;
                        y += lastY;
                    }
                    float x1 = 2 * lastX - lastX1;
                    float y1 = 2 * lastY - lastY1;
                    p.cubicTo(x1, y1, x2, y2, x, y);
                    lastX1 = x2;
                    lastY1 = y2;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'A':
                case 'a': {
                    float rx = ph.nextFloat();
                    float ry = ph.nextFloat();
                    float theta = ph.nextFloat();
                    int largeArc = (int) ph.nextFloat();
                    int sweepArc = (int) ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    drawArc(p, lastX, lastY, x, y, rx, ry, theta, largeArc, sweepArc);
                    lastX = x;
                    lastY = y;
                    break;
                }
            default: {
                System.err.println("unhandled path command: "+ cmd);
            }
                   
            }
            if (!wasCurve) {
                lastX1 = lastX;
                lastY1 = lastY;
            }
            ph.skipWhitespace();
        }
        return p;
    }

    private static void drawArc(Path p, float lastX, float lastY, float x, float y, float rx, float ry, float theta, int largeArc, int sweepArc) {
        // todo - not implemented yet, may be very hard to do using Android drawing facilities.
    }

    private static NumberParse getNumberParseAttr(String name, Attributes attributes) {
        int n = attributes.getLength();
        for (int i = 0; i < n; i++) {
            if (attributes.getLocalName(i).equals(name)) {
                return parseNumbers(attributes.getValue(i));
            }
        }
        return null;
    }

    private static String getStringAttr(String name, Attributes attributes) {
        int n = attributes.getLength();
        for (int i = 0; i < n; i++) {
            if (attributes.getLocalName(i).equals(name)) {
                return attributes.getValue(i);
            }
        }
        return null;
    }
    private static float getFloatAttr(String name, Attributes attributes, float dflt) {
        Float x = getFloatAttr(name, attributes);
        if (x == null) {
            return dflt;
        }
        return x;
    }

    private static Float getFloatAttr(String name, Attributes attributes) {
        return getFloatAttr(name, attributes, null);
    }

    private static Float getFloatAttr(String name, Attributes attributes, Float defaultValue) {
        String v = getStringAttr(name, attributes);
        if (v == null) {
            return defaultValue;
        } else {
            float factor = 1.0f;
            float ppi = 85f;
            if (v.endsWith("px")) {
                v = v.substring(0, v.length() - 2);
            } else if (v.endsWith("%")) {
                v = v.substring(0, v.length() - 1);
                factor = 0.01f;
            } else if (v.endsWith("pt")) {
                v = v.substring(0, v.length() - 2);
            } else if (v.endsWith("mm")) {
                factor = 1.0f/25.4f *ppi;
                v = v.substring(0, v.length() - 2);
            } else if (v.endsWith("cm")) {
                factor = 1.0f/2.54f *ppi;
                v = v.substring(0, v.length() - 2);
            } 
//            Log.d(TAG, "Float parsing '" + name + "=" + v + "'");
            return factor * Float.parseFloat(v);
        }
    }

    private static Integer getHexAttr(String name, Attributes attributes) {
        String v = getStringAttr(name, attributes);
        //Util.debug("Hex parsing '" + name + "=" + v + "'");
        if (v == null) {
            return null;
        } else {
            try {
                return Integer.parseInt(v.substring(1), 16);
            } catch (NumberFormatException nfe) {
                // todo - parse word-based color here
                return null;
            }
        }
    }

    private static class NumberParse {
        private ArrayList<Float> numbers;
        private int nextCmd;

        public NumberParse(ArrayList<Float> numbers, int nextCmd) {
            this.numbers = numbers;
            this.nextCmd = nextCmd;
        }

        public int getNextCmd() {
            return nextCmd;
        }

        public float getNumber(int index) {
            return numbers.get(index);
        }

    }

    private static class Gradient {
        String id;
        String xlink;
        boolean isLinear;
        boolean userSpace;
        float x1, y1, x2, y2;
        float x, y, radius;
        ArrayList<Float> positions = new ArrayList<Float>();
        ArrayList<Integer> colors = new ArrayList<Integer>();
        Matrix matrix = null;

        public Gradient createChild(Gradient g) {
            Gradient child = new Gradient();
            child.id = g.id;
            child.xlink = id;
            child.isLinear = g.isLinear;
            child.x1 = g.x1;
            child.x2 = g.x2;
            child.y1 = g.y1;
            child.y2 = g.y2;
            child.x = g.x;
            child.y = g.y;
            child.userSpace = g.userSpace;
            child.radius = g.radius;
            child.positions = positions;
            child.colors = colors;
            child.matrix = matrix;
            if (g.matrix != null) {
                if (matrix == null) {
                    child.matrix = g.matrix;
                } else {
                    Matrix m = new Matrix(matrix);
                    m.preConcat(g.matrix);
                    child.matrix = m;
                }
            }
            return child;
        }
    }

    private static class StyleSet {
        HashMap<String, String> styleMap = new HashMap<String, String>();
        private StyleSet(String string) {
            if (string != null) {
                String[] styles = string.split(";");
                for (String s : styles) {
                    String[] style = s.split(":");
                    if (style.length == 2) {
                        styleMap.put(style[0], style[1]);
                    }
                }
            }
        }

        public String getStyle(String name) {
            return styleMap.get(name);
        }
    }

    private static class Properties {
        StyleSet styles = null;
        Attributes atts;
        Properties parent;

        private Properties(Attributes atts) {
            this(null, atts);
        }

        private Properties(Properties parent, Attributes atts) {
            this.atts = atts;
            String styleAttr = getStringAttr("style", atts);
            if (styleAttr != null) {
                styles = new StyleSet(styleAttr);
            }
            this.parent = parent;
        }

        public String getAttr(String name) {
            String v = null;
            if (styles != null) {
                v = styles.getStyle(name);
            }
            if (v == null) {
                v = getStringAttr(name, atts);
            }
            if (v == null) {
                if (parent != null) {
                    v = parent.getAttr(name);
                }
            }
            return v;
        }

        public String getString(String name) {
            return getAttr(name);
        }

        public Integer getHex(String name) {
            String v = getAttr(name);
            if (v == null || !v.startsWith("#")) {
                return null;
            } else {
                try {
                    return Integer.parseInt(v.substring(1).toLowerCase(), 16);
                } catch (NumberFormatException nfe) {
                    // todo - parse word-based color here
                    System.out.println(nfe);
                    return null;
                }
            }
        }

        public Float getFloat(String name, float defaultValue) {
            Float v = getFloat(name);
            if (v == null) {
                return defaultValue;
            } else {
                return v;
            }
        }

        public Float getFloat(String name) {
            String v = getAttr(name);
            if (v == null) {
                return null;
            } else {
                try {
                    return Float.parseFloat(v);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }
        }
    }

    private static class SVGHandler extends DefaultHandler {

        Picture picture;
        Canvas canvas;
        Paint strokePaint;
        Paint paint;
        // Scratch rect (so we aren't constantly making new ones)

        RectF bounds = null;
        RectF limits = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        Integer searchColor = null;
        Integer replaceColor = null;

        boolean whiteMode = false;

        boolean pushed = false;
        HashMap<String, Canvas> symbolMap = new HashMap<String, Canvas>();
        HashMap<String, Shader> gradientMap = new HashMap<String, Shader>();
        HashMap<String, Gradient> gradientRefMap = new HashMap<String, Gradient>();
        Gradient gradient = null;

        Stack<Canvas> canvasStack = new Stack();

        private SVGHandler(Picture picture) {
            this.picture = picture;
            paint = new Paint();
            strokePaint = new Paint();
            paint.setAntiAlias(true);
        }

        public void setColorSwap(Integer searchColor, Integer replaceColor) {
            this.searchColor = searchColor;
            this.replaceColor = replaceColor;
        }

        public void setWhiteMode(boolean whiteMode) {
            this.whiteMode = whiteMode;
        }

        @Override
        public void startDocument() throws SAXException {
            // Set up prior to parsing a doc
        }

        @Override
        public void endDocument() throws SAXException {
            // Clean up after parsing a doc
        }

        Stack<Properties> gStack = new Stack();

	Stack<String> idStack = new Stack();

	void pushId(String id) {
	    idStack.push(id);
	}

	void popId() {
	    idStack.pop();
	}

	public String getId() {
	    for (String id: idStack) {
		if (id != null) return id;
	    }
	    return null;
	}

        Properties getParent() {
            if (gStack.size()  > 0) {
                return gStack.peek();
            }
            return null;
        }

        void pushG(Properties props) {
            gStack.push(props);
        }

        void popG() {
            if (gStack.size() > 0) {
                gStack.pop();
                canvas.beginClip();
                canvas.endClip();
            }
        }        

        private boolean doFill(Properties atts, HashMap<String, Shader> gradients) {
            if ("none".equals(atts.getString("display"))) {
                return false;
            }
            if (whiteMode) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFFFFFFF);
                return true;
            }
            String fillString = atts.getString("fill");
            if ("NONE".equalsIgnoreCase(fillString)) {
                return false;
            }
            doAlpha(atts, true);
            if (fillString != null && fillString.startsWith("url(#")) {
                // It's a gradient fill, look it up in our map
                String id = fillString.substring("url(#".length(), fillString.length() - 1);
                Shader shader = gradients.get(id);
                if (shader != null) {
                    //Util.debug("Found shader!");
                    paint.setShader(shader);
                    paint.setStyle(Paint.Style.FILL);
                    return true;
                } else {
                    //Util.debug("Didn't find shader!");
                    return false;
                }
            } else {
                paint.setShader(null);
                Integer color = Color.resolve(fillString);
                if (color != null) {
                    doColor(atts, color, true);
                    paint.setStyle(Paint.Style.FILL);
                    return true;
                } else if (atts.getString("fill") == null && atts.getString("stroke") == null) {
                    // Default is black fill
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(0xFF000000);
                    return true;
                }
            }
            return false;
        }

        private boolean doStroke(Properties atts, HashMap<String, Shader> gradients) {
            if (whiteMode) {
                // Never stroke in white mode
                return false;
            }
            if ("none".equals(atts.getString("display"))) {
                return false;
            }
            String strokeStr = atts.getString("stroke");
            if (strokeStr == null) {
                return false;
            }
            if ("NONE".equalsIgnoreCase(strokeStr)) {
                return false;
            }
            doAlpha(atts, false);

            if (strokeStr != null && strokeStr.startsWith("url(#")) {
                // It's a gradient fill, look it up in our map
                String id = strokeStr.substring("url(#".length(), strokeStr.length() - 1);
                Shader shader = gradients.get(id);
                if (shader != null) {
                    //Util.debug("Found shader!");
                    strokePaint.setShader(shader);
                    strokePaint.setStyle(Paint.Style.STROKE);
                } else {
                    //Util.debug("Didn't find shader!");
                    System.out.println("didn't find shader for stroke: "+ strokeStr);
                    return false;
                }
            } else {
                Integer color = Color.resolve(strokeStr);
                if (color != null) {
                    doColor(atts, color, false);
                }
            }
            // Check for other stroke attributes
            Float width = atts.getFloat("stroke-width");
            // Set defaults
            if (width != null) {
                strokePaint.setStrokeWidth(width);
            }
            String linecap = atts.getString("stroke-linecap");
            if ("round".equals(linecap)) {
                strokePaint.setStrokeCap(Paint.Cap.ROUND);
            } else if ("square".equals(linecap)) {
                strokePaint.setStrokeCap(Paint.Cap.SQUARE);
            } else if ("butt".equals(linecap)) {
                strokePaint.setStrokeCap(Paint.Cap.BUTT);
            }
            String linejoin = atts.getString("stroke-linejoin");
            if ("miter".equals(linejoin)) {
                strokePaint.setStrokeJoin(Paint.Join.MITER);
            } else if ("round".equals(linejoin)) {
                strokePaint.setStrokeJoin(Paint.Join.ROUND);
            } else if ("bevel".equals(linejoin)) {
                strokePaint.setStrokeJoin(Paint.Join.BEVEL);
            }
            strokePaint.setStyle(Paint.Style.STROKE);
            return true;
        }

        private Gradient doGradient(boolean isLinear, Attributes atts) {
            Gradient gradient = new Gradient();
            gradient.id = getStringAttr("id", atts);
            gradient.isLinear = isLinear;
            if (isLinear) {
                gradient.x1 = getFloatAttr("x1", atts, 0f);
                gradient.x2 = getFloatAttr("x2", atts, 1f);
                gradient.y1 = getFloatAttr("y1", atts, 0f);
                gradient.y2 = getFloatAttr("y2", atts, 0f);
            } else {
                gradient.x = getFloatAttr("cx", atts, 0.5f);
                gradient.y = getFloatAttr("cy", atts, 0.5f);
                gradient.x1 = getFloatAttr("fx", atts, gradient.x);
                gradient.y1 = getFloatAttr("fy", atts, gradient.y);
                gradient.radius = getFloatAttr("r", atts, 0.5f);
            }
            String units = getStringAttr("gradientUnits", atts);
            if ("userSpaceOnUse".equals(units)) {
                gradient.userSpace = true;
            }
            String transform = getStringAttr("gradientTransform", atts);
            if (transform != null) {
                gradient.matrix = parseTransform(transform);
            }
            String xlink = getStringAttr("href", atts);
            if (xlink != null) {
                if (xlink.startsWith("#")) {
                    xlink = xlink.substring(1);
                }
                gradient.xlink = xlink;
            }
            return gradient;
        }

        private void doColor(Properties atts, Integer color, boolean fillMode) {
            int c = (0xFFFFFF & color) | 0xFF000000;
            if (searchColor != null && searchColor.intValue() == c) {
                c = replaceColor;
            }
            Paint paint = fillMode ? this.paint : this.strokePaint;
            paint.setColor(c);
            doAlpha(atts, fillMode);
        }

        private void doAlpha(Properties atts, boolean fillMode) {
            Float opacity = atts.getFloat("opacity");
            Paint paint = fillMode ? this.paint : this.strokePaint;
            if (opacity == null) {
                opacity = atts.getFloat(fillMode ? "fill-opacity" : "stroke-opacity");
            }
            if (opacity == null) {
                //paint.setAlpha(255);
            } else {
                paint.setAlpha((int) (255 * opacity));
            }            
        }


        private boolean hidden = false;
        private int hiddenLevel = 0;
        private boolean boundsMode = false;

        private void doLimits(float x, float y) {
            if (x < limits.left) {
                limits.left = x;
            }
            if (x > limits.right) {
                limits.right = x;
            }
            if (y < limits.top) {
                limits.top = y;
            }
            if (y > limits.bottom) {
                limits.bottom = y;
            }
        }

        private void doLimits(float x, float y, float width, float height) {
            doLimits(x, y);
            doLimits(x + width, y + height);
        }


        private void doLimits(Path path) {
            RectF rect = new RectF();
            path.computeBounds(rect, false);
            doLimits(rect.left, rect.top);
            doLimits(rect.right, rect.bottom);
        }

        private void pushTransform(Attributes atts) {
            final String transform = getStringAttr("transform", atts);
            boolean pushed = transform != null;
            canvas.save();
            if (pushed) {
                final Matrix matrix = parseTransform(transform);
                canvas.concat(matrix);
            }
        }

        private void popTransform() {
            canvas.restore();
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            // Reset paint opacity
            paint.setAlpha(255);
            // Ignore everything but rectangles in bounds mode
            System.out.println("localName: "+localName);
            System.out.println("qname: "+qName);
            if (localName.length() == 0) {
                localName = qName;
            }
            if (boundsMode) {
                if (localName.equals("rect")) {
                    Float x = getFloatAttr("x", atts);
                    if (x == null) {
                        x = 0f;
                    }
                    Float y = getFloatAttr("y", atts);
                    if (y == null) {
                        y = 0f;
                    }
                    Float width = getFloatAttr("width", atts);
                    Float height = getFloatAttr("height", atts);
                    bounds = new RectF(x, y, x + width, y + width);
                }
                return;
            }
	    String id = getStringAttr("id", atts);
	    if (id == null && getId() == null) {
		id = localName;
	    }
	    pushId(id);
	    if (canvas != null) canvas.setId(id);
            if (localName.equals("svg")) {
                pushG(new Properties(getParent(), atts));
                int width = -1;
                int height = -1;
                if (getFloatAttr("height", atts) != null) {
                    height = (int) Math.ceil(getFloatAttr("height", atts));
                }
                if (getFloatAttr("width", atts) != null) {
                    width = (int) Math.ceil(getFloatAttr("width", atts));
                }
                canvas = picture.beginRecording(width, height);
            } else if (localName.equals("symbol")) {
                canvasStack.push(canvas);
                canvas = picture.beginRecording(0, 0);
                canvas.beginSymbol();
                symbolMap.put(id, canvas);
                NumberParse numbers = getNumberParseAttr("viewBox", atts);
                if (numbers != null) {
                    float x = numbers.numbers.get(0);
                    float y = numbers.numbers.get(1);
                    float w = numbers.numbers.get(2);
                    float h = numbers.numbers.get(3);
                    Matrix mat = new Matrix();
                    mat.postTranslate(-x, -y);
                    canvas.concat(mat);
                }
            } else if (localName.equals("use")) {
                String href = getStringAttr("href", atts);
                int n = atts.getLength();
                for (int i = 0; i < n; i++) {
                    System.out.println("localName: "+ atts.getLocalName(i));
                    System.out.println("qName: "+ atts.getQName(i));
                }
                Canvas canv = symbolMap.get(href.substring(1));
                if (canv != null) {
                    canv.use(getFloatAttr("x", atts, 0f),
                             getFloatAttr("y", atts, 0f),
                             parseTransform(getStringAttr("transform", atts)));
                } else {
                    System.out.println("can't find symbol: " +href);
                }
            } else if (localName.equals("defs")) {
                // Ignore
            } else if (localName.equals("linearGradient")) {
                gradient = doGradient(true, atts);
            } else if (localName.equals("radialGradient")) {
                gradient = doGradient(false, atts);
            } else if (localName.equals("stop")) {
                if (gradient != null) {
                    float offset = getFloatAttr("offset", atts);
                    String styles = getStringAttr("style", atts);
                    StyleSet styleSet = new StyleSet(styles);
                    String colorStyle = styleSet.getStyle("stop-color");
                    if (colorStyle == null) {
                        colorStyle = getStringAttr("stop-color", atts); // yes, some svg's have this
                    }
                    int color = Color.BLACK;
                    if (colorStyle != null) {
                        color = Color.resolve(colorStyle);
                    }
                    String opacityStyle = styleSet.getStyle("stop-opacity");
                    if (opacityStyle != null) {
                        float alpha = Float.parseFloat(opacityStyle);
                        int alphaInt = Math.round(255 * alpha);
                        System.out.println("stop-opacity: "+alphaInt);
                        color &= ~0xFF000000;
                        color |= (alphaInt << 24);
                    } else {
                        color |= 0xFF000000;
                    }
                    System.out.println("stop offset: "+ offset);
                    System.out.println("stop color: "+ String.format("%x", color));
                    gradient.positions.add(offset);
                    gradient.colors.add(color);
                }
            } else if (localName.equals("animate")) {
                //String attrType = getStringAttr("attributeType", atts);
                //String attributeName = getStringAttr("attrName", atts);
                //String from = getStringAttr("from");
                //String to = getStringAttr("to");
                //String dur = getStringAttr("dur");
                //if ("css".equalsIgnoreCase(attrType)) {
                // }
            } else if (localName.equals("g")) {
                // Check to see if this is the "bounds" layer
                Properties props = new Properties(getParent(), atts);
                pushG(props);
                pushTransform(atts);
                if ("bounds".equalsIgnoreCase(getStringAttr("id", atts))) {
                    boundsMode = true;
                }
                if (hidden) {
                    hiddenLevel++;
                    //Util.debug("Hidden up: " + hiddenLevel);
                }
                // Go in to hidden mode if display is "none"
                if ("none".equals(getStringAttr("display", atts))) {
                    if (!hidden) {
                        hidden = true;
                        hiddenLevel = 1;
                        //Util.debug("Hidden up: " + hiddenLevel);
                    }
                }
            } else if (!hidden && localName.equals("rect")) {
                Float x = getFloatAttr("x", atts);
                if (x == null) {
                    x = 0f;
                }
                Float y = getFloatAttr("y", atts);
                if (y == null) {
                    y = 0f;
                }
                Float width = getFloatAttr("width", atts);
                Float height = getFloatAttr("height", atts);
                pushTransform(atts);
                Properties props = new Properties(getParent(), atts);
                if (doFill(props, gradientMap)) {
                    doLimits(x, y, width, height);
                    canvas.drawRect(x, y, x + width, y + height, paint);
                }
                if (doStroke(props, gradientMap)) {
                    canvas.drawRect(x, y, x + width, y + height, strokePaint);
                }
                popTransform();
            } else if (!hidden && localName.equals("line")) {
                Float x1 = getFloatAttr("x1", atts);
                Float x2 = getFloatAttr("x2", atts);
                Float y1 = getFloatAttr("y1", atts);
                Float y2 = getFloatAttr("y2", atts);
                Properties props = new Properties(getParent(), atts);
                if (doStroke(props, gradientMap)) {
                    pushTransform(atts);
                    doLimits(x1, y1);
                    doLimits(x2, y2);
                    canvas.drawLine(x1, y1, x2, y2, strokePaint);
                    popTransform();
                }
            } else if (!hidden && localName.equals("circle")) {
                Float centerX = getFloatAttr("cx", atts);
                Float centerY = getFloatAttr("cy", atts);
                Float radius = getFloatAttr("r", atts);
                if (centerX != null && centerY != null && radius != null) {
                    pushTransform(atts);
                    System.out.println("doing circle");
                    Properties props = new Properties(getParent(), atts);
                    if (doFill(props, gradientMap)) {
                        System.out.println("did fill");
                        doLimits(centerX - radius, centerY - radius);
                        doLimits(centerX + radius, centerY + radius);
                        canvas.drawCircle(centerX, centerY, radius, paint);
                    }
                    if (doStroke(props, gradientMap)) {
                        canvas.drawCircle(centerX, centerY, radius, strokePaint);
                    }
                    popTransform();
                }
            } else if (!hidden && localName.equals("ellipse")) {
                Float centerX = getFloatAttr("cx", atts);
                Float centerY = getFloatAttr("cy", atts);
                Float radiusX = getFloatAttr("rx", atts);
                Float radiusY = getFloatAttr("ry", atts);
                if (centerX != null && centerY != null && radiusX != null && radiusY != null) {
                    pushTransform(atts);
                    Properties props = new Properties(getParent(), atts);
                    RectF rect = new RectF();
                    rect.set(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);
                    if (doFill(props, gradientMap)) {
                        doLimits(centerX - radiusX, centerY - radiusY);
                        doLimits(centerX + radiusX, centerY + radiusY);
                        canvas.drawOval(rect, paint);
                    }
                    if (doStroke(props, gradientMap)) {
                        canvas.drawOval(rect, strokePaint);
                    }
                    popTransform();
                }
            } else if (!hidden && (localName.equals("polygon") || localName.equals("polyline"))) {
                NumberParse numbers = getNumberParseAttr("points", atts);
                if (numbers != null) {
                    Path p = new Path();
                    ArrayList<Float> points = numbers.numbers;
                    if (points.size() > 1) {
                        pushTransform(atts);
                        Properties props = new Properties(getParent(), atts);
                        p.moveTo(points.get(0), points.get(1));
                        for (int i = 2; i < points.size(); i += 2) {
                            float x = points.get(i);
                            float y = points.get(i + 1);
                            p.lineTo(x, y);
                        }
                        // Don't close a polyline
                        if (localName.equals("polygon")) {
                            p.close();
                        }
                        if (doFill(props, gradientMap)) {
                            doLimits(p);
                            canvas.drawPath(p, paint);
                        } 
                        if (doStroke(props, gradientMap)) {
                            canvas.drawPath(p, strokePaint);
                        }
                        popTransform();
                    }
                }
            } else if (!hidden && localName.equals("clipPath")) {
                canvas.beginClip();
            } else if (!hidden && localName.equals("path")) {
                pushTransform(atts);
                Properties props = new Properties(getParent(), atts);
                String fillRule = props.getString("fill-rule");
                Path p = new Path();
                if ("evenodd".equals(fillRule)) {
                    p.setFillEvenOdd();
                } else if ("nonzero".equals(fillRule)) {
                    p.setFillNonZero();
                }
                doPath(p, getStringAttr("d", atts));
                if (doFill(props, gradientMap)) {
                    doLimits(p);
                    canvas.drawPath(p, paint);
                }
                if (doStroke(props, gradientMap)) {
                    canvas.drawPath(p, strokePaint);
                }
                popTransform();
            } else if (!hidden) {
                //Log.d(TAG, "UNRECOGNIZED SVG COMMAND: " + localName);
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            // no-op
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
            if (localName.length() == 0) {
                localName = qName;
            }
	    popId();
            if (localName.equals("svg")) {
                popG();
            } else if (localName.equals("clipPath")) {
                canvas.endClip();
            } else if (localName.equals("symbol")) {
                canvas.endSymbol();
                canvas = canvasStack.pop();
            } else if (localName.equals("g")) {
                popG();
                popTransform();
            } else if (localName.equals("linearGradient")) {
                if (gradient.id != null) {
                    if (gradient.xlink != null) {
                        Gradient parent = gradientRefMap.get(gradient.xlink);
                        if (parent != null) {
                            gradient = parent.createChild(gradient);
                        }
                    }
                    gradient.isLinear = true;
                    int[] colors = new int[gradient.colors.size()];
                    for (int i = 0; i < colors.length; i++) {
                        colors[i] = gradient.colors.get(i);
                    }
                    float[] positions = new float[gradient.positions.size()];
                    for (int i = 0; i < positions.length; i++) {
                        positions[i] = gradient.positions.get(i);
                    }
                    if (colors.length == 0) {
                        //Log.d("BAD", "BAD");
                    }
                    LinearGradient g = new LinearGradient(gradient.x1, gradient.y1, gradient.x2, gradient.y2, colors, positions, Shader.TileMode.CLAMP);
                    g.userSpace = gradient.userSpace;
                    if (gradient.matrix != null) {
                        g.setLocalMatrix(gradient.matrix);
                    }
                    gradientMap.put(gradient.id, g);
                    gradientRefMap.put(gradient.id, gradient);
                }
            } else if (localName.equals("radialGradient")) {
                if (gradient.id != null) {
                    if (gradient.xlink != null) {
                        Gradient parent = gradientRefMap.get(gradient.xlink);
                        if (parent != null) {
                            gradient = parent.createChild(gradient);
                        }
                    }
                    gradient.isLinear = false;
                    int[] colors = new int[gradient.colors.size()];
                    for (int i = 0; i < colors.length; i++) {
                        colors[i] = gradient.colors.get(i);
                    }
                    float[] positions = new float[gradient.positions.size()];
                    for (int i = 0; i < positions.length; i++) {
                        positions[i] = gradient.positions.get(i);
                    }
                    RadialGradient g = new RadialGradient(gradient.x, gradient.y, gradient.x1, gradient.y1, gradient.radius, colors, positions, Shader.TileMode.CLAMP);
                    if (gradient.matrix != null) {
                        g.setLocalMatrix(gradient.matrix);
                    }
                    gradientMap.put(gradient.id, g);
                    gradientRefMap.put(gradient.id, gradient);
                }
            } else if (localName.equals("g")) {
                if (boundsMode) {
                    boundsMode = false;
                }
                // Break out of hidden mode
                if (hidden) {
                    hiddenLevel--;
                    //Util.debug("Hidden down: " + hiddenLevel);
                    if (hiddenLevel == 0) {
                        hidden = false;
                    }
                }
                // Clear gradient map
                gradientMap.clear();
            }
        }
    }
}
