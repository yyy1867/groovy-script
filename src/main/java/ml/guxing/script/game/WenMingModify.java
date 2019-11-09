package ml.guxing.script.game;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class WenMingModify {

    private final static XPath XPATH;

    static {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPATH = xPathFactory.newXPath();
    }

    public static void main(String[] args) throws Exception {\
//        String path = "F:\\Game\\fengyunbianhuan\\fengyunbianhuan\\common\\Sid Meier's Civilization VI\\" +
//                "DLC\\Expansion2\\Data\\Expansion1_Governors.xml";
//        Document doc = readXmlBakToDocument(path);
//        modifyXml(doc);
//        compressionDocument(doc.getDocumentElement());
//        writeXml(path, doc);
//        System.out.println(doc);
        modifyDir("F:\\Game\\fengyunbianhuan\\fengyunbianhuan\\common\\Sid Meier's Civilization VI\\DLC");
        modifyDir("F:\\Game\\fengyunbianhuan\\fengyunbianhuan\\common\\Sid Meier's Civilization VI\\Base\\Assets\\Gameplay\\Data");
    }

    private static void modifyDir(String path) throws Exception {
        List<String> keys = Arrays.asList(new String[]{"Leaders", "Policies", "Civilizations", "Beliefs","Governors"});
        File dir = new File(path);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".xml")) {
                    for (String key : keys) {
                        if (file.getName().toLowerCase().indexOf(key.toLowerCase()) != -1) {
                            if (file.getPath().toLowerCase().indexOf("icons") == -1) {
                                if (file.getPath().toLowerCase().indexOf("\\data\\") != -1) {
                                    System.out.println(file.getPath());
                                    Document doc = readXmlBakToDocument(file.getPath());
                                    modifyXml(doc);
//                                    compressionDocument(doc.getDocumentElement());
                                    writeXml(file.getPath(), doc);
                                }
                            }
                        }
                    }
                } else if (file.isDirectory()) {
                    modifyDir(file.getPath());
                }
            }
        }
    }

    private static void modifyDir(String path) throws Exception {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File child : files) {
                if (child.isFile() && child.getName().endsWith(".xml")) {
                    System.out.println(child.getPath());
                    Document doc = readXmlBakToDocument(child.getPath());
                    modifyXml(doc);
                } else if (child.isDirectory()) {
                    modifyDir(child.getPath());
                }
            }
        }
    }

    private static void modifyXml(Document doc) throws Exception {
        String elevl = "//ModifierArguments//Row[Name='Amount']|//ModifierArguments//Row[@Name='Amount']";
        String typeevlfmt = "//Modifiers//Row[ModifierId='%s']|//Modifiers//Row[@ModifierId='%s']";
        List<Element> els = query(elevl, doc);
        for (Element el : els) {
            String modifierId = getVal("ModifierId", el);
            String val = getVal("Value", el);
            List<Element> types = query(String.format(typeevlfmt, modifierId, modifierId), doc);
            if (types != null && types.size() > 0) {
                String modifierType = getVal("ModifierType", types.get(0));
                String str = Joiner.on(":").join(modifierType, modifierId, val);
                modifyVal(val, el, str);
            }
        }
    }

    private static void modifyVal(String value, Element el, String msg) {
        String[] vals = value.split(",");
        int multiplier = 5;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].indexOf(".") != -1) {// 小数的处理形式
                System.out.println("---");
            } else {
                boolean lessZero = false;
                Integer num = Integer.valueOf(vals[i]);
                if (num < 0) {
                    num = num * -1;
                    lessZero = true;
                }
                if (num > 0 && num <= 100) {
                    num *= multiplier;
                } else if (num > 100) {
                    num = 100 + (num - 100) * multiplier;
                }
                if (lessZero) num *= -1;
                vals[i] = num.toString();
            }
        }
        value = Joiner.on(",").join(vals);
        if (el.hasAttribute("Value")) {
            el.setAttribute("Value", value);
        }
        NodeList nodes = el.getElementsByTagName("Value");
        if (nodes != null && nodes.getLength() > 0) {
            Node item = nodes.item(0);
            item.setTextContent(value);
        }
//            System.out.println(msg + ":" + Joiner.on(",").join(vals));
    }

    private static String getVal(String key, Element el) {
        String val = el.getAttribute(key);
        if (Strings.isNullOrEmpty(val)) {
            NodeList nodeList = el.getElementsByTagName(key);
            if (nodeList != null) {
                val = nodeList.item(0).getTextContent();
            }
        }
        return val;
    }

    private static List<Element> query(String eval, Document doc) throws Exception {
        List<Element> els = new ArrayList();
        NodeList nodeList = (NodeList) XPATH.evaluate(eval, doc, XPathConstants.NODESET);
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    els.add((Element) node);
                }
            }
        }
        return els;
    }

    private static void compressionDocument(Element parent) {
        Map<String, List<Element>> elsMap = new HashMap();
        List<Element> els = new ArrayList();
        List<Element> childs = findChilds(parent);
        for (Element child : childs) {
            if (findChilds(child).size() == 0 && child.getAttributes().getLength() == 0) {
                if (!elsMap.containsKey(child.getTagName())) elsMap.put(child.getTagName(), new ArrayList());
                elsMap.get(child.getTagName()).add(child);
            } else {
                els.add(child);
            }
        }
        for (Map.Entry<String, List<Element>> entry : elsMap.entrySet()) {
            if (entry.getValue().size() == 1) {
                Element el = entry.getValue().get(0);
                parent.setAttribute(el.getTagName(), el.getTextContent());
                parent.removeChild(el);
            } else {
                els.addAll(entry.getValue());
            }
        }
        for (Element el : els) {
            compressionDocument(el);
        }
    }

    private static List<Element> findChilds(Element parent) {
        List<Element> childs = new ArrayList();
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            Node node = parent.getChildNodes().item(i);
            if (node instanceof Element) {
                childs.add((Element) node);
            }
        }
        return childs;
    }

    private static void writeXml(String path, Document doc) throws Exception {
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        Transformer transformer = transformerFactory.newTransformer();
//        transformer.setOutputProperty(OutputKeys.ENCODING, "ascii");
//        transformer.transform(new DOMSource(doc), new StreamResult(new File(path)));
        OutputFormat outputFormat = new OutputFormat(doc);
        outputFormat.setEncoding("ascii");
        outputFormat.setIndent(2);
        outputFormat.setIndenting(true);
        outputFormat.setLineWidth(180);
//        StringWriter writer = new StringWriter();
        FileWriter writer = new FileWriter(new File(path));
        XMLSerializer serializer = new XMLSerializer(writer, outputFormat);
        serializer.serialize(doc);
        writer.close();
//        System.out.println(writer.toString());
    }

    private static Document readXmlBakToDocument(String path) throws Exception {
        Document doc = null;
        File file = new File(path);
        if (file.isFile()) {
            File bak = new File(path + ".bak");
            if (!bak.isFile()) {
                Files.copy(file, bak);
            }
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            doc = documentBuilder.parse(bak);
        }
        return doc;
    }
}
