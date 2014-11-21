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
	
	private static final String SUCCESS = "�ύ�ɹ�";
	private static final String FAILE = "�ύʧ��,�������ѽ�������";
	private static final String EXCEL_PATH = "F:/���̶�����ͼ�¼-20140801.xls";
	private String errorMsg = null;
	public String submit(final String name, final String money){
		
		new Thread(){
			@Override
			public void run() {
				//�����ļ�����  
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
							System.out.println("�������߳����ڲ������ļ�����ǰ�߳�����1000����"); 
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
	 * �������ֽ����ѽ��д��excel<br>
	 * ��Ҫ�õ���������ѡ��sheet����<br>
	 * ��������ѡ����<br>
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
				errorMsg =  "�ļ������ļ����Ҳ�����Ӧ�·ݼ�¼�������ļ���¼";
			}
			Sheet sheet = workbook.getSheet(sheetNum);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int columnIndex = getColumnIndex(day, sheet);
			if(columnIndex < 0){
				errorMsg =  "�ļ������ļ����Ҳ�����Ӧ���ڵ��У������ļ���¼";
			}
			
			int rowIndex = getRowIndex(name, sheet);
			if(rowIndex < 0){
				errorMsg=  "������û��������ڣ�����������û�������ϵ����Ա����ļ����Ƿ���ڸ��û���#"+name+"#";
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
	 * ���������������ǵڼ���
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
	 * ���������е�����Ϣ���Ҷ�Ӧ���У��ӵ����е�һ�в���<br>
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
	 * �����·�ȷ������һ��sheet��sheet�����ָ���ʽΪ"8��"
	 * @param month
	 * @param workbook 
	 * @return
	 */
	private int getSheetNum(int month, Workbook workbook) {
		// TODO Auto-generated method stub
		String searchName = month+"��";
		String[] sheetNames = workbook.getSheetNames();
		for(int i = 0; i< sheetNames.length; i++){
			if(sheetNames[i].equals(searchName)){
				return i;
			}
		}
		return -1;
	}
	
	public static void main(String[] args) {
		String name = "����˳";
		String money = "3f";
		for(int i = 0; i < 10; i++){
			System.out.println(new SubmitMoney().submit(name, money));
		}
			
	}
}
