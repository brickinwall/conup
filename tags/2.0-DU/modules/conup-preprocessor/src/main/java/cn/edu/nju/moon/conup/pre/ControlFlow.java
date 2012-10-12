package cn.edu.nju.moon.conup.pre;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
/**
 * 
 * 
 * @author <a href="mailto:njupsu@gmail.com">Su Ping</a>
 */
public class ControlFlow {

	List<flow> con = new LinkedList<flow>();

	public flow getFlow(int src) {
		Iterator<flow> f = con.iterator();
		while (f.hasNext()) {
			flow fn = f.next();
			if (fn.src == src) {
				return fn;
			}
		}
		return null;
	}

	public void addFlow(int src, int dst) {
		flow fl = getFlow(src);
		if (fl != null) {
			fl.setDst(dst);
		} else {
			con.add(new flow(src, dst));
		}
//		System.out.println(src+"->"+dst);
	}

	public int getDstSize(int src) {
		return getFlow(src).dst.size();
	}

	public List getDst(int src) {
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
		Iterator<flow> f = con.iterator();
		while (f.hasNext()) {
			flow fn = f.next();
			System.out.print(fn.src + "->");
			List d = getDst(fn.src);
			for (int i = 0; i < d.size(); i++) {
//				System.out.print(d.get(i) + ",");
			}
			System.out.println();
		}

	}

}

class flow {
	int src;
	boolean isWhile;
	List dst = new LinkedList();

	public flow(int src, int dst) {
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

	public List getDst() {
		return dst;
	}

	public boolean getIsWhile() {
		return isWhile;
	}

	public void setIsWhile(boolean w) {
		isWhile = w;
	}
}