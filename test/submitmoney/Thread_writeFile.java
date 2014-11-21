/**
 * 2014-9-9
 * jiaoqishun
 */
package test.submitmoney;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Calendar;
/**
 * @author chb
 */
public class Thread_writeFile extends Thread{
    public void run(){
        Calendar calstart=Calendar.getInstance();
        File file=new File("D:/test.txt");        
        try {
            if(!file.exists())
                file.createNewFile();
                        
            //�Ը��ļ�����
            RandomAccessFile out = new RandomAccessFile(file, "rw");
            FileChannel fcout=out.getChannel();
            FileLock flout=null;
            while(true){  
                try {
                	flout = fcout.tryLock();
					break;
				} catch (Exception e) {
					 System.out.println("write�������߳����ڲ������ļ�����ǰ�߳�����1000����"); 
					 sleep(1000);  
				}
            }
            String _n = "\n";
        
            for(int i=1;i<=1000;i++){
                sleep(10);
                StringBuffer sb=new StringBuffer();
                sb.append("���ǵ�"+i+"�У�Ӧ��ûɶ��� ");
                out.write(sb.toString().getBytes("utf-8"));
				out.writeChars(_n);
            }
            
            flout.release();
            fcout.close();
            out.close();
            out=null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Calendar calend=Calendar.getInstance();
        System.out.println("д�ļ�������"+(calend.getTimeInMillis()-calstart.getTimeInMillis())+"����");
    }
    
    public static void main(String[] args) {
    	Thread_writeFile thf3=new Thread_writeFile();  
    	Thread_writeFile thf31=new Thread_writeFile();  
    	Thread_writeFile thf32=new Thread_writeFile();  
    	Thread_writeFile thf33=new Thread_writeFile();  
//        Thread_readFile thf4=new Thread_readFile();  
//        thf4.start();  
        thf3.start();  
        thf31.start();  
        thf32.start();  
        thf33.start();  
	}
}
