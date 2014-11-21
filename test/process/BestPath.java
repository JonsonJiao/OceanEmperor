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
		int size =91;	//һ����91���ۿ�
		int[][] routeGraph = new int[size][size];
		bp.initRouteMap(routeGraph, recordMap);
		Dijkstra dijk = new Dijkstra(routeGraph);
		//resultMap��Ÿ����ۿڵ�·����ע�⣬���еı���Ǵ�0��ʼ�ģ�ʹ�õ�ʱ����Ҫ��һ
		HashMap<String, String> resultMap = new HashMap<String, String>();
		for(int i= 0;i < size; i++){
			HashMap<String, String> nodePathMap = dijk.calculate(i);
			if(nodePathMap == null){
				continue;	//�п��ܵ���������Ϣû��¼���ȥ������null
			}else{
				resultMap.putAll(nodePathMap);	//����iΪ�������·����Ϣ���뵽Map��
			}
		}
		HashMap<String, String> rollPathMap = bp.getRollPathMap(resultMap);
		HashMap<String, Integer> bestLineMap = bp.getBestLineMap(rollPathMap, recordMap);
		for(int i=0; i< size; i++){
			HashMap<String, Integer> tempMap = bp.getIndexMap(bestLineMap, i);
			if(tempMap.size() >0){
				String bestMoney = bp.bestMoney(tempMap);
				if(bestMoney != null){
					System.out.println("��ǰ�ڵ�"+(i+1)+"--"+bp.selectPortName(i+1)+"  ����·��Ϊ��");
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
		return "���ֵ��"+max+"**"+getPortName(maxStr);
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
	 * �����ܵ�·�����ҵ�һ�������Ļ�··����
	 * @param resultMap
	 * @return key: ��ʼ��-ĳ��-��ʼ�㣻 Value�����������е�
	 */
	private HashMap<String, String> getRollPathMap(
			HashMap<String, String> resultMap) {
		// TODO Auto-generated method stub
		Iterator<Entry<String,String>> iter = resultMap.entrySet().iterator();
		HashMap<String, String> returnMap = new  HashMap<String, String>();
		ArrayList<String> existKeyList = new ArrayList<String>();	//1-2-1��2-1-2��ͬһ��·�����ܵľ�������ȵģ��������û�б�Ҫ������
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
//				System.out.println(wholeKey + "·���ڵ㣺"+value+newValue);
				returnMap .put(wholeKey, value+newValue);
				existKeyList.add(key);
			}
		}
		return returnMap;
	}

	/**
	 * �õ�һ�������Ļ�·i-j-i����ʽ
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
	 * �������ڵ㷴ת
	 * @param key <��ʼ�ڵ�-��ֹ�ڵ�>
	 * @return key<��ֹ�ڵ�-��ʼ�ڵ�>
	 */
	private String getRollKey(String key) {
		// TODO Auto-generated method stub
		String[] ijStr = key.split("-");
		String newKey = ijStr[1]+"-"+ijStr[0];
		return newKey;
	}

	/**
	 * ��ʼ���ۿ�֮���������Ϣͼ����ΪҪʹ��GIS�е�Dijkstra�����㷨����Ҫ֪��ÿ���ڵ��ľ��루�˴�������TOTAL-����ʱ�������ʾ��
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
					//�˴�ȡһ����ֵ����Ϊ��Ӧ����̾��빫ʽ
					routeGraph[i][j] = TOTAL - recordMap.get(temp);
				}else{
					routeGraph[i][j] = INF;
				}
			}
		}
	}

	/**
	 * ��ȡ��¼�ĸۿ���ۿ�֮��ó�׵�����ʱ��ȵ���Ϣ<br>
	 * Key:fromPortId-toPortId<br>
	 * Value:����ʱ���<br>
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
