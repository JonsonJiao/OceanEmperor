/**
 * 2014-8-18
 * jiaoqishun
 */
package test.process;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dijkstra�㷨ʵ���࣬����������̾����һ���㷨
 * 2014-8-18
 * author jiaoqishun
 */
public class Dijkstra {
	private int [][] graph;//��Ȩ����ͼ	
	private int start;//Դ���� �� 0��ʼ
	private int dimention;
	static int INF  = Integer.MAX_VALUE /100 ;
	//���ڱ�Ƕ����Ƿ��Ѿ�����
	private Set<Integer> vertexSet = new HashSet<Integer>();	
	//�洢�����Map��key��Ӧ���յ��ţ�value��Ӧ·������б�
	private Map<Integer, List<Integer>> pathListMap = new HashMap<Integer, List<Integer>>();
	/**
	 * ���캯���������ʼ��·���������ʼ��
	 * @param graph
	 * @param start
	 */
	//Dijkstra�㷨�õ���3������  
	int[] s;//��¼�����Ƿ��ڼ�����  
	int[] dist;//��¼·����Ȩֵ  
	int[] path;//��¼·��  
	int v0;
	public Dijkstra(int[][] graph){
		this.graph = graph;
		this.dimention = graph.length;
	}
	
	/**
	 * ������startΪ��ʼ�㣬�������������̾���
	 * @param start
	 * @return <���-�յ�, �����ĵ�>
	 */
	public HashMap<String, String> calculate(int start) {
		int[] dist = new int[dimention];
		int[] s = new int[dimention];
		int[] path = new int[dimention];
		int v0 = start;
		int i, j, k;// ѭ������
		for (i = 0; i < dimention; i++) {
			dist[i] = graph[v0][i];
			s[i] = 0;
			if (i != v0 && dist[i] < INF)
				path[i] = v0;
			else
				path[i] = -1;
		}
		s[v0] = 1;
		dist[v0] = 0;// ����v0���붥�㼯��s
		for (i = 0; i < dimention - 1; i++)// �Ӷ���v0ȷ��n-1�����·��
		{
			int min = INF, u = v0;
			// ѡ��ǰ����T�о������·���Ķ���u
			for (j = 0; j < dimention; j++) {
				if (s[j] == 0 && dist[j] < min) {
					u = j;
					min = dist[j];
				}
			}
			s[u] = 1;// ������u���뼯��s����ʾ�������·�������
			// �޸�T�����ж����dist��path����Ԫ��
			for (k = 0; k < dimention; k++) {
				if (s[k] == 0 && graph[u][k] < INF
						&& dist[u] + graph[u][k] < dist[k]) {
					dist[k] = dist[u] + graph[u][k];
					path[k] = u;
				}
			}
		}
		HashMap<String, String> result = getNodePath(start, path, dist);
		dist = null;
		s = null; 
		path =null;
		return result;
	}
	/**
	 * �������ʼ��start�������������̾���
	 * @param start
	 * @param path
	 * @param dist
	 * @return
	 */
	private HashMap<String, String> getNodePath(int start, int[] path,
			int[] dist) {
		// TODO Auto-generated method stub
		int[] shortest = new int[dimention];//������·���ϵĸ�������ʱ��Ÿ�����������  
		StringBuffer sb = null;
		 HashMap<String, String> result = null;
	    for(int i=0; i<dimention; i++)  
	    {  
	    	if(i == start || (dist[i] == INF))
	    		continue;
	    	if(result == null){
	    		result = new HashMap<String, String>();
	    	}
	    	String key = start + "-" +i;
	    	sb = new StringBuffer();
	        //���´��������������start������i�����·��  
	        int k = 0;//k��ʾshorttest���������һ��Ԫ�ص��±�  
	        shortest[k] = i;  
	        while( path[ shortest[k] ]>=0 )  
	        {  
	            k++;  
	            shortest[k] = path[shortest[k-1]];  
	        }  
	        for(int j=k; j>0; j--)  {
	        	sb.append(shortest[j]+"--");
	        }
	        result.put(key, sb.toString());
	        sb = null;
	    }  
		return result;
	}

	public Dijkstra(int[][] graph, int start) {
		this.graph = graph;
		this.start = start;
		this.dimention = graph.length;		
		this.v0 = start;
		s = new int[dimention];
		dist = new int[dimention];
		path = new int[dimention];
		calculate();
		print();
	}
	  
	/**
	 * 
	 */
	private void print() {
		// TODO Auto-generated method stub
		int[] shortest = new int[dimention];//������·���ϵĸ�������ʱ��Ÿ�����������  
	    for(int i=0; i<dimention; i++)  
	    {  
	    	if(i == this.start || (dist[i] == INF))
	    		continue;
	        System.out.print("����"+this.start +"���ڵ�"+i+"����̾��룺   "+dist[i]+"\t");//�������0������i�����·������  
	        //���´��������������start������i�����·��  
	        int k = 0;//k��ʾshorttest���������һ��Ԫ�ص��±�  
	        shortest[k] = i;  
	        
	        while( path[ shortest[k] ]>=0 )  
	        {  
	            k++;  
	            shortest[k] = path[shortest[k-1]];  
	        }  
	        for(int j=k; j>0; j--)  
	            System.out.print(shortest[j]+"--");  
	        System.out.println(shortest[0]);  
	    }  
	}

	void calculate()//��v0������������·��  
	{  
	    int i, j, k;//ѭ������  
	    for(i=0; i<dimention; i++)  
	    {  
	        dist[i] = graph[v0][i];  
	        s[i] = 0;  
	        if( i!=v0 && dist[i]<INF )  
	            path[i] = v0;  
	        else  
	            path[i] = -1;  
	    }  
	    s[v0] = 1; dist[v0] = 0;//����v0���붥�㼯��s  
	    for(i=0; i<dimention-1; i++)//�Ӷ���v0ȷ��n-1�����·��  
	    {  
	        int min = INF, u = v0;  
	        //ѡ��ǰ����T�о������·���Ķ���u  
	        for(j=0; j<dimention; j++)  
	        {  
	            if( s[j] == 0 && dist[j]<min )  
	            {  
	                u = j;  
	                min = dist[j];  
	            }  
	        }  
	        s[u] = 1;//������u���뼯��s����ʾ�������·�������  
	        //�޸�T�����ж����dist��path����Ԫ��  
	        for(k=0; k<dimention; k++)  
	        {  
	            if( s[k] == 0 && graph[u][k]<INF && dist[u]+graph[u][k]<dist[k] )  
	                {  
	                    dist[k] = dist[u] + graph[u][k];  
	                    path[k] = u;  
	                }  
	        }  
	    }  
	}
}