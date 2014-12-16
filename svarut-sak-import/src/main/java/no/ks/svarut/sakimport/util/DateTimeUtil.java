package no.ks.svarut.sakimport.util;

import org.joda.time.DateTime;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


public class DateTimeUtil {

    private DateTimeUtil(){}

    public static XMLGregorianCalendar toGregorianCalendar(DateTime date) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(date.toGregorianCalendar());
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
