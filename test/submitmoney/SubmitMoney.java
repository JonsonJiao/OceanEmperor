/**
 * 2014-9-4
 * jiaoqishun
 */
package test.submitmoney;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Calendar;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 2014-9-4
 * author jiaoqishun
 */
public class SubmitMoney {
	
	private static final String SUCCESS = "提交成功";
	private static final String FAILE = "提交失败,您的消费金额不是数字";
	private static final String EXCEL_PATH = "F:/工程二部午餐记录-20140801.xls";
	private String errorMsg = null;
	public String submit(final String name, final String money){
		
		new Thread(){
			@Override
			public void run() {
				//给该文件加锁  
				final File file = new File(EXCEL_PATH);
	            RandomAccessFile fis;
				try {
					fis = new RandomAccessFile(file, "rw");
					FileChannel fcout=fis.getChannel();  
					FileLock flout=null;  
					while(true){
						try {
							flout = fcout.tryLock();
							break;
						} catch (Exception e) {
							System.out.println("有其他线程正在操作该文件，当前线程休眠1000毫秒"); 
							try {
								sleep(1000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}  
						}
						
					}  
					writeToExcel(file, name, money);
					flout.release();
					fcout.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
			}
		}.start();
		while(errorMsg == null){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return errorMsg;
	}
	/**
	 * 根据名字将消费金额写入excel<br>
	 * 需要用到根据日期选择sheet和列<br>
	 * 根据名字选择行<br>
	 * @param name
	 * @param _money
	 */
	private void writeToExcel(final File file, final String name, final String money) {
		// TODO Auto-generated method stub
		Calendar calendar = Calendar.getInstance();
		int month= calendar.get(Calendar.MONTH)+1;
		try {
			FileInputStream in = new FileInputStream(file);
			Workbook workbook = Workbook.getWorkbook(in);
			int sheetNum = getSheetNum(month, workbook);
			if(sheetNum < 0){
				errorMsg =  "文件错误，文件中找不到对应月份记录，请检查文件记录";
			}
			Sheet sheet = workbook.getSheet(sheetNum);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int columnIndex = getColumnIndex(day, sheet);
			if(columnIndex < 0){
				errorMsg =  "文件错误，文件中找不到对应日期的列，请检查文件记录";
			}
			
			int rowIndex = getRowIndex(name, sheet);
			if(rowIndex < 0){
				errorMsg=  "输入的用户名不存在，请检查输入的用户名或联系管理员检查文件中是否存在改用户名#"+name+"#";
			}
			float _money = Float.valueOf(money);
			WritableWorkbook _workbook = Workbook.createWorkbook(file, workbook);
			NumberFormat currencyFormat = new NumberFormat(" ###,###.0", NumberFormat.COMPLEX_FORMAT); 
			WritableCellFormat lCurrencyFormat = new WritableCellFormat(currencyFormat);
			jxl.write.Number currency = new jxl.write.Number(columnIndex, rowIndex, _money, lCurrencyFormat);
			WritableSheet ws = _workbook.getSheet(sheetNum);
			ws.addCell(currency);
			_workbook.write();
			_workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
			errorMsg = FAILE;
			return;
		}
		errorMsg = SUCCESS;
	}
	/**
	 * 根据人名来查找是第几行
	 * @param name
	 * @param sheet
	 * @return
	 */
	private int getRowIndex(String name, Sheet sheet) {
		// TODO Auto-generated method stub
		int rowCount = sheet.getRows();
		for(int i = 1; i< rowCount; i++){
			String contents = sheet.getCell(0, i).getContents();
			if(contents == null){
				continue;
			}
			if(name.equals(contents)){
				return i;
			}
		}
		return -1;
	}
	/**
	 * 根据日期中的天信息查找对应的列，从第三列第一行查找<br>
	 * @param day
	 * @param sheet
	 * @return
	 */
	private int getColumnIndex(int day, Sheet sheet) {
		// TODO Auto-generated method stub
		String searchContent = day+"";
		int columnCount = sheet.getColumns();
		for(int i =2; i< columnCount; i++){
			String contents = sheet.getCell(i, 0).getContents();
			if(contents == null){
				continue;
			}
			if(searchContent.equals(contents)){
				return i;
			}
		}
		return -1;
	}
	/**
	 * 根据月份确定是哪一个sheet，sheet的名字个格式为"8月"
	 * @param month
	 * @param workbook 
	 * @return
	 */
	private int getSheetNum(int month, Workbook workbook) {
		// TODO Auto-generated method stub
		String searchName = month+"月";
		String[] sheetNames = workbook.getSheetNames();
		for(int i = 0; i< sheetNames.length; i++){
			if(sheetNames[i].equals(searchName)){
				return i;
			}
		}
		return -1;
	}
	
	public static void main(String[] args) {
		String name = "焦其顺";
		String money = "3f";
		for(int i = 0; i < 10; i++){
			System.out.println(new SubmitMoney().submit(name, money));
		}
			
	}
}
