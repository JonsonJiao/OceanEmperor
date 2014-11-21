package test.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Practise {
	public Practise(List<Road> roads) {
		this.roadList = roads;
	}

	public List<Road> getroadList() {
		return roadList;
	}

	public void setroadList(List<Road> roadList) {
		this.roadList = roadList;
	}

	public List<String> getbackList() {
		return backList;
	}

	public void setbackList(List<String> backList) {
		this.backList = backList;
	}

	public Set<String> getresultSet() {
		return resultSet;
	}

	public void setresultSet(Set<String> resultSet) {
		this.resultSet = resultSet;
	}

	List<Road> roadList = null; //��֪��·��
	List<String> backList = new ArrayList<String>(); //����Ѿ����ʹ��Ľڵ�
	Set<String> resultSet = new HashSet<String>(); //Ŀ��·��--���ʽ��
	Set<Road> cirList = new HashSet<Road>(); 	//��·
	
	/**
	 * ·�������ĺ����㷨
	 * @param start
	 * ---��ҪѰ�ҵ���ʼ�ڵ㡣������ΪA
	 * @param destination
	 * ---��ҪѰ�ҵ��ս�㡣������ΪZ
	 */
	public void getAllRoad(String start, String destination) {
		backList.add(start);
		for (int z = 0; z < roadList.size(); z++) {
			if (roadList.get(z).getBegin().equals(start)) { //Ѱ������start��ʼ��·��
				if (roadList.get(z).getEnd().equals(destination)) { //�����destination��β����Ϊһ����Ч·��
					resultSet.add(backList.toString().substring(0, backList.toString().lastIndexOf("]")) + "," + destination + "]");
					continue;
				}
				if (!backList.contains(roadList.get(z).getEnd())) {//�˽ڵ���δ���������������
					getAllRoad(roadList.get(z).getEnd(), destination);
				} else {//�C���л�·
					cirList.add(roadList.get(z));
					//System.out.println("wwww");
				}
			}
		}
		backList.remove(start);
	}

	public Set<Road> getCirList() {
		return cirList;
	}

	public void setCirList(Set<Road> cirList) {
		this.cirList = cirList;
	}
}
