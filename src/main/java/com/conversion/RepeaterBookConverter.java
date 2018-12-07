package com.conversion;

import org.codehaus.plexus.util.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RepeaterBookConverter implements IConverter {

    public static final String REPEATERBOOK_PROPERTIES = "repeaterbook.properties";
    public static final String BASE2_URL_PROPERTY_NAME = "base2.url";
    public static final String BASE70_URL_PROPERTY_NAME = "base70.url";
    public static final String STATE_PREFIX = "state.";
    public static final String KML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
            "<Document>\n";
    public static final String KML_POSTFIX =
            "</Document>\n" +
                    "</kml>";
    public static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    public static final String KML_EXTENSION = ".kml";
    public static final String M2_POSTFIX = "-2m";
    public static final String CM70_POSTFIX = "-70cm";
    public static final String RESULTS_FOLDER_NAME = "results";
    private HttpClient client = new HttpClient();

    @Override
    public void process() throws IOException {
        Properties converterProps = loadProperties();
        if (converterProps == null) throw new IOException("Cannot load properties");
        String baseUrl = converterProps.getProperty(BASE2_URL_PROPERTY_NAME);
        if (StringUtils.isEmpty(baseUrl)) {
            throw new IOException("Cannot locate property " + BASE2_URL_PROPERTY_NAME);
        }
        process(converterProps, baseUrl, M2_POSTFIX);

        baseUrl = converterProps.getProperty(BASE70_URL_PROPERTY_NAME);
        if (StringUtils.isEmpty(baseUrl)) {
            throw new IOException("Cannot locate property " + BASE70_URL_PROPERTY_NAME);
        }
        process(converterProps, baseUrl, CM70_POSTFIX);
    }

    private void process(Properties converterProps, String baseUrl, String postfix) throws IOException {
        Map<String, Integer> stateList = extractStateList(converterProps);
        for (Map.Entry<String, Integer> next : stateList.entrySet()) {
            String nextUrl = baseUrl.replace("{state-id}", next.getValue().toString());
            String payload = client.getXmlPayload(nextUrl);
            payload = XML_PREFIX + payload;
            try {
                writeKmlFile(generateXmlKml(parseXml(payload)), next.getKey() + postfix + KML_EXTENSION);
            } catch (Exception e) {
                System.out.println("An exception occurred while trying to process " + next.getKey());
                e.printStackTrace();
            }
        }
    }

    private void writeKmlFile(String payload, String fileName) throws IOException {
        File directory = new File(RESULTS_FOLDER_NAME);
        if (! directory.exists()){
            if (!directory.mkdir()) {
                throw new IOException("Cannot create folder " + RESULTS_FOLDER_NAME);
            }
        }
        Files.write(Paths.get(RESULTS_FOLDER_NAME + File.separator + fileName), payload.getBytes());
    }

    private String generateXmlKml(List<Marker> markers) {
        StringBuilder sb = new StringBuilder();
        sb.append(KML_PREFIX);
        for (Marker next : markers) {
            sb.append("<Placemark>");
            sb.append("<name>CS:" + clean(next.call) + ":LOC:" + clean(next.location) + ":FRQ:" + clean(next.freq) + ":OFF:" + next.offset + ":PL:" + next.pl + "</name>");
            sb.append("<Point>");
            sb.append("<coordinates>" + next.longtitude + "," + next.latitude + "</coordinates>");
            sb.append("</Point>");
            sb.append("</Placemark>");

        }
        sb.append(KML_POSTFIX);
        return sb.toString();
    }

    private static String clean(String origin) {
        return origin.replace('&', ' ');
    }

    public List<Marker> parseXml(String payload) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XmlParser xmlParser = new XmlParser();
        try (InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8))) {
            saxParser.parse(stream, xmlParser);
        }
        return xmlParser.result;

    }

    private Map<String, Integer> extractStateList(Properties properties) {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry next : properties.entrySet()) {
            if (!next.getKey().toString().startsWith(STATE_PREFIX)) continue;
            String keyName = next.getKey().toString();
            String stateName = keyName.substring(STATE_PREFIX.length());
            try {
            result.put(stateName, Integer.parseInt(next.getValue().toString()));
            } catch (Exception e) {
                System.out.println("Cannot parse numeric state code " + next.getValue() + " for " + next.getKey());
            }
        }
        return result;
    }

    private Properties loadProperties() throws IOException {
        Properties result = new Properties();
        ClassLoader classLoader = RepeaterBookConverter.class.getClassLoader();
        URL propsUrl = classLoader.getResource(REPEATERBOOK_PROPERTIES);
        if (propsUrl == null) throw new IOException("Cannot locate file " + REPEATERBOOK_PROPERTIES);
        result.load(propsUrl.openStream());
        return result;
    }

}
