package test.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 2014-8-8 author jiaoqishun
 */
public class GetInfo {
	static String driverName = "com.mysql.jdbc.Driver";
	// "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	static String dbURL = "jdbc:mysql://127.0.0.1:3306/hanghaizhengba";
	// "jdbc:sqlserver://127.0.0.1:1433;DatabaseName=Hanghaizhengba";
	static String userName = "root";
	// "sa";
	static String userPwd = "1234dhcc";

	public static String getInfo(String hotGoodsName) {
		String trimStr = hotGoodsName.replaceAll(" +", "");
		String str = selectLine(trimStr);
		return str;
	}

	private static String selectLine(String hotGoodsName) {
		System.out.println(hotGoodsName);
		if (hotGoodsName != null && hotGoodsName.length() > 0) {
			boolean testDriverFlag = testDriver();
			if (testDriverFlag) {
				String sqlSelectExportName = "select name from Table_Export where export0 = ? or export1 = ? or export2 = ?"
						+ " or export3 = ? or export4 = ?";
				String sqlSelectImportName = "select name from Table_Import where import0 = ? or import1 = ? or import2 = ?"
						+ " or import3 = ? or import4 = ?";
				// String sqlSelectPortInfo =
				// "select location,size,importType from Table_Port where name = ?";
				PreparedStatement pstmt = null;
				Connection con = null;
				try {
					con = DriverManager.getConnection(dbURL, userName, userPwd);
					pstmt = con.prepareStatement(sqlSelectExportName);
					int index = 1;
					pstmt.setString(index++, hotGoodsName);
					pstmt.setString(index++, hotGoodsName);
					pstmt.setString(index++, hotGoodsName);
					pstmt.setString(index++, hotGoodsName);
					pstmt.setString(index++, hotGoodsName);
					ResultSet rs = pstmt.executeQuery();
					String exportName = null;
					while (rs.next()) {
						exportName = rs.getString(1);
					}
					if (exportName == null) {
						return "这个货物根本没的卖，请检查输入的货物是否真实有效！";
					} else {
						pstmt.close();
						pstmt = con.prepareStatement(sqlSelectImportName);
						index = 1;
						pstmt.setString(index++, hotGoodsName);
						pstmt.setString(index++, hotGoodsName);
						pstmt.setString(index++, hotGoodsName);
						pstmt.setString(index++, hotGoodsName);
						pstmt.setString(index++, hotGoodsName);
						ResultSet rsImportName = pstmt.executeQuery();
						String importName = null;
						while (rsImportName.next()) {
							importName = rsImportName.getString(1);
						}
						rsImportName.close();
						pstmt.close();
						PortBean exportBean = selectPortInfo(exportName);
						if (importName == null) {
							// 当没有港口特别需要的时候，需要根据输入的货物类型来寻找最佳的出口港口，条件1该货物在该港口特别受欢迎，条件2距离当前港口最远；
							PortBean importBean = selectBestImportInfo(
									exportBean, hotGoodsName);
							return "你被耍了， 这个货物根本没有港口特别需要！<br>" + "购买地："
									+ exportBean.getLocation() + "-"
									+ exportName + "-"
									+ exportBean.getImportType() + "<br>"
									+ "建议销售地：" + importBean.getLocation() + "-"
									+ importBean.getName();
						} else {
							PortBean importBean = selectPortInfo(importName);

							return "购买地：" + exportBean.getLocation() + "-"
									+ exportName + "<br>" + "销售地："
									+ importBean.getLocation() + "-"
									+ importName + "";
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return "抱歉，数据库连接失败，请稍后查询";
				} finally {
					closeConnectionSafe(con);
				}
			} else {
				return "抱歉，数据库连接失败，请稍后查询";
			}
		} else {
			return "你确定你输入了货物，别拿空格糊弄我";
		}
	}

	/**
	 * 根据传入的风靡货物名称和出口地，找到距离该出口港口最远的港口，且该港口对于风靡货物的类型是最受欢迎的
	 * 
	 * @param exportBean
	 * @param hotGoodsName
	 * @return
	 */
	private static PortBean selectBestImportInfo(PortBean exportBean,
			String hotGoodsName) {
		// 第一步，根据出口港口的位置确定最远的销售地所在大洲
		int exportPortLocId = exportBean.getLoc_id();
		int importPortLocId = selectFarestLocId(exportPortLocId);

		// 第二步，查询风靡的货物是哪一种类型， 食品、原材料还是奢侈品
		String importType = selectGoodType(hotGoodsName);

		// 第三步，根据受欢迎的物品种类在销售大洲找到具体的港口返回
		PortBean importBean = selectImportBean(importPortLocId, importType);

		return importBean;
	}

	/**
	 * @param importPortLocId
	 * @param importType
	 * @return
	 */
	private static PortBean selectImportBean(int importPortLocId,
			String importType) {
		// TODO Auto-generated method stub
		PortBean portBean = null;
		if (SimpleConnectionPool.CONNECTSTATUS) {
			Connection con = SimpleConnectionPool.getConnection();
			String selectSql = "select * from table_port where loc_id =? and importType=?";
			try {
				PreparedStatement pstmt = con.prepareStatement(selectSql);
				pstmt.setInt(1, importPortLocId);
				pstmt.setString(2, importType);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					// 有可能会有多个港口满足条件，这个时候如何处理呢？将第一个返回即可；
					portBean = new PortBean();
					portBean.setName(rs.getString(1));
					portBean.setLocation(rs.getString(2));
					portBean.setSize(rs.getString(3));
					portBean.setImportType(rs.getString(4));
					portBean.setId(rs.getInt(5));
					portBean.setLoc_id(rs.getInt(6));
					break;
				}
				rs.close();
				pstmt.close();
				SimpleConnectionPool.closeConnection(con);
				return portBean;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return portBean;
			}
		} else {
			SimpleConnectionPool.printDbFail();
			return portBean;
		}
	}

	/**
	 * 根据货物的名称查询货物的类型
	 * 
	 * @param hotGoodsName
	 * @return 错误结果为null, 正确结果为以下三种之一（原材料，食品，奢侈品）
	 */
	private static String selectGoodType(String hotGoodsName) {
		// TODO Auto-generated method stub
		String type = null;
		if (SimpleConnectionPool.CONNECTSTATUS) {
			Connection con = SimpleConnectionPool.getConnection();
			String selectSql = "select type from table_good_type where name=?";
			boolean selectError = false;
			try {
				PreparedStatement pstmt = con.prepareStatement(selectSql);
				pstmt.setString(1, hotGoodsName);
				int count = 0;
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					count++;
					if (count > 1) { // result must be only one
						selectError = true;
						;
						break;
					}
					type = rs.getString(1);
				}
				rs.close();
				pstmt.close();
				SimpleConnectionPool.closeConnection(con);
				if (selectError) {
					SimpleConnectionPool.printSelectFail();
				}
				return type;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return type;
			}
		} else {
			SimpleConnectionPool.printDbFail();
			return type;
		}
	}

