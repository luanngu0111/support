package murex.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
	static String INPUT = "resources/wf_eod_dwh.xml";
	static Tree<String> root = new Tree<String>("workflow");
	static Tree<String> first = root;
	static String OUTPUT = "resources/wf_eod_dwh_v3.txt";
	static String WORKFLOW = "resources/wf_eod_dwh.xml";
	static String crashed_action = "aSTG_FEED_DEALCRD";
	static Workflow wf = new Workflow();

	enum LAUNCHER {
		RERUN, XTR_CMD, XTR_LOG
	}

	public static void main(String[] args) {
		LAUNCHER lch = null;
		OUTPUT = WORKFLOW.substring(WORKFLOW.lastIndexOf(File.separator) + 1, WORKFLOW.lastIndexOf("."));
		String state_action = "state";
		Date today = new Date();
		String timestamp = today.getYear() + "_" + today.getMonth() + "_" + today.getDate();
		for (int i = 0; i < args.length; i = i + 2) {
			switch (args[i]) {
			case "-c":
				crashed_action = args[i + 1];
				OUTPUT += "_" + crashed_action + "_" + timestamp + ".txt";
				lch = LAUNCHER.RERUN;
				break;
			case "-i":
				INPUT = args[i + 1];
				break;
			case "-o":
				OUTPUT = args[i + 1];
				break;
			case "-x":
				lch = LAUNCHER.XTR_CMD;
				INPUT = args[i + 1];
				break;
			case "-log":
				lch = LAUNCHER.XTR_LOG;
				state_action = args[i + 1];
				break;
			case "-w":
				WORKFLOW = args[i + 1];
				break;
			default:
				System.out.println("The parameter is invalid. Please refer to below instruction:\r\n" + 
						"	-w : specify the workflow file\r\n" + 
						"	-c : specify the crashed action name\r\n" + 
						"	-i : input parameter, combine with \"-log\", that's input log file/folder. \r\n" + 
						"	-o : specify the output file name\r\n" + 
						"	-x : specify the action/file contains list of actions. This function extracts commands of actions. \r\n" + 
						"	-log : specify the level extracted. \"state\" or \"action\"");
				break;
			}
		}

		if (lch == LAUNCHER.RERUN) {
			ProceedWorkflow();
		} else if (lch == LAUNCHER.XTR_CMD) {
			String[] test = ReadFile(INPUT);
			ExtractCommands(test);
		} else if (lch == LAUNCHER.XTR_LOG) {
			ReadLog.ExtractLogInfo(INPUT, OUTPUT, state_action);
		}

	}

	public static String[] ReadFile(String path) {
		List<String> lst_act = new ArrayList<String>();
		BufferedReader br = null;
		FileReader fr = null;
		String[] rs = null;
		try {

			// br = new BufferedReader(new FileReader(FILENAME));
			fr = new FileReader(path);
			br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				lst_act.add(sCurrentLine);
			}

		} catch (IOException e) {

			lst_act.add(path);

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
			rs = new String[lst_act.size()];
			rs = lst_act.toArray(rs);
		}

		return rs;
	}

	public static void ConvertXmlToTree() {
		wf = new Workflow();
		String wf_name = WORKFLOW.substring(WORKFLOW.lastIndexOf("/") + 1, WORKFLOW.lastIndexOf("."));

		try {

			File fXmlFile = new File(WORKFLOW);
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
		String output = "";
		for (String cmd : cmds) {
			System.out.println(cmd);
			output += cmd + "\n";
		}
		WriteFile(output);
	}

	public static void WriteFile(String output) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			fw = new FileWriter(OUTPUT);
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
