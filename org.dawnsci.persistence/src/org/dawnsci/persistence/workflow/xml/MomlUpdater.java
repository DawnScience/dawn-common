package org.dawnsci.persistence.workflow.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class used to read a workflow file with its _workbenchVersion. 
 * If it is less than 1.4 then remove all attributes called "Expression Mode".
 * 
 * @author wqk87977
 *
 */
public class MomlUpdater {

	private final Logger Logger = LoggerFactory.getLogger(MomlUpdater.class);
	private String filePath;

	public MomlUpdater() {
//		this.filePath = filePath;
	}

	/**
	 * Updates the Moml file after reading its workbench version
	 * and deleting its expression mode properties if necessary.
	 * @param filePath
	 *          The full file Path of the Moml file
	 */
	public void updateMoml(String filePath) {
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		factory.setValidating(false);
//		factory.setIgnoringElementContentWhitespace(true);
		try {
//			DocumentBuilder builder = factory.newDocumentBuilder();
//			File file = new File(filePath);
//			String xml = readFile(filePath, StandardCharsets.UTF_8);

//			Document doc = builder.parse(new InputSource(new StringReader(xml)));
			
			File fXmlFile = new File(filePath);  

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
			  
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			// uncomment the below lines to get rid of the 'connection refused:
			// connect' error:

			// dBuilder.setEntityResolver(new EntityResolver(){
			// public InputSource resolveEntity(String publicId, String
			// systemId){
			// return new InputSource(
			// new
			// ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
			// }});

			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("staff");
			System.out.println("-----------------------");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					System.out.println("Last Name : "
							+ getTagValue("lastname", eElement));
					System.out.println("Nick Name : "
							+ getTagValue("nickname", eElement));
					System.out.println("Salary : "
							+ getTagValue("salary", eElement));

				}
			}
//			Element workBenchVersionNode = doc.getElementById("property");
//			int length = workBenchVersionNode.getAttribute("_workbench");
//			System.out.println(length);
			
			// Do something with the document here.
		} catch (ParserConfigurationException e) {
			Logger.error("ParserConfigurationException:"+e);
		} catch (SAXException e) {
			Logger.error("SAXException:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.error("IOException:"+e);
		}
	}

	private String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

	public String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
}
