import java.util.*;

import org.xml.sax.Attributes;

/**
 * @author xp
 */
public class Node {

    public final boolean element;

    public String uri = null;

    public String localName = null;

    public String qName = null;

    public Map<String, String> attributeMap = null;

    public Node parent = null;

    public List<Node> children = null;

    public char[] value = null;

    public Node(Node parent, String uri, String localName, String qName, Attributes attributes) {
        this.element = true;
        this.uri = uri;
        this.localName = localName;
        this.qName = qName.toLowerCase();
        if (attributes != null) {
            Map<String, String> attributeMap = new LinkedHashMap<>();
            for (int i = 0, size = attributes.getLength(); i < size; ++i) {
                attributeMap.put(attributes.getQName(i), attributes.getValue(i));
            }
            this.attributeMap = attributeMap;
        }
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
        this.children = new LinkedList<>();
    }

    public Node(Node parent, char[] value) {
        this.element = false;
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return qName.equals(node.qName);
    }

    @Override
    public int hashCode() {
        return qName.hashCode();
    }

    public String stringify() {
        if (element) {
            StringBuilder sb = new StringBuilder();
            for (Node child : children) {
                sb.append(child.stringify()).append(" ");
            }
            return sb.toString().trim();
        } else {
            return new String(value);
        }
    }

    public Map<String, Object> toMap() {
        if (!element) {
            throw new IllegalStateException();
        }
        Map<String, Object> map = new HashMap<>(attributeMap);
        map.put("key", qName);

        boolean simplified = false;
        if (this.children.size() == 1) {
            Node child = this.children.iterator().next();
            if (!child.element) {
                map.put("value", child.stringify());
                simplified = true;
            }
        }
        if (!simplified) {
            List<Object> children = new ArrayList<>(this.children.size());
            for (Node child : this.children) {
                if (child.element) {
                    children.add(child.toMap());
                } else {
                    children.add(child.stringify());
                }
            }
            map.put("children", children);
        }

        return map;
    }
}
