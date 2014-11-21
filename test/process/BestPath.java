/**
 * 2014-8-18
 * jiaoqishun
 */
package test.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 2014-8-18 author jiaoqishun
 */
public class BestPath {
	
	static int INF  = Integer.MAX_VALUE /100 ;
	
	static int TOTAL = 100000;

	public static void main(String[] args) throws SQLException {
		BestPath bp = new BestPath();
		HashMap<String, Integer> recordMap = bp.getRecordMap();
		int size =91;	//一共有91个港口
		int[][] routeGraph = new int[size][size];
		bp.initRouteMap(routeGraph, recordMap);
		Dijkstra dijk = new Dijkstra(routeGraph);
		//resultMap存放各个港口的路径，注意，其中的编号是从0开始的，使用的时候需要加一
		HashMap<String, String> resultMap = new HashMap<String, String>();
		for(int i= 0;i < size; i++){
			HashMap<String, String> nodePathMap = dijk.calculate(i);
			if(nodePathMap == null){
				continue;	//有可能到其他点信息没有录入进去而返回null
			}else{
				resultMap.putAll(nodePathMap);	//将以i为起点的最短路径信息放入到Map中
			}
		}
		HashMap<String, String> rollPathMap = bp.getRollPathMap(resultMap);
		HashMap<String, Integer> bestLineMap = bp.getBestLineMap(rollPathMap, recordMap);
		for(int i=0; i< size; i++){
			HashMap<String, Integer> tempMap = bp.getIndexMap(bestLineMap, i);
			if(tempMap.size() >0){
				String bestMoney = bp.bestMoney(tempMap);
				if(bestMoney != null){
					System.out.println("当前节点"+(i+1)+"--"+bp.selectPortName(i+1)+"  最优路径为：");
					System.out.println(bestMoney);
				}
			}
			
		}
		
	}

	/**
	 * @param tempMap
	 * @return
	 * @throws SQLException 
	 */
	private String bestMoney(HashMap<String, Integer> tempMap) throws SQLException {
		// TODO Auto-generated method stub
		Iterator<Entry<String, Integer>> iter = tempMap.entrySet().iterator();
		int max =0; 
		String maxStr = null;
		while(iter.hasNext()){
			Entry<String, Integer> entry = iter.next();
			String key = entry.getKey();
			String path = key.substring(key.indexOf(":")+1);
			if(path.split("--").length <=10){
				int value = entry.getValue();
				if(max < value){
					max = value;
					maxStr = path;
				}
			}
		}
		if(maxStr == null){
			return null;
		}
		return "最大值："+max+"**"+getPortName(maxStr);
	}

	/**
	 * @param maxStr
	 * @return
	 * @throws SQLException 
	 */
	private String getPortName(String maxStr) throws SQLException {
		// TODO Auto-generated method stub
		String[] ids = maxStr.split("--");
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i< ids.length; i++){
			int id = Integer.parseInt(ids[i])+1;
			sb.append(selectPortName(id)+"--");
		}
		int id  = Integer.parseInt(ids[0])+1;
		sb.append(selectPortName(id));
		return sb.toString();
	}

	/**
	 * @param id
	 * @return
	 * @throws SQLException 
	 */
	private String selectPortName(int id) throws SQLException {
		// TODO Auto-generated method stub
		String portName =null;
		if(SimpleConnectionPool.CONNECTSTATUS){
			Connection con = SimpleConnectionPool.getConnection();
			String sqlStr = "select name, location from table_port where id =?";
			PreparedStatement pstmt = con.prepareStatement(sqlStr);
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				portName= rs.getString(1)+"("+rs.getString(2)+")";
			}
			closeConnectionSafe(con);
			rs.close();
			pstmt.close();
		}
		return portName;
	}

	/**
	 * @param bestLineMap
	 * @param i
	 * @return
	 */
	private HashMap<String, Integer> getIndexMap(
			HashMap<String, Integer> bestLineMap, int i) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> returnMap = new HashMap<String, Integer>();
		Iterator<Entry<String, Integer>> iter = bestLineMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, Integer> entry = iter.next();
			String key = entry.getKey();
			String path = key.substring(key.indexOf(":"));
			if(key.substring(0, key.indexOf("-")).equals(i+"")){
				returnMap.put(i+path, entry.getValue());
//				System.out.println(i+path+":"+ entry.getValue());
			}
		}
		return returnMap;
	}

	/**
	 * @param rollPathMap
	 * @param recordMap
	 * @return
	 */
	private HashMap<String, Integer> getBestLineMap(
			HashMap<String, String> rollPathMap,
			HashMap<String, Integer> recordMap) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> returnMap = new HashMap<String, Integer>();
		Iterator<Entry<String, String>> iter = rollPathMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			int sum = getSum(value, recordMap);
			returnMap.put(key+":"+value, sum);
