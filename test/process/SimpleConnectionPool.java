/**
 * 2014-8-18
 * jiaoqishun
 */
package test.process;

/**
 * 2014-8-18
 * author jiaoqishun
 */
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * ���ݿ����ӳ�ʵ��
 * 1. ��һ���򵥵ĺ��������ӳ��еõ�һ�� Connection��
 * 2. close �������뽫connection �Ż� ���ݿ����ӳء�
 * 3. �����ݿ����ӳ���û�п��е�connection�����ݿ����ӳر����ܹ��Զ�����connection ������
 * 4. �����ݿ����ӳ��е�connection ������ĳһ���ر��ʱ���úܴ󣬵����Ժ�ܳ�ʱ��ֻ������һС���֣�Ӧ�ÿ����Զ��������connection �رյ���
 * 5. ������ܣ�Ӧ���ṩdebug ��Ϣ����û�йرյ�new Connection ��
 * 2014-8-15 author jiaoqishun
 */
public class SimpleConnectionPool {
	/**
	 * ���ݿ�����ʧ����Ϣ
	 */
	private static final String MSG_DB_CONNECT_FAIL = "���ݿ�����ʧ��";
	private static final String MSG_DB_SELECT_NOT_UNIQUE= "���ݿ��ѯ�����Ψһ";
	private static LinkedList<ConnectionWrapper> m_notUsedConnection = new LinkedList<ConnectionWrapper>();
	private static HashSet<ConnectionWrapper> m_usedUsedConnection = new HashSet<ConnectionWrapper>();
	private final static String DBDRIVER = "com.mysql.jdbc.Driver";
	private final static String DBURL = "jdbc:mysql://127.0.0.1:3306/hanghaizhengba";
	private final static String DBUSER = "root";
	private final static String DBPASSWORD = "1234dhcc";
	static boolean DEBUG = true;
	static private long m_lastClearClosedConnection = System
			.currentTimeMillis();
	public static long CHECK_CLOSED_CONNECTION_TIME = 4 * 60 * 60 * 1000; // 4
																			// hours
	public static boolean CONNECTSTATUS = false; // ��ʼ��״̬
	static {
		initDriver();
	}

	private SimpleConnectionPool() {
	}
	
	/**
	 * ���ݿ�����ʧ��
	 */
	public static void printDbFail() {
		// TODO Auto-generated method stub
		System.err.println(MSG_DB_CONNECT_FAIL);
	}

	private static void initDriver() {
		Driver driver = null;
		// load sql server driver
		try {
			driver = (Driver) Class.forName(DBDRIVER).newInstance();
			installDriver(driver);
			CONNECTSTATUS = true;
		} catch (Exception e) {
		}
	}

