import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import jpcap.JpcapCaptor;
import jpcap.JpcapWriter;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.DatalinkPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

public class CatchPacket implements PacketReceiver,Runnable{
	public NetworkInterface[] devices;
	public NetworkInterface device;//���������м���
	public JpcapCaptor jCaptor;
	public LinkedList<Packet> packets = new LinkedList();
	public HashMap<NetworkInterface, StringBuilder> hm_inface_sb = new HashMap<>();
	
	//�������ӿ�
	public NetworkInterface[] getDevices() {
		devices = JpcapCaptor.getDeviceList();
		return devices;
	}
	//�������list����
	public void desNetworkInterface() {
		for(int i = 0; i < devices.length; i++){
			StringBuilder sb_network = new StringBuilder(); 
			sb_network.append("�ӿ�" + (i + 1) + ":\n");
			sb_network.append("�ӿ�����:" + devices[i].name + "\n");


			if (!(devices[i].name.contains("GenericDialupAdapter"))) {
				device = devices[i];
			}
			
			sb_network.append("����ӿ�����:" + devices[i].description + "\n");
			sb_network.append("������·������:" + devices[i].datalink_name + "\n");
			sb_network.append("������·������:" + devices[i].description + "\n");
			//loopbackΪ���ص�ַ
			sb_network.append("�Ƿ����LOOPBACK�豸:" + devices[i].loopback + "\n");
			sb_network.append("MAC��ַ:");
			int count = 0;
			for(byte b:devices[i].mac_address){
				count++;
				if (count < devices[i].mac_address.length) {
					sb_network.append(Integer.toHexString(b & 0xff) + ":");
				}else {
					sb_network.append(Integer.toHexString(b & 0xff) + "\n");
				}
			}
			hm_inface_sb.put(devices[i], sb_network);
		}
	}
	//���ĳ������������
	public void getCap(NetworkInterface nInterface, boolean mixMode,String filter){
		try {
			/*�������壺
			 * 1.��Ҫʹ�õ�ĳ�������ӿ�
			 * 2.һ����ץȡ������󳤶�
			 * 3.�����Ƿ����ģʽ�����ڻ���ģʽ�������������ݰ�
			 * ���������Ϊ����ģʽ������˰����˺���setFilter()�������κ����ã�
			 * 4.ָ����ʱ��ʱ��*/
			jCaptor = JpcapCaptor.openDevice(nInterface, 65535, mixMode, 20);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//���ù�����
	public void setFilter(String filter) {
		try {
			jCaptor.setFilter(filter, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//����ץ��ģʽ��ʼץ��
	public void beginCatch() {
		jCaptor.processPacket(1, this);
	}
	
	//����ץ��
	public void endCatch() {
		if (jCaptor != null) {
			jCaptor.breakLoop();
		}
	}
	
	//������İ��浽�ļ���
	public void saveFile(String fileName) {
		if (jCaptor == null) {
			JOptionPane.showMessageDialog(null, "δ���񵽰���");
		}else {
			try {
				JpcapWriter writer = JpcapWriter.openDumpFile(jCaptor, fileName);
				while(packets.size() != 0){
					writer.writePacket(packets.removeFirst());
				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//�򿪴����ļ��еİ�
	public void openFile(String fileName) {
		try {
			jCaptor = jCaptor.openFile(fileName);
			while(true){
				Packet packet = jCaptor.getPacket();
				if (packet == null) {//??
					break;
				}
				analysis(packet);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (jCaptor != null) {
				jCaptor.close();
			}
		}
	}
	public void analysis(Packet packet) {
		StringBuilder sb_analysis = new StringBuilder();
		sb_analysis.append("Captured Length:"+packet.caplen+" byte\n");
		sb_analysis.append("Length of this Packet:"+packet.len+" byte/n");
		sb_analysis.append("Header:"+packet.header+"/n");
		sb_analysis.append("Length of Header:"+packet.header.length+" byte/n");
		sb_analysis.append("Data:"+packet.data+"/n");
		sb_analysis.append("Length of Data:"+packet.data.length+" byte/n");
		
		DatalinkPacket dpacket = packet.datalink;
		if (dpacket instanceof EthernetPacket) {//��̫��֡
			EthernetPacket ePacket = (EthernetPacket)dpacket;
			int count = 0;
			int count2 = 0;
			sb_analysis.append("src_mac:");
			for(byte b:ePacket.src_mac){
				count++;
				if (count < ePacket.src_mac.length) {
					sb_analysis.append(Integer.toHexString(b & 0xff)+" : ");
				}else {
					sb_analysis.append(Integer.toHexString(b & 0xff) + "/n");
				}
			}
			sb_analysis.append("dst_mac:");
			for(byte b : ePacket.dst_mac){
				count2++;
				if (count2 < ePacket.dst_mac.length) {
					sb_analysis.append(Integer.toHexString(b & 0xff) + ":");
				}else {
					sb_analysis.append(Integer.toHexString(b & 0xff) + "\n");
				}
			}
			sb_analysis.append("frametype:" + Integer.toHexString(ePacket.frametype & 0xffff) + "\n");
		}else {
			sb_analysis.append(dpacket + "\n");
		}
		if(packet instanceof ARPPacket){               //����ARPЭ��
			sb_analysis.append("---ARP---/n");
			ARPPacket aPacket = (ARPPacket)packet;
			sb_analysis.append("Ӳ�����ͣ�"+aPacket.hardtype+"/n");
			sb_analysis.append("Э�����ͣ�"+aPacket.prototype+"/n");
			sb_analysis.append("Ӳ����ַ���ȣ�"+aPacket.hlen+"/n");
			sb_analysis.append("Э���ַ���ȣ�"+aPacket.plen+"/n");
			sb_analysis.append("Operation��"+aPacket.operation+"/n");
			sb_analysis.append("������Ӳ����ַ��"+aPacket.sender_hardaddr+"/n");
			sb_analysis.append("������Э���ַ��"+aPacket.sender_protoaddr+"/n");
			sb_analysis.append("Ŀ��Ӳ����ַ��"+aPacket.target_hardaddr+"/n");
			sb_analysis.append("Ŀ��Э���ַ��"+aPacket.target_protoaddr+"/n");
			sb_analysis.append("------------------/n");
		}
		if(packet instanceof ICMPPacket){          //����ICMPЭ��
			sb_analysis.append("---ICMP---/n");        
			ICMPPacket iPacket = (ICMPPacket)packet;
			sb_analysis.append("ICMP_TYPE:"+iPacket.type+"/n");
			sb_analysis.append("����ICMP��ʽ���෱�࣬��ʡȥ������/n");
			sb_analysis.append("------------------/n");
		}
		if(packet instanceof IPPacket){        //����IP
			IPPacket iPacket = (IPPacket)packet;
			sb_analysis.append("---IP�汾: "+iPacket.version+" ---/n");
			if(iPacket.version==4){                //����IPv4Э��
				sb_analysis.append("Type of service:"+iPacket.rsv_tos+"/n");
				sb_analysis.append("Priprity:"+iPacket.priority+"/n");
				sb_analysis.append("Packet Length:"+iPacket.length+"/n");
				sb_analysis.append("Identification:"+iPacket.ident+"/n");
				sb_analysis.append("Don't Frag? "+iPacket.dont_frag+"/n");
				sb_analysis.append("More Frag? "+iPacket.more_frag+"/n");
				sb_analysis.append("Frag Offset:"+iPacket.offset+"/n");
				sb_analysis.append("Time to Live:"+iPacket.hop_limit+"/n");
				sb_analysis.append("Protocol:"+iPacket.protocol+"        (TCP = 6; UDP = 17)/n");
				sb_analysis.append("Source address:"+iPacket.src_ip.toString()+"/n");
				sb_analysis.append("Destination address:"+iPacket.dst_ip.toString()+"/n");
				sb_analysis.append("Options:"+iPacket.option+"/n");
				sb_analysis.append("------------------/n");
			}
			if(iPacket instanceof UDPPacket){      //����UDPЭ��
				sb_analysis.append("---UDP---/n");
				UDPPacket uPacket = (UDPPacket)iPacket;
				sb_analysis.append("Source Port:"+uPacket.src_port+"/n");
				sb_analysis.append("Destination Port:"+uPacket.dst_port+"/n");
				sb_analysis.append("Length:"+uPacket.length+"/n");
				sb_analysis.append("------------------/n");
				if(uPacket.src_port==53||uPacket.dst_port==53){  //����DNSЭ��
					sb_analysis.append("---DNS---/n");
					sb_analysis.append("�˰���ץ�񣬷�����.../n");
					sb_analysis.append("------------------/n");
				}
			}
			if(iPacket instanceof TCPPacket){      //����TCPЭ��
				sb_analysis.append("---TCP---/n");
				TCPPacket tPacket = (TCPPacket)iPacket;
				sb_analysis.append("Source Port:"+tPacket.src_port+"/n");
				sb_analysis.append("Destination Port:"+tPacket.dst_port+"/n");
				sb_analysis.append("Sequence Number:"+tPacket.sequence+"/n");
				sb_analysis.append("Acknowledge Number:"+tPacket.ack_num+"/n");
				sb_analysis.append("URG:"+tPacket.urg+"/n");
				sb_analysis.append("ACK:"+tPacket.ack+"/n");
				sb_analysis.append("PSH:"+tPacket.psh+"/n");
				sb_analysis.append("RST:"+tPacket.rst+"/n");
				sb_analysis.append("SYN:"+tPacket.syn+"/n");
				sb_analysis.append("FIN:"+tPacket.fin+"/n");
				sb_analysis.append("Window Size:"+tPacket.window+"/n");
				sb_analysis.append("Urgent Pointer:"+tPacket.urgent_pointer+"/n");
				sb_analysis.append("Option:"+tPacket.option+"/n");
				sb_analysis.append("------------------/n");
				if(tPacket.src_port==80 || tPacket.dst_port==80){     //����HTTPЭ��
					sb_analysis.append("---HTTP---/n");
					byte[] data = tPacket.data;
					if(data.length==0){
						sb_analysis.append("��Ϊ�������ݵ�Ӧ���ģ�/n");
					}else{
						if(tPacket.src_port==80){                 //����HTTP��Ӧ
							String str = null;
							try {
								String str1 = new String(data,"UTF-8");
								if(str1.contains("HTTP/1.1")){
									str = str1;
								}else{
									String str2 = new String(data,"GB2312");
									if(str2.contains("HTTP/1.1")){
										str = str2;
									}else{
										String str3 = new String(data,"GBK");
										if(str3.contains("HTTP/1.1")){
											str = str3;
										}else{
											str = new String(data,"Unicode");
										}
									}
								}

								sb_analysis.append(str+"/n");
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if(tPacket.dst_port==80){
							try{
								String str = new String(data,"ASCII");
								sb_analysis.append(str);
							}catch (Exception e) {
								// TODO: handle exception
							}
						}
					}
				}
			}
			sb_analysis.append("/n");
			System.out.println(sb_analysis);
		}
	}

	public HashMap<NetworkInterface, StringBuilder> getNetWorkDes() {
		return hm_inface_sb;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			try {
				Thread.sleep(5000);
				beginCatch();
				//System.out.println(111);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receivePacket(Packet packet) {
		// TODO Auto-generated method stub
		packets.add(packet);
		analysis(packet);
	}

}
