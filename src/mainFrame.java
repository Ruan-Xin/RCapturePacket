import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.NetworkInterface;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;

public class mainFrame extends JFrame implements Runnable,ActionListener{
	private JPanel jp;
	private JPanel TablePanel;
	private JPanel desPanel;

	JTable table;
	String []entry = {"No.","Time","Source","Destination","Protocol","Length"};
	private DefaultTableModel model = new DefaultTableModel(null, entry);
	
	private JMenuBar menuBar;
	private JMenu Op;
	private JMenu selectNetWork;
	private CatchPacket cp = new CatchPacket();
	
	private boolean isRun = false;
	
	private static mainFrame mf = new mainFrame();
	public static mainFrame getInstance() {
		return mf;
	}
	private mainFrame() {
		// TODO Auto-generated constructor stub
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toolkit toolkit = getToolkit();
		Dimension dim = toolkit.getScreenSize();
		setLocation((int)(dim.getWidth() - LenthAll.WINDOW_WIDTH) / 2,
				(int)(dim.getHeight() - LenthAll.WINDOW_HEIGHT) / 2);
		setSize(LenthAll.WINDOW_WIDTH, LenthAll.WINDOW_HEIGHT);
		setTitle("RoyCapture");
		jp = (JPanel) getContentPane();
		jp.setLayout(new GridLayout(2, 1));
		
		cp.getDevices();//��ʼ������
		cp.desNetworkInterface();//��ȡ��������
		//��һ�������ӿڣ�����������İ�
		cp.getCap(cp.devices[0], true, "");
		
		//��cell���óɲ��ɱ༭
		table = new JTable(model){
			public boolean isCellEditable(int row,int column) {
				return false;
			}
		};
		//�������
		createTable();
		//����������������
		createDes();
		//�����˵�
		menuBar = new JMenuBar();
		
		jp.add(TablePanel);
		jp.add(desPanel);
		
		initOpMenu();
		initSelectNetWork();
		setJMenuBar(menuBar);
		setVisible(true);
	}
	//�����˵�
	public void initOpMenu() {
		Op = new JMenu("Operate");
		menuBar.add(Op);
		JMenuItem begin = new JMenuItem("begin");
		JMenuItem end = new JMenuItem("end");
		begin.addActionListener(this);
		end.addActionListener(this);
		Op.add(begin);Op.add(end);
	}
	public void initSelectNetWork() {
		selectNetWork = new JMenu("NetWork");
		menuBar.add(selectNetWork);
		HashMap<jpcap.NetworkInterface, StringBuilder> hm = cp.getNetWorkDes();
		for(int i = 1; i <= hm.size();i++){
			NetWorkJMenuItem inface = new NetWorkJMenuItem("Interface:" + i);
//			JMenuItem inface = new JMenuItem("Interface:" + i);
			selectNetWork.add(inface);
		}
	}
	//��׽���б�
	public void createTable() {
		TablePanel = new JPanel();
		table.setModel(model);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		TablePanel.setLayout(new BoxLayout(TablePanel, BoxLayout.Y_AXIS));//��ֱ����
		TablePanel.add(Box.createVerticalStrut(10));//�붥�ľ���
		TablePanel.add(scrollPane);
		TablePanel.add(Box.createVerticalStrut(5));
	}
	
	//���ľ�������
	public void createDes() {
		desPanel = new JPanel();
		JTextPane jtp = new JTextPane();
		JScrollPane scrollPane = new JScrollPane(jtp);
		jtp.setEditable(false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		desPanel.setLayout(new BoxLayout(desPanel, BoxLayout.Y_AXIS));
		desPanel.setBackground(Color.WHITE);
		desPanel.setSize(0, 400);
		desPanel.add(scrollPane);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int count = 1;//����
		
		long startTime = System.currentTimeMillis();
		isRun = true;
		while(isRun){
			cp.beginCatch();
			PacketAtrr pa = cp.vc_patrr.removeFirst();
			long endTime = System.currentTimeMillis();
			long continueTime = endTime - startTime;
			String[] rowData = {count + "",continueTime + "",pa.getSourceaddr()
					,pa.getDestinationAddr(),pa.getProtocol(),pa.getLength() + ""};
			model.addRow(rowData);
			table.setModel(model);
			count++;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Thread td = new Thread(mf);
		if (e.getActionCommand().equals("begin")) {
			td.start();
		}
		else if (e.getActionCommand().equals("end")) {
			isRun = false;
		}
	}
}