	public static void installDriver(Driver driver) {
		try {
			DriverManager.registerDriver(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized Connection getConnection() {
		clearClosedConnection();
		while (m_notUsedConnection.size() > 0) {
			try {
				ConnectionWrapper wrapper = (ConnectionWrapper) m_notUsedConnection
						.removeFirst();
				if (wrapper.connection.isClosed()) {
					continue;
				}
				m_usedUsedConnection.add(wrapper);
				if (DEBUG) {
					wrapper.debugInfo = new Throwable(
							"Connection initial statement");
				}
				return wrapper.connection;
			} catch (Exception e) {
			}
		}
		int newCount = getIncreasingConnectionCount();
		LinkedList<ConnectionWrapper> list = new LinkedList<ConnectionWrapper>();
		ConnectionWrapper wrapper = null;
		for (int i = 0; i < newCount; i++) {
			wrapper = getNewConnection();
			if (wrapper != null) {
				list.add(wrapper);
			}
		}
		if (list.size() == 0) {
			return null;
		}
		wrapper = (ConnectionWrapper) list.removeFirst();
		m_usedUsedConnection.add(wrapper);

		m_notUsedConnection.addAll(list);
		list.clear();

		return wrapper.connection;
	}

	private static ConnectionWrapper getNewConnection() {
		try {
			Connection con = DriverManager.getConnection(DBURL, DBUSER,
					DBPASSWORD);
			ConnectionWrapper wrapper = new ConnectionWrapper(con);
			return wrapper;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static synchronized void pushConnectionBackToPool(ConnectionWrapper con) {
		boolean exist = m_usedUsedConnection.remove(con);
		if (exist) {
			m_notUsedConnection.addLast(con);
		}
	}

	public static int close() {
		int count = 0;

		Iterator<ConnectionWrapper> iterator = m_notUsedConnection.iterator();
		while (iterator.hasNext()) {
			try {
				((ConnectionWrapper) iterator.next()).close();
				count++;
			} catch (Exception e) {
			}
		}
		m_notUsedConnection.clear();

		iterator = m_usedUsedConnection.iterator();
		while (iterator.hasNext()) {
			try {
				ConnectionWrapper wrapper = (ConnectionWrapper) iterator.next();
				wrapper.close();
				if (DEBUG) {
					wrapper.debugInfo.printStackTrace();
				}
				count++;
			} catch (Exception e) {
			}
		}
		m_usedUsedConnection.clear();

		return count;
	}

	private static void clearClosedConnection() {
		long time = System.currentTimeMillis();
		// sometimes user change system time,just return
		if (time < m_lastClearClosedConnection) {
			time = m_lastClearClosedConnection;
			return;
		}
		// no need check very often
		if (time - m_lastClearClosedConnection < CHECK_CLOSED_CONNECTION_TIME) {
			return;
		}
		m_lastClearClosedConnection = time;

		// begin check
		Iterator<ConnectionWrapper> iterator = m_notUsedConnection.iterator();
		while (iterator.hasNext()) {
			ConnectionWrapper wrapper = (ConnectionWrapper) iterator.next();
			try {
				if (wrapper.connection.isClosed()) {
					iterator.remove();
				}
			} catch (Exception e) {
				iterator.remove();
				if (DEBUG) {
					System.out
							.println("connection is closed, this connection initial StackTrace");
					wrapper.debugInfo.printStackTrace();
				}
			}
		}

		// make connection pool size smaller if too big
		int decrease = getDecreasingConnectionCount();
		if (m_notUsedConnection.size() < decrease) {
			return;
		}

		while (decrease-- > 0) {
			ConnectionWrapper wrapper = (ConnectionWrapper) m_notUsedConnection
					.removeFirst();
			try {
				wrapper.connection.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * get increasing connection count, not just add 1 connection
	 * 
	 * @return count
	 */
	public static int getIncreasingConnectionCount() {
		int count = 1;
		int current = getConnectionCount();
		count = current / 4;
		if (count < 1) {
			count = 1;
		}
		return count;
	}

	/**
	 * get decreasing connection count, not just remove 1 connection
	 * 
	 * @return count
	 */
	public static int getDecreasingConnectionCount() {
		int current = getConnectionCount();
		if (current < 10) {
			return 0;
		}
		return current / 3;
	}

	public synchronized static void printDebugMsg() {
		printDebugMsg(System.out);
	}

	public synchronized static void printDebugMsg(PrintStream out) {
		if (DEBUG == false) {
			return;
		}
		StringBuffer msg = new StringBuffer();
		msg.append("debug message in " + SimpleConnectionPool.class.getName());
		msg.append("\r\n");
		msg.append("total count is connection pool: " + getConnectionCount());
		msg.append("\r\n");
		msg.append("not used connection count: " + getNotUsedConnectionCount());
		msg.append("\r\n");
		msg.append("used connection, count: " + getUsedConnectionCount());
		out.println(msg);
		Iterator<ConnectionWrapper> iterator = m_usedUsedConnection.iterator();
		while (iterator.hasNext()) {
			ConnectionWrapper wrapper = (ConnectionWrapper) iterator.next();
			wrapper.debugInfo.printStackTrace(out);
		}
		out.println();
	}

	public static synchronized int getNotUsedConnectionCount() {
		return m_notUsedConnection.size();
	}

	public static synchronized int getUsedConnectionCount() {
		return m_usedUsedConnection.size();
	}

	public static synchronized int getConnectionCount() {
		return m_notUsedConnection.size() + m_usedUsedConnection.size();
	}

	/**
	 * @param con
	 */
	public static void closeConnection(Connection con) {
		// TODO Auto-generated method stub
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public static void printSelectFail() {
		// TODO Auto-generated method stub
		System.err.println(MSG_DB_SELECT_NOT_UNIQUE);
	}

}

class ConnectionWrapper implements InvocationHandler {
	private final static String CLOSE_METHOD_NAME = "close";
	public Connection connection = null;
	private Connection m_originConnection = null;
	public long lastAccessTime = System.currentTimeMillis();
	Throwable debugInfo = new Throwable("Connection initial statement");

	ConnectionWrapper(Connection con) {
		this.connection = (Connection) Proxy.newProxyInstance(con.getClass()
				.getClassLoader(), 
				//con.getClass().getInterfaces(),	//ʹ��mysql5���ϰ汾��ʱ����Ҫ�޸�Ϊ�������ʽ��ԭ�������conn.getClass().getInterfaces()������������Class�����飬������ĵ�һ��Ԫ�ر�����Connection���ܰѴ����Ĵ�����תΪConnection����
				new Class[]{Connection.class},
				this);
		m_originConnection = con;
	}

	void close() throws SQLException {
		m_originConnection.close();
	}

	public Object invoke(Object proxy, Method m, Object[] args)
			throws Throwable {
		Object obj = null;
		if (CLOSE_METHOD_NAME.equals(m.getName())) {
			SimpleConnectionPool.pushConnectionBackToPool(this);
		} else {
			obj = m.invoke(m_originConnection, args);
		}
		lastAccessTime = System.currentTimeMillis();
		return obj;
	}
}
