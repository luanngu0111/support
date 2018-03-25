package murex.support;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Workflow {
	Tree<String> _root;
	Tree<String> _current_node;
	private static String space = "";
	private static boolean flag = false;

	String workflow_name;

	String _result;

	public Workflow() {
	}

	public Workflow(Tree<String> root) {
		this._root = root;
		this._root.level = 0;
		this._current_node = this._root;
		this._result = "";
	}

	public void resetNode() {
		this._current_node = _root;
	}

	public void attach(Tree<String> t_node, Node xml_node) {

		Element eElement = null;
		eElement = (Element) xml_node;
		if (xml_node.getNodeName().equals("action")) {
			Element param = (Element) xml_node.getFirstChild().getNextSibling();
			t_node.command = param.getAttribute("value");
			System.out.println(
					"-- Action: " + eElement.getAttribute("id") + " with command: " + param.getAttribute("value"));
		} else {
			String mode = eElement.getAttribute("mode");
			System.out.println("- State: " + eElement.getAttribute("id") + " Mode: " + eElement.getAttribute("mode"));
			Node first = xml_node.getFirstChild();
			Node last = xml_node.getLastChild().getPreviousSibling();
			if ("sequence".equals(mode)) {
				_current_node = t_node;
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
					Tree<String> node = new Tree<String>(eChild.getAttribute("id"));
					node.command = command;
					node.type = eChild.getNodeName();
					_current_node = _current_node.addSibling(node);
					// System.out.println("- State: " + _current_node.data);
					attach(node, child);
					// _current_node = node;
					// System.out.println("- State: " + _current_node.data);

				}
			} else if ("parallel".equals(mode)) {
				_current_node = t_node;
				for (Node child = first; child.getNextSibling() != null; child = child.getNextSibling()) {
					if (child.getAttributes() == null)
						continue;
					Element eChild = (Element) child;
					String command = "";
					if (child.getNodeName().equals("action")) {
						Element param = (Element) child.getFirstChild().getNextSibling();
						command = param.getAttribute("value");
					}
					Tree<String> node = new Tree<String>(eChild.getAttribute("id"));
					node.command = command;
					node.type = eChild.getNodeName();
					_current_node.addChild(node);
					// System.out.println("- State: " + _current_node.data);
					attach(node, child);
					_current_node = t_node;
					// System.out.println("- State: " + _current_node.data);

				}
			}
		}
	}

	public Tree<String> seek(Tree<String> tree, String action) {
		if (tree == null) {
			return null;
		}
		if (tree.children != null && tree.children.size() > 0) {
			for (Tree<String> child : tree.children) {
				if (child.data.equals(action))
					return child;
				seek(child, action);
			}

		}
		return seek(tree.next, action);
	}

	public void Draw() {
		Draw(_root);
	}

	public String Draw(Tree<String> tree) {
		space = space + "-";
		if (tree == null) {
			space = space.substring(0, space.length() - 1);
			return space;
		}
		System.out.println("State : " + tree.data);

		if (tree.children != null && tree.children.size() > 0) {
			for (Tree<String> child : tree.children) {
				System.out.print(space);
				space = Draw(child);
			}

		}
		Draw(tree.next);
		space = space.substring(0, space.length() - 1);
		return space;
	}

	public void Draw(String action) {
		_result = "";
		Draw(_root, action);
	}

	public String Draw(Tree<String> tree, String action) {
		// space = space + "-";
		String output = "";
		String comm = "";
		if (tree == null) {
			// space = space.substring(0, space.length() - 1);
			return space;
		}
		if (flag) {
			if (tree.command != null && tree.command.length() > 0)
				comm = "Command : " + tree.command;

			output = String.format("%s : %s  %s", tree.type, tree.data, comm);
			if (tree.type.equals("action"))
				output = tree.data + " = " + tree.command;
			if (tree.level == 0) {
				flag = false;
				System.out.println("Continue from workflow: $MXBIN/workflow/mx_launch_workflow.pl -w " + workflow_name
						+ " -c " + tree.data);
				_result += "Continue from workflow: $MXBIN/workflow/mx_launch_workflow.pl -w " + workflow_name + " -c "
						+ tree.data + "\n\n";
			} else {
				System.out.println(output);
				_result += output + "\n\n";
			}
		}

		// find out the crash action
		if (tree.data.equals(action)) {
			System.out.println("*** Rerun action : " + tree.data + " Command : " + tree.command.toString());
			_result += "*** Rerun action : " + tree.data + " Command : " + tree.command.toString() + "\n\n";
			System.out.println("--- NEXT STEP ---");
			_result += "--- NEXT STEP ---\n\n";
			flag = true;
		}

		if (tree.children != null && tree.children.size() > 0) {
			for (Tree<String> child : tree.children) {
				if (child.data.equals(action)) {
					flag = true;
					System.out.println("*** Rerun Action : " + action + " Command : \"" + child.command + "\"");
					_result += "*** Rerun Action : " + action + " Command : \"" + child.command + "\"" + "\n\n";
					System.out.println("--- NEXT STEP ---");
					_result += "--- NEXT STEP ---\n\n";
				}
				if (!flag) {
					Draw(child, action);
				}
			}

		}
		Draw(tree.next, action);
		// space = space.substring(0, space.length() - 1);
		return space;
	}

	public String ExtractCommand(String action) {
		return ExtractCommand(this._root, action);
	}

	public String ExtractCommand(Tree<String> tree, String action) {
		String result = "";
		if (tree == null) {
			return result;
		}
		if (tree.data.equals(action)) {
			return tree.command;
		}
		if (tree.children != null && tree.children.size() > 0) {
			for (Tree<String> child : tree.children) {
				if (child.data.equals(action)) {
					return child.command;
				}
				result = ExtractCommand(child, action);
				if (!result.equals(""))
				{
					return result;
				}
			}
		}
		result = ExtractCommand(tree.next, action);
		return result;

	}
	
	public List<String> ExtractCommand(String[] actions)
	{
		List<String> commands = new ArrayList<String>();
		for (String action: actions)
		{
			
			commands.add(action+ " = " + ExtractCommand(action));
		}
		return commands;
	}
	
	public List<String> ExtractCommand(List<String> actions)
	{
		String[] acts = new String[actions.size()];
		actions.toArray(acts);
		return ExtractCommand(acts);
	}
}
