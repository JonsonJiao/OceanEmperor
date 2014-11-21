/**
 * 2014-8-20
 * jiaoqishun
 */
package test.hanghai;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import test.process.SimpleConnectionPool;

/**
 * 2014-8-20
 * author jiaoqishun
 */
public class Hanghaizhengba {
	/**
	 * 
	 */
	private static final int LUXURY_ID = 102;
	/**
	 * 
	 */
	private static final int FOOD_ID = 101;
	/**
	 * 
	 */
	private static final int RAW_ID = 100;
	public static void main(String[] args) {
		Hanghaizhengba hhzb = new Hanghaizhengba();
		File file = new File("F:\\PrivateData\\������ʱ����Ʒ��V3.0-modify.xls");
		ArrayList<String[]> valuesList = hhzb.readExcel(file, 0);
		try {
			hhzb.save2TableGoodType(valuesList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
	}
	
	/**
	 * ����ȡ�����ݴ洢���������Ա���
	 * @param valuesList
	 */
	private boolean save2TableGoodType(ArrayList<String[]> valuesList) throws Exception{
		// TODO Auto-generated method stub
		if(valuesList == null || valuesList.size()==0){
			return false;
		}
		if(SimpleConnectionPool.CONNECTSTATUS){
			Connection con = SimpleConnectionPool.getConnection();
			String insertSql = "insert into table_good_type values(?,?,?,?)";
			PreparedStatement pstmt = null;
//			try {
				for(int i =0 ; i < valuesList.size(); i++){
					String[] rowStr = valuesList.get(i);
					for(int j = 1; j< rowStr.length; j+=2){
						String type = rowStr[j];
						String goodName = getGoodName(rowStr[j+1]);
						int typeId = getTypeId(type);
						String level = getLevel(j);
						if(typeId < 0){
							throw new Exception();
						}
						pstmt =con.prepareStatement(insertSql);
						pstmt.setString(1, goodName);
						pstmt.setString(2, type);
						pstmt.setInt(3, typeId);
						pstmt.setString(4, level);
						int flag =pstmt.executeUpdate();
						if(flag < 0){
							throw new SQLException();
						}
						pstmt.close();
						pstmt = null;
//						System.out.println(goodName+"\t"+type+"\t"+typeId+"\t"+level);
					}
				}
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			SimpleConnectionPool.closeConnection(con);
		}
		return true;
		
	}

	/**
	 * @param type
	 * @return
	 */
	private int getTypeId(String type) {
		// TODO Auto-generated method stub
		if(type.equals("ԭ����")){
			return RAW_ID;
		}else if(type.equals("ʳƷ")){
			return FOOD_ID;
		}else if(type.equals("�ݳ�Ʒ")){
			return LUXURY_ID;
		}else{
			return -1;
		}
	}

	/**
	 * �����±��ҵ�����
	 * @param j
	 * @return
	 */
	private String getLevel(int j) {
		// TODO Auto-generated method stub
		String level = null;
		switch(j){
		case 1:
			level = "��ͨ";
			break;
		case 3:
			level = "��ͨ";
			break;
		case 5:
			level = "����";
			break;
		case 7:
			level = "��";
			break;
		case 9:
			level = "���";
			break;
		}
		return level;
	}

	/**
	 * ��Ҫ�Ի������һ��ת�������溬��һЩ�Ǻ�
	 * @param string
	 * @return
	 */
	private String getGoodName(String string) {
		// TODO Auto-generated method stub
		String[] temp = string.split(" +");
		return temp[0];
	}

	/**
	 * ��excel�ж�ȡ���ݣ�ÿһ��Ϊһ��String[],��֮���ö��Ÿ���
	 * @param file
	 * @param skipLine ����������
	 * @return
	 */
	private ArrayList<String[]> readExcel(File file, int skipLine) {
		// TODO Auto-generated method stub
		try {
			Workbook workbook = Workbook.getWorkbook(file);
			Sheet sheet = workbook.getSheet(0);
			int rows = sheet.getRows();
			int columns = sheet.getColumns();
			ArrayList<String[]> valuesList = new ArrayList<String[]>();
			for(int i =skipLine; i < rows; i++){
				String[] values = new String[columns];
				for(int j = 0; j < columns; j++){
					Cell cell = sheet.getCell(j, i);
					String cellValue = null;
					cellValue = cell.getContents().trim();
//					System.out.print(cellValue+"\t");
					values[j] = sbcToDbc(cellValue);
				}
				valuesList.add(values);
//				System.out.println();
			}
			return valuesList;
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * ȫ���ַ�ת��Ϊ����ַ�<br>
	 * ȫ��(SBC case)���(DBC case)ת����<br>
	 * ȫ�ǿո�Ϊ12288����ǿո�Ϊ32<br>
	 * �����ַ����(33-126)��ȫ��(65281-65374)�Ķ�Ӧ��ϵ�ǣ������65248<br>
	 * @param input
	 * @return
	 */
	private String sbcToDbc(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}
}
