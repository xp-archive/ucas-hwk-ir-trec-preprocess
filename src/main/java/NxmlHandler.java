import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author xp
 */
public class NxmlHandler extends DefaultHandler {

    private Stack<Node> qStack;
    private Node topNode;

    private Result result;

//    private final TransformerFactory transformerFactory;
//    private boolean raw;
//    private int rawDepth;
//    private ByteArrayOutputStream rawOut;
//    private TransformerHandler transformerHandler;
//
//    public NxmlHandler(TransformerFactory transformerFactory) {
//        this.transformerFactory = transformerFactory;
//    }

    @Override
    public void startDocument() throws SAXException {
        qStack = new Stack<>();
        result = new Result();
    }

    @Override
    public void endDocument() throws SAXException {
        if (!qStack.empty()) {
            throw new RuntimeException("stack is not empty!\n" + qStack.toString());
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        qStack.push(new Node(topNode, uri, localName, qName, attributes));
        topNode = qStack.peek();
    }



    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        new Node(topNode, Arrays.copyOfRange(ch, start, start + length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!qName.toLowerCase().equals(topNode.qName)) {
            throw new RuntimeException(String.format("el %s is not match with stack %s", qName, topNode.qName));
        }

        Map<String, String> attributeMap = topNode.attributeMap;

        if (check("article", "front", "article-meta", "article-id")) {
            String name = attributeMap.get("pub-id-type");
            result.ids.put(name, topNode.stringify());
        }
        if (check("article", "front", "article-meta", "subj-group", "subject")) {
            result.subjects.add(topNode.stringify());
        }
        if (check("article", "front", "article-meta", "title-group", "article-title")) {
            result.titles.add(topNode.stringify());
        }
        if (check("article", "front", "article-meta", "contrib-group", "contrib")) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", attributeMap.get("id"));
            map.put("type", attributeMap.get("contrib-type"));

            int index = getNodeIndex(topNode.children, "name");
            if (index > -1) {
                Node nameNode = topNode.children.get(index);
                map.put("name", nameNode.stringify());
            }
            result.contributors.add(map);
        }
        if (check("article", "front", "article-meta", "abstract")) {
            result.Abstract = topNode.stringify();
        }
        if (check("article", "front", "article-meta", "kwd-group", "kwd")) {
            result.keywords.add(topNode.stringify());
        }
        if (check("article", "body")) {
            String body = topNode.stringify();
            body = body.replaceAll("\\[.+?\\]", "");
            result.body = body;
        }
        if (check("article", "body", "xref")) {
            topNode.children.clear();
        }
//        if (check("article", "back", "ref-list", "ref")) {
//            result.refs.add(topNode.toMap());
//        }

        qStack.pop();
        try {
            topNode = qStack.peek();
        } catch (EmptyStackException e) {
            topNode = null;
        }
    }

    public Result getResult() {
        return result;
    }

    private int getNodeIndex(List<Node> list, String qName) {
        return list.indexOf(new Node(null, null, null, qName, null));
    }

    private boolean check(String... qs) {
        if (topNode == null || !topNode.qName.equals(qs[qs.length - 1])) {
            return false;
        }

        int pos = -1;
        for (int i = 0; i < qs.length - 1; i++) {
            int cur = getNodeIndex(qStack, qs[i]);
            if (cur <= pos) {
                return false;
            }
            pos = cur;
        }
        return true;
    }

//    private void rawBegin() {
//        try {
//            raw = true;
//            rawDepth = qStack.size();
//            Transformer transformer = transformerFactory.newTransformer();
//            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//            transformerHandler = new TransformerHandlerImpl((TransformerImpl) transformer);
//            rawOut = new ByteArrayOutputStream();
//            StreamResult rawResult = new StreamResult(rawOut);
//            transformerHandler.setResult(rawResult);
//            transformerHandler.startDocument();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private String rawEnd() {
//        try {
//            raw = false;
//            transformerHandler.endDocument();
//            return new String(rawOut.toByteArray(), StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            transformerHandler = null;
//            try {
//                rawOut.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            } finally {
//                rawOut = null;
//            }
//        }
//    }
}
