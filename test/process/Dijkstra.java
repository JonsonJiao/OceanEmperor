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
 * Dijkstra算法实现类，用来计算最短距离的一种算法
 * 2014-8-18
 * author jiaoqishun
 */
public class Dijkstra {
	private int [][] graph;//加权有向图	
	private int start;//源点编号 从 0开始
	private int dimention;
	static int INF  = Integer.MAX_VALUE /100 ;
	//用于标记顶点是否已经计算
	private Set<Integer> vertexSet = new HashSet<Integer>();	
	//存储结果，Map的key对应各终点编号，value对应路径编号列表。
	private Map<Integer, List<Integer>> pathListMap = new HashMap<Integer, List<Integer>>();
	/**
	 * 构造函数，必须初始化路径矩阵和起始点
	 * @param graph
	 * @param start
	 */
	//Dijkstra算法用到的3个数组  
	int[] s;//记录顶点是否在集合中  
	int[] dist;//记录路径的权值  
	int[] path;//记录路径  
	int v0;
	public Dijkstra(int[][] graph){
		this.graph = graph;
		this.dimention = graph.length;
	}
	
	/**
	 * 计算以start为起始点，到其他顶点的最短距离
	 * @param start
	 * @return <起点-终点, 经过的点>
	 */
	public HashMap<String, String> calculate(int start) {
		int[] dist = new int[dimention];
		int[] s = new int[dimention];
		int[] path = new int[dimention];
		int v0 = start;
		int i, j, k;// 循环变量
		for (i = 0; i < dimention; i++) {
			dist[i] = graph[v0][i];
			s[i] = 0;
			if (i != v0 && dist[i] < INF)
				path[i] = v0;
			else
				path[i] = -1;
		}
		s[v0] = 1;
		dist[v0] = 0;// 顶点v0加入顶点集合s
		for (i = 0; i < dimention - 1; i++)// 从顶点v0确定n-1条最短路径
		{
			int min = INF, u = v0;
			// 选择当前集合T中具有最短路径的顶点u
			for (j = 0; j < dimention; j++) {
				if (s[j] == 0 && dist[j] < min) {
					u = j;
					min = dist[j];
				}
			}
			s[u] = 1;// 将顶点u加入集合s，表示它的最短路径已求得
			// 修改T集合中顶点的dist和path数组元素
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
	 * 计算从起始点start到各个顶点的最短距离
	 * @param start
	 * @param path
	 * @param dist
	 * @return
	 */
	private HashMap<String, String> getNodePath(int start, int[] path,
			int[] dist) {
		// TODO Auto-generated method stub
		int[] shortest = new int[dimention];//输出最短路径上的各个顶点时存放各个顶点的序号  
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
	        //以下代码用于输出顶点start到顶点i的最短路径  
	        int k = 0;//k表示shorttest数组中最后一个元素的下标  
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
		int[] shortest = new int[dimention];//输出最短路径上的各个顶点时存放各个顶点的序号  
	    for(int i=0; i<dimention; i++)  
	    {  
	    	if(i == this.start || (dist[i] == INF))
	    		continue;
	        System.out.print("顶点"+this.start +"到节点"+i+"的最短距离：   "+dist[i]+"\t");//输出顶点0到顶点i的最短路径长度  
	        //以下代码用于输出顶点start到顶点i的最短路径  
	        int k = 0;//k表示shorttest数组中最后一个元素的下标  
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

	void calculate()//求v0到其他点的最短路径  
	{  
	    int i, j, k;//循环变量  
	    for(i=0; i<dimention; i++)  
	    {  
	        dist[i] = graph[v0][i];  
	        s[i] = 0;  
	        if( i!=v0 && dist[i]<INF )  
	            path[i] = v0;  
	        else  
	            path[i] = -1;  
	    }  
	    s[v0] = 1; dist[v0] = 0;//顶点v0加入顶点集合s  
	    for(i=0; i<dimention-1; i++)//从顶点v0确定n-1条最短路径  
	    {  
	        int min = INF, u = v0;  
	        //选择当前集合T中具有最短路径的顶点u  
	        for(j=0; j<dimention; j++)  
	        {  
	            if( s[j] == 0 && dist[j]<min )  
	            {  
	                u = j;  
	                min = dist[j];  
	            }  
	        }  
	        s[u] = 1;//将顶点u加入集合s，表示它的最短路径已求得  
	        //修改T集合中顶点的dist和path数组元素  
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