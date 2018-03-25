package murex.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tree<T> implements Iterable<Tree<T>> {
	T data;
	String type;
	String command;
	Tree<T> parent;
	List<Tree<T>> children; // for parallel
	Tree<T> next;
	int level;

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
		childNode.level = this.level+1;
		this.children.add(childNode);
		return childNode;
	}

	public Tree<T> addSibling(T sibling)
	{
		Tree<T> sibNode = new Tree<T>(sibling);
		return addSibling(sibNode);
	}
	
	public Tree<T> addSibling(Tree<T> sibNode)
	{
		this.next = sibNode;
		sibNode.level = this.level;
		return sibNode;
	}
	
	public Tree<T> addSibling(T sibling, String type, String command)
	{
		Tree<T> sibNode = new Tree<T>(sibling);
		sibNode.type=type;
		sibNode.command = command;
		sibNode.parent = this.parent;
		return addSibling(sibNode);
	}
	
	public Iterator<Tree<T>> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

}
