package murex.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
	static String crashed_action = "aSTG_FEED_DEALCRD";
	static Workflow wf = new Workflow();

	enum LAUNCHER {
		RERUN, XTR_CMD
	}

	public static void main(String[] args) {
		LAUNCHER lch = null;
		FILENAME = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
		Date today = new Date();
		String timestamp = today.getYear() + "_" + today.getMonth() + "_" + today.getDate();
		for (int i = 0; i < args.length; i = i + 2) {
			switch (args[i]) {
			case "-c":
				crashed_action = args[i + 1];
				FILENAME += "_" + crashed_action + "_" + timestamp + ".txt";
				lch = LAUNCHER.RERUN;
				break;
			case "-o":
				FILENAME = args[i + 1];
				break;
			case "-x":
				lch = LAUNCHER.XTR_CMD;
				break;
			default:
				break;
			}
		}

		if (lch == LAUNCHER.RERUN) {
			ProceedWorkflow();
		}

	}

	public static void ConvertXmlToTree() {
		wf = new Workflow();
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
		wf.Draw(crashed_action);
		WriteFile(wf._result);
	}

	public static void ExtractCommands(String[] commands) {
		ConvertXmlToTree();
		List<String> cmds = wf.ExtractCommand(commands);
		for (String cmd : cmds) {
			System.out.println(cmd);
		}
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
