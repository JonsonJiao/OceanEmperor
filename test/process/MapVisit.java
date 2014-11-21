package test.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MapVisit {
	/**
	 * �����ʼ��·��--��֪
	 * 
	 * @return
	 */
	public List<Road> init() {
		List<Road> list = new ArrayList<Road>();
		list.add(new Road("A", "B")); //A-->B �Դ�����
		list.add(new Road("B", "C"));
		list.add(new Road("B", "E"));
		list.add(new Road("C", "B"));
		list.add(new Road("C", "D"));
		list.add(new Road("D", "F"));
		list.add(new Road("D", "Z"));
		list.add(new Road("E", "C"));
		list.add(new Road("E", "D"));
		list.add(new Road("E", "Z"));
		list.add(new Road("G", "B"));
		list.add(new Road("G", "F"));
		return list;
	}
	
	/**
	 * �ж�����·�����Ƿ�����begin�ڵ�Ϊ���Ļ���·��<br>
	 * ���������ʼ�㲻����Ҫ���ҵĽ�����<br>
	 * Ŀ�ģ����ҵ�����ʼ��ŵ���ɾ�������У����Ҫɾ���Դ�Ϊ�������·��
	 * @param begin
	 * @param roads
	 * @return
	 */
	public boolean existBegin(String begin, String searchEnd, List<Road> roads) {
		if (begin.equals(searchEnd)) {
			return true;
		}
		boolean result = false;
		for (Road r : roads) {
			if (r.getBegin().equals(begin)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * �ж�����·�����Ƿ�����end�ڵ�Ϊ�յ�Ļ���·��<br>
	 * ������������㲻����Ҫ���ҵ���ʼ��<br>
	 * Ŀ�ģ����ҵ��Ľ�����ŵ���ɾ�������У����Ҫɾ���Դ�Ϊ��ʼ���·��
	 * @param end
	 * @param roads
	 * @return
	 */
	public boolean existEnd(String end, String searchBegin, List<Road> roads) {
		if (end.equals(searchBegin)) {
			return true;
		}
		boolean result = false;
		for (Road r : roads) {
			if (r.getEnd().equals(end)) {
				result = true;
				break;
			}
		}
		return result;
	}
	/**
	 * ����·����ȡ�����еĽڵ�
	 * @param roads
	 * @return
	 */
	public Set<String> getAllNodes(List<Road> roads) {
		Set<String> nodes = new HashSet<String>();
		for(Road r : roads) {
			nodes.add(r.getBegin());
			nodes.add(r.getEnd());
		}
		return nodes;
	}

	/**
	 * ��ȡ����Ҫɾ����·��
	 * 
	 * @param begins
	 * ---��Ч��ʼ�ڵ�
	 * @param ends
	 * ---��Ч�ս��
	 * @param roads
	 */
	public Set<Road> deleteRoad(Set<String> begins, Set<String> ends,
			List<Road> roads) {
		Set<Road> set = new HashSet<Road>();
		for (String str : begins) {
			for (Road r : roads) {
				if (r.getBegin().equals(str)) {
					System.out.println("ɾ��·����"+r.getBegin()+"->"+r.getEnd());
					set.add(r);
				}
			}
		}
		
		for (String str : ends) {
			for (Road r : roads) {
				if (r.getEnd().equals(str)) {
					if(!set.contains(r)){
						System.out.println("ɾ��·����"+r.getBegin()+"->"+r.getEnd());
						set.add(r);
					}
				}
			}
		}
		
		return set;
	}

	/**
	 * ���˵����õĽڵ�
	 * ��ȡ����Ч��ʼ�ڵ����Ч������
	 * @param all
	 * @param searchEnd 
	 * @param searchBegin 
	 * @return
	 */
	public Set<String> filterInvalidNode(Set<String> all, List<Road> roads,
			Set<String> begins, Set<String> ends, String searchBegin, String searchEnd) {
		Set<String> result = new HashSet<String>();
		boolean isBegin = true;
		boolean isEnd = true;
		for (String str : all) {
			if (!existEnd(str, searchBegin, roads)) { // û���Դ˽ڵ��β��·������֤���˽ڵ�Ϊ���ýڵ�
				isBegin = false;
				begins.add(str);
			} else if (!existBegin(str, searchEnd, roads)) {// û���Դ˽ڵ㿪ͷ��·������֤���˽ڵ�Ϊ���ýڵ�
				isEnd = false;
				ends.add(str);
			} else {
				result.add(str); // ���õĽڵ�
			}
		}
		if (isBegin == true && isEnd == true) {
			return result;
		} else {
			return filterInvalidNode(result, roads, begins, ends, searchBegin, searchEnd);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MapVisit visit = new MapVisit();
		List<Road> roads = visit.init(); // �������·��--Ϊ��֪����
		Set<String> invalidBegins = new HashSet<String>(); // ��Ч����ʼ�ڵ�
		Set<String> invalidEnds = new HashSet<String>(); // ��Ч�Ľ����ڵ�
		Set<String> allNodes = visit.getAllNodes(roads);
		String searchBegin = "A"; //��ʼ��
		String searchEnd = "Z"; //�ս��
		visit.filterInvalidNode(allNodes, roads, invalidBegins, invalidEnds,searchBegin,searchEnd); //��ȡ����Ч��ʼ�ڵ����Ч������
		Set<Road> invalidRoads = visit.deleteRoad(invalidBegins, invalidEnds,
				roads); // ��ȡ��Ҫɾ����·��
		roads.removeAll(invalidRoads); // ɾ����Ч��·��
		Practise pra = new Practise(roads);
		//�@ȡ����Ч·���ĺ��ķ���
		pra.getAllRoad(searchBegin, searchEnd);

		Iterator<String> it = pra.getresultSet().iterator();
		System.out.println("-----------------��" + searchBegin + "��" + searchEnd + "����Ч·������-----------------");
		while (it.hasNext()) {
			System.out.println(it.next());
		}
		System.out.println("-----------------��������·���л�·-----------------");
		Set<Road> cir = pra.getCirList();
		for(Road r :cir) {
			System.out.println(r);
		}
		
		
		
		
	}

}