//			System.out.println(key+":"+value+":"+sum);
		}
		return returnMap;
	}

	/**
	 * @param value
	 * @param recordMap
	 * @return
	 */
	private int getSum(String value, HashMap<String, Integer> recordMap) {
		// TODO Auto-generated method stub
		int sum = 0;
		String[] ids = value.split("--");
		for(int i =0;i < ids.length; i++){
			int start = Integer.parseInt(ids[i])+1;
			int end = 0;
			if(i == ids.length-1){
				end = Integer.parseInt(ids[0])+1;
			}else{
				end = Integer.parseInt(ids[i+1])+1;
			}
			String key = start+"-" + end;
			try{
				sum+=recordMap.get(key);
			}catch(NullPointerException e){
				System.out.println(key);
			}
		}
		return sum;
	}

	/**
	 * 根据总的路径表，找到一个完整的回路路径表
	 * @param resultMap
	 * @return key: 起始点-某点-起始点； Value：经过的所有点
	 */
	private HashMap<String, String> getRollPathMap(
			HashMap<String, String> resultMap) {
		// TODO Auto-generated method stub
		Iterator<Entry<String,String>> iter = resultMap.entrySet().iterator();
		HashMap<String, String> returnMap = new  HashMap<String, String>();
		ArrayList<String> existKeyList = new ArrayList<String>();	//1-2-1和2-1-2是同一个路径，总的距离是相等的，因此我们没有必要做两次
		while(iter.hasNext()){
			Entry<String,String> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue();
			String rollKey = getRollKey(key);
			String newValue = null;
			if(resultMap.containsKey(rollKey) && !existKeyList.contains(rollKey)){
				newValue = resultMap.get(rollKey);
			}
			if(newValue != null){
				String wholeKey = getWholeKey(key);
//				System.out.println(wholeKey + "路径节点："+value+newValue);
				returnMap .put(wholeKey, value+newValue);
				existKeyList.add(key);
			}
		}
		return returnMap;
	}

	/**
	 * 得到一个完整的回路i-j-i的形式
	 * @param key
	 * @return
	 */
	private String getWholeKey(String key) {
		// TODO Auto-generated method stub
		String[] ids = key.split("-");
		String wholeKey = ids[0] +"-"+ids[1]+"-"+ids[0];
		return wholeKey;
	}

	/**
	 * 将两个节点反转
	 * @param key <起始节点-终止节点>
	 * @return key<终止节点-起始节点>
	 */
	private String getRollKey(String key) {
		// TODO Auto-generated method stub
		String[] ijStr = key.split("-");
		String newKey = ijStr[1]+"-"+ijStr[0];
		return newKey;
	}

	/**
	 * 初始化港口之间的收益信息图，因为要使用GIS中的Dijkstra距离算法，需要知道每个节点间的距离（此处距离用TOTAL-利润时间比来表示）
	 * @param routeGraph
	 * @param recordMap
	 */
	private void initRouteMap(int[][] routeGraph,
			HashMap<String, Integer> recordMap) {
		// TODO Auto-generated method stub
		for(int i=0; i < routeGraph.length; i++){
			for(int j = 0; j < routeGraph.length; j++){
				if(i == j){
					routeGraph[i][j] = INF;
					continue;
				}
				String temp = (i+1) + "-" + (j+1);
				if(recordMap.containsKey(temp)){
					//此处取一个差值，是为了应用最短距离公式
					routeGraph[i][j] = TOTAL - recordMap.get(temp);
				}else{
					routeGraph[i][j] = INF;
				}
			}
		}
	}

	/**
	 * 获取记录的港口与港口之间贸易的利润时间比等信息<br>
	 * Key:fromPortId-toPortId<br>
	 * Value:利润时间比<br>
	 * @return
	 * @throws SQLException
	 */
	private HashMap<String, Integer> getRecordMap() throws SQLException {
		if (SimpleConnectionPool.CONNECTSTATUS) {
			Connection con = SimpleConnectionPool.getConnection();
			PreparedStatement pstmt = null;
			String selectSqlStr = "select * from table_netincome";
			pstmt = con.prepareStatement(selectSqlStr);
			ResultSet rs = pstmt.executeQuery();
			HashMap<String, Integer> recordMap = new HashMap<String, Integer>();
			while (rs.next()) {
				String fromPort = rs.getString(1);
				String toPort = rs.getString(2);
				float ratio = rs.getFloat(5);
				int fromPortId = selectPortId(fromPort);
				int toPortId = selectPortId(toPort);
				if (fromPortId > 0 && toPortId > 0) {
					if(ratio > 70){
						recordMap.put(fromPortId + "-" + toPortId, (int) ratio);
					}
				} else {
					return null;
				}
			}
			return recordMap;
		}
		return null;
	}

	/**
	 * @param portName
	 * @return
	 * @throws SQLException
	 */
	private int selectPortId(String portName) throws SQLException {
		Connection con = SimpleConnectionPool.getConnection();
		PreparedStatement pstmt = null;
		String selecSqlStr = "select id from table_port where name = ?";
		pstmt = con.prepareStatement(selecSqlStr);
		pstmt.setString(1, portName);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			int id = rs.getInt(1);
			rs.close();
			pstmt.close();
			closeConnectionSafe(con);
			return id;
		}
		return -1;
	}

	/**
	 * @param con
	 */
	private void closeConnectionSafe(Connection con) {
		// TODO Auto-generated method stub
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
