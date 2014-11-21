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
                        
            //对该文件加锁
            RandomAccessFile out = new RandomAccessFile(file, "rw");
            FileChannel fcout=out.getChannel();
            FileLock flout=null;
            while(true){  
                try {
                	flout = fcout.tryLock();
					break;
				} catch (Exception e) {
					 System.out.println("write有其他线程正在操作该文件，当前线程休眠1000毫秒"); 
					 sleep(1000);  
				}
            }
            String _n = "\n";
        
            for(int i=1;i<=1000;i++){
                sleep(10);
                StringBuffer sb=new StringBuffer();
                sb.append("这是第"+i+"行，应该没啥错哈 ");
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
        System.out.println("写文件共花了"+(calend.getTimeInMillis()-calstart.getTimeInMillis())+"毫秒");
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
