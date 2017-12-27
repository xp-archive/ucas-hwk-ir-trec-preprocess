import java.io.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author xp
 */
public class NxmlParser {

    private final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

    public NxmlParser() {
        try {
            parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            parserFactory.setValidating(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Result parse(Supplier<Reader> supplier) throws IOException {
        try (Reader reader = supplier.get()) {
            SAXParser saxParser = parserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();

            //NxmlHandler handler = new NxmlHandler(SAXTransformerFactory.newInstance());
            NxmlHandler handler = new NxmlHandler();
            xmlReader.setContentHandler(handler);

            xmlReader.parse(new InputSource(reader));
            return handler.getResult();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
