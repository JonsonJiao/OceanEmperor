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
		File file = new File("F:\\PrivateData\\航海大时代商品表V3.0-modify.xls");
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
	 * 将读取的数据存储到货物属性表中
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
		if(type.equals("原材料")){
			return RAW_ID;
		}else if(type.equals("食品")){
			return FOOD_ID;
		}else if(type.equals("奢侈品")){
			return LUXURY_ID;
		}else{
			return -1;
		}
	}

	/**
	 * 根据下标找到级别
	 * @param j
	 * @return
	 */
	private String getLevel(int j) {
		// TODO Auto-generated method stub
		String level = null;
		switch(j){
		case 1:
			level = "普通";
			break;
		case 3:
			level = "普通";
			break;
		case 5:
			level = "友善";
			break;
		case 7:
			level = "尊敬";
			break;
		case 9:
			level = "崇拜";
			break;
		}
		return level;
	}

	/**
	 * 需要对货物进行一次转化，里面含有一些星号
	 * @param string
	 * @return
	 */
	private String getGoodName(String string) {
		// TODO Auto-generated method stub
		String[] temp = string.split(" +");
		return temp[0];
	}

	/**
	 * 从excel中读取数据，每一行为一个String[],列之间用逗号隔开
	 * @param file
	 * @param skipLine 跳过的行数
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
	 * 全角字符转换为半角字符<br>
	 * 全角(SBC case)半角(DBC case)转换类<br>
	 * 全角空格为12288，半角空格为32<br>
	 * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248<br>
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
