package murex.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Hello world!
 *
 */
public class App {
	static String path = "resources/wf_eod_dwh.xml";
	static Tree<String> root = new Tree<String>("workflow");
	static Tree<String> first = root;
	static String FILENAME = "resources/wf_eod_dwh_v3.txt";
	static Workflow wf = new Workflow();

	public static void main(String[] args) {
		ProceedWorkflow();

	}

	public static void ConvertXmlToTree() {
		String wf_name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
		try {

			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("state");
			root.data = doc.getDocumentElement().getAttribute("id");
			System.out.println("----------------------------");

			wf._root = first;
			wf.workflow_name = wf_name;
			for (Node nNode = nList.item(0); nNode.getNextSibling() != null; nNode = nNode.getNextSibling()) {

				System.out.println("\nCurrent Element :" + nNode.getNodeName());
				// if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				System.out.println("State : " + eElement.getAttribute("id"));
				System.out.println("Mode: " + eElement.getAttribute("mode"));
				wf.attach(first, nNode);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ProceedWorkflow() {
		ConvertXmlToTree();
		System.out.println("------------------------Print Action-----------------------------");
		wf.Draw("aSTG_FEED_DEALCRD");
		WriteFile(wf._result);
	}

	public static void WriteFile(String output) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			fw = new FileWriter(FILENAME);
			bw = new BufferedWriter(fw);
			bw.write(output);

			System.out.println("Done");

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
	}

}
