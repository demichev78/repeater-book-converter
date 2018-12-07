package com.conversion;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class XmlParser extends DefaultHandler {

    public List<Marker> result = new ArrayList<>();

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (!qName.equalsIgnoreCase("marker")) return;
        Marker next = new Marker();
        next.freq = attributes.getValue("freq");
        next.longtitude = attributes.getValue("lng");
        next.latitude = attributes.getValue("lat");
        next.call = attributes.getValue("call");
        next.offset =  attributes.getValue("offset");
        next.pl = attributes.getValue("pl");
        next.location = attributes.getValue("location");
        result.add(next);
    }

}
