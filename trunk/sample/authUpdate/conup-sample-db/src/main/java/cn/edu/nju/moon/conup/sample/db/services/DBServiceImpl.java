package cn.edu.nju.moon.conup.sample.db.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.oasisopen.sca.annotation.Service;

import cn.edu.nju.moon.conup.algorithm.VcAlgorithmImpl;
import cn.edu.nju.moon.conup.container.VcContainerImpl;
import cn.edu.nju.moon.conup.def.VcTransaction;
import cn.edu.nju.moon.conup.listener.ComponentListener;
import cn.edu.nju.moon.conup.listener.ComponentListenerImpl;
import com.mysql.jdbc.Statement;


@Service(DBService.class)
public class DBServiceImpl implements DBService {
	
	public DBServiceImpl(){
		System.out.println("New DBServiceImpl");
	}

	@Override
	@VcTransaction
	public List<String> dbOperation() {
		List<String> result = new ArrayList<String>();
//		ComponentListener listener = ComponentListenerImpl.getInstance();
//		Set<String> futureC = new HashSet<String>();
//		Set<String> pastC = new HashSet<String>();
//		String threadID = new Integer(Thread.currentThread().hashCode()).toString();
//		listener.notify("start", threadID, futureC, pastC);
//		
//		listener.notify("running", threadID, futureC, pastC);
		
		result.add("hello tuscany...");

//		listener.notify("end", threadID, futureC, pastC);
		return result;
		
//		String sql = "select * from info";
//		String driver = "com.mysql.jdbc.Driver"; 
//        String url = "jdbc:mysql://114.212.85.33:3306/user";
//        String user = "root"; 
//        String password = "artemis"; 
//        Connection conn;
//        Statement stmt;
//        ResultSet rs;
//        
//		try {
//			Class.forName(driver); 
//			conn = DriverManager.getConnection(url, user, password);
//			stmt = (Statement) conn.createStatement();
//			rs = stmt.executeQuery(sql);
//			
//			
//			while(rs.next()){
//				String str = "id: " + rs .getString("id")+ " name: " + rs.getString("name") + " passwd: "+ rs.getString("passwd");
//				result.add(str);
//				
//				System.out.println("user" + rs .getString("id") + " " + str);
//			}
//			rs.close();
//			stmt.close();
//			
//			if(!conn.isClosed()) 
//				conn.close(); 
//				//System.out.println("connect success��"); 
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		return result;
		 
	}

}