	/**
	 * 根据传入的港口所在大洲id从数据库中找到距离该港口最远的大洲id
	 * 
	 * @param exportPortLocId
	 * @return 错误时返回-1，正确结果为非负数（0-8）
	 */
	private static int selectFarestLocId(int exportPortLocId) {
		// TODO Auto-generated method stub
		int toid = -1;
		if (SimpleConnectionPool.CONNECTSTATUS) {
			Connection con = SimpleConnectionPool.getConnection();
			String selectSql = "select toid, time from table_loctime where fromid =?";
			int timeMax = 0;
			try {
				PreparedStatement pstmt = con.prepareStatement(selectSql);
				pstmt.setInt(1, exportPortLocId);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					int time = rs.getInt(2);
					if (time > timeMax) {
						timeMax = time;
						toid = rs.getInt(1);
					}
				}
				rs.close();
				pstmt.close();
				SimpleConnectionPool.closeConnection(con);
				return toid;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return toid;
			}
		} else {
			SimpleConnectionPool.printDbFail();
			return toid;
		}
	}

	/**
	 * 安全关闭这个连接
	 * 
	 * @param con
	 */
	private static void closeConnectionSafe(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将结果输入到数据库的Table_IncomeEntry中，包括起始港口、终点港口、花费时间、贸易利润、当前仓容和推进力<br>
	 * 不管数据表中存在该路线不，都将该记录存储到数据表中，以便于进行最后的平均；
	 * 
	 * @param fromPort
	 * @param toPort
	 * @param timeInSecond
	 * @param money
	 * @param propulsion
	 * @param capacity
	 */
	private static void insert2table(String fromPort, String toPort,
			int timeInSecond, int money, int capacity, int propulsion) {
		double ratio = money * 1.0 / timeInSecond;
		System.out.println("贸易比：" + ratio);
		if (testDriver()) {
			Connection con = null;
			String sqlUrl = "insert into Table_IncomeEntry(fromport, toport, timeinsecond, money, create_time, capacity, propulsion) "
					+ "values(?,?,?,?,now(),?,?)";
			try {
				con = DriverManager.getConnection(dbURL, userName, userPwd);
				PreparedStatement pstmt = con.prepareStatement(sqlUrl);
				pstmt.setString(1, fromPort);
				pstmt.setString(2, toPort);
				pstmt.setInt(3, timeInSecond);
				pstmt.setInt(4, money);
				pstmt.setInt(5, capacity);
				pstmt.setInt(6, propulsion);
				int flag = pstmt.executeUpdate();
				if (flag > 0) {
					System.out.println("insert success");
				}
				pstmt.close();
			} catch (SQLException e) {
				System.out.println("insert failed");
				e.printStackTrace();
			} finally {
				closeConnectionSafe(con);
			}
		} else {
			System.out.println("insert failed");
		}
	}

	/**
	 * @param fromPort
	 * @param toPort
	 * @param timeInSecond
	 * @param money
	 * @return
	 */
	private static boolean existEntry(String fromPort, String toPort,
			int timeInSecond, int money) throws Exception {
		boolean testFlag = testDriver();
		if (testFlag) {
			Connection con = null;
			String quarySqlStr = "select * from Table_NetIncome where fromport =? and toport =?";
			try {
				con = DriverManager.getConnection(dbURL, userName, userPwd);
				PreparedStatement pstmt = con.prepareStatement(quarySqlStr);
				pstmt.setString(1, fromPort);
				pstmt.setString(2, toPort);
				ResultSet rs = pstmt.executeQuery();
				ArrayList<String[]> existList = new ArrayList<String[]>(1);
				int count = 0;
				while (rs.next()) {
					if (count >= 1) {
						throw new ArrayIndexOutOfBoundsException();
					}
					count++;
					existList.add(new String[] { rs.getString(1),
							rs.getString(2), rs.getInt(3) + "",
							rs.getInt(4) + "" });
				}
				rs.close();
				pstmt.close();
				if (count == 1) {
					if (updateEntry(existList, timeInSecond, money)) {
						return true;
					}
				}
				return false;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				throw e;
			} finally {
				closeConnectionSafe(con);
			}
		} else {
			throw new ClassNotFoundException();
		}
	}

	/**
	 * @param existList
	 * @param timeInSecond
	 * @param money
	 * @throws SQLException
	 */
	private static boolean updateEntry(ArrayList<String[]> existList,
			int timeInSecond, int money) throws SQLException {
		String[] entryStr = existList.get(0);
		String fromPort = entryStr[0];
		String toPort = entryStr[1];
		int timeInSecondPre = Integer.parseInt(entryStr[2]);
		timeInSecondPre = (timeInSecondPre + timeInSecond) / 2;
		int moneyPre = Integer.parseInt(entryStr[3]);
		moneyPre = (moneyPre + money) / 2;
		Connection con = DriverManager.getConnection(dbURL, userName, userPwd);
		String updateSqlStr = "update Table_NetIncome set timeinsecond =?, money=? where fromport=? and toport=?";
		PreparedStatement pstmt = con.prepareStatement(updateSqlStr);
		pstmt.setInt(1, timeInSecondPre);
		pstmt.setInt(2, moneyPre);
		pstmt.setString(3, fromPort);
		pstmt.setString(4, toPort);
		int flag = pstmt.executeUpdate();
		closeConnectionSafe(con);
		pstmt.close();
		if (flag > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return
	 */
	private static boolean testDriver() {
		// TODO Auto-generated method stubs
		boolean flag = false;
		try {
			Class.forName(driverName);
			flag = true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("加载驱动失败！");
		}
		return flag;
	}

	/**
	 * 根据港口的名字查找港口的属性信息
	 * 
	 * @param portName
	 * @return
	 */
	private static PortBean selectPortInfo(String portName) {
		PortBean portBean = null;
		if (SimpleConnectionPool.CONNECTSTATUS) {
			String sqlSelectPortInfo = "select location,size,importType,id,loc_id from Table_Port where name = ?";
			Connection con = null;
			try {
				con = SimpleConnectionPool.getConnection();
				PreparedStatement pstmt = con
						.prepareStatement(sqlSelectPortInfo);
				int index = 1;
				pstmt.setString(index++, portName);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					if (portBean == null)
						portBean = new PortBean();
					portBean.setName(portName);
					portBean.setLocation(rs.getString(1));
					portBean.setSize(rs.getString(2));
					portBean.setImportType(rs.getString(3));
					portBean.setId(rs.getInt(4));
					portBean.setLoc_id(rs.getInt(5));
				}
				rs.close();
				pstmt.close();
				SimpleConnectionPool.closeConnection(con);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			SimpleConnectionPool.printDbFail();
		}
		return portBean;
	}

	static class PortBean {
		private String name;
		private String location;
		private String size;
		private String importType;
		private int id;
		private int loc_id;

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the location
		 */
		public String getLocation() {
			return location;
		}

		/**
		 * @param location
		 *            the location to set
		 */
		public void setLocation(String location) {
			this.location = location;
		}

		/**
		 * @return the size
		 */
		public String getSize() {
			return size;
		}

		/**
		 * @param size
		 *            the size to set
		 */
		public void setSize(String size) {
			this.size = size;
		}

		/**
		 * @return the importType
		 */
		public String getImportType() {
			return importType;
		}

		/**
		 * @param importType
		 *            the importType to set
		 */
		public void setImportType(String importType) {
			this.importType = importType;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(int id) {
			this.id = id;
		}

		/**
		 * @return the loc_id
		 */
		public int getLoc_id() {
			return loc_id;
		}

		/**
		 * @param loc_id
		 *            the loc_id to set
		 */
		public void setLoc_id(int loc_id) {
			this.loc_id = loc_id;
		}
	}

	public static void main(String[] args) throws IOException {
		inputTimeMoney();
	}

	private void findLowIncome() {
		// 第一步找到利润比小于70的起始港口和终止港口的名字
		// String[] names = selectLowIncome(70);
		// String exportName = names[0];
		// String importName = names[1];

		// 根据两个名字找到起始港口出产的货物单价,我认为这些低利润的出口货物单价是很低的
	}

	/**
	 * 将路程表输入到数据库中
	 * 
	 * @throws SQLException
	 */
	private static void insertToLocTimeTable(boolean updateFlag)
			throws SQLException {
		// TODO Auto-generated method stub
		int[][] graph = initDistanceTable();
		Connection con = SimpleConnectionPool.getConnection();
		PreparedStatement pstmt = null;
		String sqlInsert = updateFlag ? "update table_loctime set fromid=?, toid=?, time=？"
				: "insert into table_loctime values(?,?,?)";
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (i == j) {
					continue;
				}
				pstmt = con.prepareStatement(sqlInsert);
				pstmt.setInt(1, i);
				pstmt.setInt(2, j);
				pstmt.setInt(3, graph[i][j]);
				int flag = pstmt.executeUpdate();
				if (flag < 0) {
					throw new SQLException();
				}
				pstmt.close();
				pstmt = null;
			}
		}
		closeConnectionSafe(con);
	}

	/**
	 * 初始化大洲之间的距离，用记录在利润表中的时间平均值来衡量距离
	 * 
	 * @return
	 * @throws SQLException
	 */
	private static int[][] initDistanceTable() throws SQLException {
		// TODO Auto-generated method stub
		HashMap<String, Integer> portsTimeMap = getPortsTimeMap();
		int[][] portsDistGraph = new int[9][9];
		for (int i = 0; i < 9; i++) {
			for (int j = i; j < 9; j++) {
				if (i == j) {
					continue;
				}
				Iterator<Entry<String, Integer>> iter = portsTimeMap.entrySet()
						.iterator();
				float sum = 0;
				int count = 0;
				while (iter.hasNext()) {
					Entry<String, Integer> entry = iter.next();
					String locIdStr = getLocIdStr(entry);
					if (locIdStr.equals(i + "-" + j)
							|| locIdStr.equals(j + "-" + i)) {
						sum += entry.getValue();
						count++;
					}
				}
				if (count > 0) {
					int time = (int) (sum / count);
					portsDistGraph[i][j] = time;
					portsDistGraph[j][i] = time;
				}
			}
		}
		return portsDistGraph;
	}

	/**
	 * @param entry
	 * @return
	 * @throws SQLException
	 */
	private static String getLocIdStr(Entry<String, Integer> entry)
			throws SQLException {
		// TODO Auto-generated method stub
		String key = entry.getKey();
		String[] ids = key.split("-");
		int locId1 = getLocIdById(ids[0]);
		int locId2 = getLocIdById(ids[1]);
		return locId1 + "-" + locId2;
	}

	/**
	 * @param string
	 * @return
	 * @throws SQLException
	 */
	private static int getLocIdById(String string) throws SQLException {
		// TODO Auto-generated method stub
		int id = Integer.parseInt(string);
		int locId = -1;
		if (SimpleConnectionPool.CONNECTSTATUS) {
			Connection con = SimpleConnectionPool.getConnection();
			Statement statement = con.createStatement();
			statement.execute("select loc_id from table_port where id =" + id);
			ResultSet rs = statement.getResultSet();
			while (rs.next()) {
				locId = rs.getInt(1);
			}
			rs.close();
			statement.close();
			closeConnectionSafe(con);
		}
		if (locId < 0) {
			throw new SQLException();
		}
		return locId;
	}

	/**
	 * 获取记录的港口与港口之间贸易的利润时间比等信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	private static HashMap<String, Integer> getPortsTimeMap()
			throws SQLException {
		if (SimpleConnectionPool.CONNECTSTATUS) {
			Connection con = SimpleConnectionPool.getConnection();
			PreparedStatement pstmt = null;
			String selectSqlStr = "select fromport,toport,timeinsecond from table_netincome";
			pstmt = con.prepareStatement(selectSqlStr);
			ResultSet rs = pstmt.executeQuery();
			HashMap<String, Integer> recordMap = new HashMap<String, Integer>();
			while (rs.next()) {
				String fromPort = rs.getString(1);
				String toPort = rs.getString(2);
				int time = rs.getInt(3);
				int fromPortId = selectPortId(fromPort);
				int toPortId = selectPortId(toPort);
				if (fromPortId > 0 && toPortId > 0) {
					recordMap.put(fromPortId + "-" + toPortId, time);
				} else {
					System.out.println("查询id失败");
					return null;
				}
			}
			pstmt.close();
			rs.close();
			closeConnectionSafe(con);
			return recordMap;
		} else {
			System.out.println("连接数据库失败");
			return null;
		}
	}

	/**
	 * @param portName
	 * @return
	 * @throws SQLException
	 */
	private static int selectPortId(String portName) throws SQLException {
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
	 * @throws IOException
	 */
	private static void inputTimeMoney() throws IOException {
		String fromPort = null;
		String toPort = null;
		int timeInSecond = 0;
		int money = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String inStr = null;
		int index = 0;
		System.out.println("Tips:");
		System.out.println("\t input over to end");
		System.out.println("\t input reset to restart to input");

		int capacity = 0; // 仓容，设置每次输入时仓容一样
		int propulsion = 0; // 推进力，设置每次输入时推进力一样
		System.out.println("************首先请输入仓容和推进力************");
		while ((inStr = br.readLine()) != null) {
			switch (index) {
			case 0:
				capacity = Integer.parseInt(inStr);
				break;
			case 1:
				propulsion = Integer.parseInt(inStr);
				break;
			}
			index++;
			if (index == 2) {
				index = 0;
				break;
			}
		}
		System.out.println("************开始输入贸易记录************");
		while ((inStr = br.readLine()) != null) {
			if (inStr.equals("over")) {
				break;
			}
			if (inStr.equals("reset")) {
				index = 0;
				System.out.println("reset success");
				continue;
			}
			switch (index) {
			case 0:
				fromPort = inStr;
				break;
			case 1:
				toPort = inStr;
				break;
			case 2:
				timeInSecond = getTime(inStr);
				break;
			case 3:
				money = Integer.parseInt(inStr);
				break;
			default:
				break;
			}
			index++;
			if (index % 4 == 0) {
				insert2table(fromPort, toPort, timeInSecond, money, capacity,
						propulsion);
				index = 0;
			}
		}
		
		//更新平均数据表Table_NetIncome
	}

	/**
	 * @param inStr
	 * @return
	 */
	private static int getTime(String inStr) {
		// TODO Auto-generated method stub
		String minuteStr = inStr.substring(0, inStr.lastIndexOf("."));
		String secondStr = inStr.substring(inStr.lastIndexOf(".") + 1);
		int minute = Integer.parseInt(minuteStr);
		int second = Integer.parseInt(secondStr);
		return minute * 60 + second;
	}
}
