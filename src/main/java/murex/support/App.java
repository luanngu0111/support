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
	static String path = "resources/wf_eod_extract_daily.xml";
	static Tree<String> root = new Tree<String>("workflow");
	static Tree<String> first = root;
	static String FILENAME = "resources/wf_eod_live_v4.txt";

	public static Tree<String> Loop_v2(Tree<String> tree_node, Node stateNode) {
		Element eElement = null;
		eElement = (Element) stateNode;
		Tree<String> node = tree_node;
		if (stateNode.getNodeName().equals("action")) {
			Element param = (Element) stateNode.getFirstChild().getNextSibling();
			node.command = param.getAttribute("value");
			System.out.println(
					"-- Action: " + eElement.getAttribute("id") + " with command: " + param.getAttribute("value"));
		} else {
			String mode = eElement.getAttribute("mode");
			System.out.println("- State: " + eElement.getAttribute("id") + " Mode: " + eElement.getAttribute("mode"));
			Node first = stateNode.getFirstChild();
			Node last = stateNode.getLastChild().getPreviousSibling();
			if ("sequence".equals(mode)) {
				for (Node child = first; child.getNextSibling() != null; child = child.getNextSibling()) {
					if (child.getAttributes() == null)
						continue;
					Element eChild = (Element) child;
					String command = "";
					if (child.getNodeName().equals("action")) {
						if (child.getFirstChild() != null) {
							Element param = (Element) child.getFirstChild().getNextSibling();
							command = param.getAttribute("value");
						}
					}
					node = tree_node.addChild(eChild.getAttribute("id"), child.getNodeName(), command);
					tree_node = Loop_v2(node, child);
				}
			} else if ("parallel".equals(mode)) {
				for (Node child = first; child.getNextSibling() != null; child = child.getNextSibling()) {
					if (child.getAttributes() == null)
						continue;
					Element eChild = (Element) child;
					String command = "";
					if (child.getNodeName().equals("action")) {
						Element param = (Element) child.getFirstChild().getNextSibling();
						command = param.getAttribute("value");
					}
					node = tree_node.addChild(eChild.getAttribute("id"), child.getNodeName(), command);
					if (child.isEqualNode(last)) {
						tree_node = Loop_v2(tree_node, child);
						node = tree_node;
					} else
						Loop_v2(tree_node, child);
				}
			}
		}
		return node;
	}

	static String space = "";
	static String output = "";

	public static String DrawTree(Tree<String> tree) {
		space = space + "-";
		// System.out.println(space+tree.data);
		output += space + tree.data + "\n";
		if (tree.children.size() == 0) {
			if (space.length() > 1)
				space = space.substring(0, space.length() - 1);
			return space;
		}
		for (Tree<String> t : tree.children) {
			space = DrawTree(t);
		}
		space = space.substring(0, space.length() - 1);
		return space;
	}

	static boolean start = false;
	static Tree<String> pivot = null;

	public static Tree<String> seek(Tree<String> tree, String fromAction) {
		Tree<String> result = null;
		if (tree.children.size() == 0 || tree.data.equals(fromAction)) {
			result = tree;
		} else {
			for (Tree<String> branch : tree.children) {
				if (branch.data.equals(fromAction)) {
					pivot = branch;
					return branch;
				}

				result = seek(branch, fromAction);
			}
		}
		return result;
	}

	public static void PrintNextAction(Tree<String> last_sibling) {
		if ("action".equals(last_sibling.type))
			System.out.println(last_sibling.data + " Command: " + last_sibling.command);
		else {
			System.out.println(last_sibling.data);
		}
		if (last_sibling.children.size() == 0)
			return;
		Tree<String> baby_nephew = last_sibling.children.get(last_sibling.children.size() - 1);
		PrintNextAction(baby_nephew);

	}

	public static void PrintNextAction_State(Tree<String> last_sibling) {

		if ("state".equals(last_sibling.type)) {
			System.out.println("Next State : ");
			System.out.println("$MXBIN/workflow/mx_launch_workflow.pl -c " + last_sibling.data);
			return;
		}

		Tree<String> baby_nephew = last_sibling.children.get(last_sibling.children.size() - 1);
		PrintNextAction_State(baby_nephew);

	}

	public static void PrintNextState(Tree<String> grand) {
		Tree<String> last_child = grand.children.get(grand.children.size() - 1);
		System.out.println("$MXBIN/workflow/mx_launch_workflow.pl -c " + last_child.data);
		// while (last_child.children.size()>0)
		// {
		// System.out.println(last_child.data);
		// last_child = last_child.children.get(last_child.children.size()-1);
		// }
		//

	}

	public static void proceedWorkflow(Tree<String> tree, String fromAction, boolean isshow) {
		seek(tree, fromAction);
		System.out.println("Use the suggestion to proceed workflow");
		// System.out.println(pivot.data);
		Tree<String> current = pivot;
		while (current != null) {
			System.out.println(current.data + " Command : " + current.command);
			if (current.children.size() > 0)
				current = current.children.get(current.children.size() - 1);
			else
				current = null;
		}

		Tree<String> ancest = pivot.parent;
		Tree<String> grand = ancest.parent;
		Tree<String> baby_sibling = ancest.children.get(ancest.children.size() - 1);

		if (ancest.equals(grand.children.get(grand.children.size() - 1))) {
			PrintNextAction_State(baby_sibling);
		} else {
			System.out.println("Next action: ");
			PrintNextAction(baby_sibling);
			System.out.println("Next state: ");
			PrintNextState(ancest.parent);
		}
	}

	public static void main(String[] args) {

		try {

			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("state");
			root.data = doc.getDocumentElement().getAttribute("id");
			System.out.println("----------------------------");

			for (Node nNode = nList.item(0); nNode.getNextSibling() != null; nNode = nNode.getNextSibling()) {
				root = first;
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
				// if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				System.out.println("State : " + eElement.getAttribute("id"));
				System.out.println("Mode: " + eElement.getAttribute("mode"));
				// Loop(nNode);
				Loop_v2(root, nNode);
			}
			DrawTree(first);
			WriteFile();
			proceedWorkflow(first, "aDEL_DIV_BEFORE", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void WriteFile() {
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
