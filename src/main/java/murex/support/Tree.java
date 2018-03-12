package murex.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tree<T> implements Iterable<Tree<T>> {
	T data;
	String type;
	String command;
	Tree<T> parent;
	List<Tree<T>> children;

	public Tree(T data) {
	        this.data = data;
	        this.children = new ArrayList<Tree<T>>();
	    }

	public Tree<T> addChild(T child) {
		Tree<T> childNode = new Tree<T>(child);
		return addChild(childNode);
	}
	
	public Tree<T> addChild(T child, String type, String command) {
		Tree<T> childNode = new Tree<T>(child);
		childNode.type=type;
		childNode.command = command;
		return addChild(childNode);
	}
	
	public Tree<T> addChild(Tree<T> childNode) {
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}

	public Iterator<Tree<T>> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

}
