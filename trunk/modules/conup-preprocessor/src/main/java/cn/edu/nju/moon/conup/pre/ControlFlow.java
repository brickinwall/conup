package cn.edu.nju.moon.conup.pre;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
/**
 * 
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class ControlFlow {
	private final static Logger LOGGER = Logger.getLogger(ControlFlow.class.getName());
	
	public static Logger getLogger() {
		return LOGGER;
	}

	List<NodeAndOutarcs> con = new LinkedList<NodeAndOutarcs>();

	public NodeAndOutarcs getFlow(int src) {
		Iterator<NodeAndOutarcs> f = con.iterator();
		while (f.hasNext()) {
			NodeAndOutarcs fn = f.next();
			if (fn.src == src) {
				return fn;
			}
		}
		return null;
	}

	public void addFlow(int src, int dst) {
		NodeAndOutarcs fl = getFlow(src);
		if (fl != null) {
			fl.setDst(dst);
		} else {
			con.add(new NodeAndOutarcs(src, dst));
		}
		LOGGER.fine(src+"->"+dst);
	}

	public int getDstSize(int src) {
		return getFlow(src).dst.size();
	}

	public List<Integer> getDst(int src) {
		return getFlow(src).getDst();
	}

	public List<AbstractInsnNode> getDstNode(MethodNode mn, int src) {
		List<AbstractInsnNode> dstnode = new LinkedList<AbstractInsnNode>();
		for (int i = 0; i < getDstSize(src); i++) {
			dstnode.add((AbstractInsnNode) mn.instructions
					.get((Integer) getDst(src).get(i)));
		}
		return dstnode;

	}

	public void showControlFlow() {
		Iterator<NodeAndOutarcs> f = con.iterator();
		while (f.hasNext()) {
			NodeAndOutarcs fn = f.next();
			System.out.print(fn.src + "->");
			List<Integer> d = getDst(fn.src);
			for (int i = 0; i < d.size(); i++) {
//				System.out.print(d.get(i) + ",");
			}
//			System.out.println();
		}

	}

}

class NodeAndOutarcs {
	int src;
	boolean isWhile;
	List<Integer> dst = new LinkedList<Integer>();

	public NodeAndOutarcs(int src, int dst) {
		this.src = src;
		this.dst.add(dst);
		isWhile = false;
	}

	public void setSrc(int s) {
		src = s;
	}

	public void setDst(int d) {
		dst.add(d);
	}

	public int getSrc() {
		return src;
	}

	public List<Integer> getDst() {
		return dst;
	}

	public boolean getIsWhile() {
		return isWhile;
	}

	public void setIsWhile(boolean w) {
		isWhile = w;
	}
}