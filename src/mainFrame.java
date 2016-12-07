import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Vector;

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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class mainFrame extends JFrame implements Runnable,ActionListener,MouseListener{
	private JPanel jp;
	private JPanel TablePanel;
	private JPanel desPanel;
	private	JTextPane jtp;
	
	private int count = 1;// ����
	private JTable table;
	private String []entry = {"No.","Time","Source","Destination","Protocol","Length"};
	private HashMap<String, DefaultTableModel> hm_str_model = new HashMap<>();
	//��ǰ����model
	private DefaultTableModel currentModel = new DefaultTableModel(null, entry);
	private JMenuBar menuBar;
	private JMenu Op;
	private JMenu selectNetWork;
	private CatchPacket cp = new CatchPacket();
	private Vector<NetWorkJMenuItem> vc_item = new Vector<>();
	
	//���paֵ�����Jtextpane���@ʾ���w��������mouseclick̎���{
	private Vector<PacketAtrr> vc_packet = new Vector<>();
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
		cp.getCap(cp.device, true, "");

		//�����˵�
		menuBar = new JMenuBar();
		initOpMenu();
		initSelectNetWork();
		
		currentModel = hm_str_model.get(vc_item.get(0).getText());
		//��cell���óɲ��ɱ༭
		table = new JTable(currentModel){
			public boolean isCellEditable(int row,int column) {
				return false;
			}
		};
		table.addMouseListener(this);
		//�������
		createTable();
		//����������������
		createDes();
		jp.add(TablePanel);
		jp.add(desPanel);
		

		setJMenuBar(menuBar);
		setVisible(true);
	}
	//�����˵�
	public void initOpMenu() {
		Op = new JMenu("Operate");
		menuBar.add(Op);
		JMenuItem begin = new JMenuItem("begin");
		JMenuItem end = new JMenuItem("end");
		JMenuItem clear = new JMenuItem("clear");
		begin.addActionListener(this);
		end.addActionListener(this);
		clear.addActionListener(this);
		Op.add(begin);Op.add(end);Op.add(clear);
	}
	public void initSelectNetWork() {
		selectNetWork = new JMenu("NetWork");
		menuBar.add(selectNetWork);
		//HashMap<NetworkInterface, StringBuilder> hm = cp.getNetWorkDes();
		for(int i = 1; i <= cp.devices.length;i++){
			NetWorkJMenuItem inface = new NetWorkJMenuItem("Interface:" + i);
			vc_item.add(inface);//��ͬ�ӿڵ�item
			hm_str_model.put(vc_item.get(i - 1).getText(), new DefaultTableModel(null, entry));
			selectNetWork.add(inface);
			inface.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					currentModel = hm_str_model.get(inface.getText());
					System.out.println(inface.getText());
					for (int j = 0; j < vc_item.size(); j++) {
						if (vc_item.get(j) != inface) {
							vc_item.get(j).removeImage();
						}else {
							vc_item.get(j).addImage();
							cp.device = cp.devices[j];
							cp.getCap(cp.device, true, "");
						}
					}
					
				}
			});
		}
		vc_item.get(0).addImage();//Ĭ��Ϊ��һ������
	}
	//��׽���б�
	public void createTable() {
		TablePanel = new JPanel();
		table.setModel(currentModel);

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
		jtp = new JTextPane();
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

		long startTime = System.currentTimeMillis();
		isRun = true;
		DefaultTableModel temp_model = currentModel;
		while (isRun) {
			cp.beginCatch();
			// ͬ������ֹ��̬����
			if (cp.vc_patrr.size() != 0) {
//				synchronized (this) {
					if (temp_model == currentModel) {
						PacketAtrr pa = cp.vc_patrr.removeFirst();
						vc_packet.add(pa);
						long endTime = System.currentTimeMillis();
						long continueTime = endTime - startTime;
						String[] rowData = { count + "", continueTime + "", pa.getSourceaddr(), pa.getDestinationAddr(),
								pa.getProtocol(), pa.getLength() + "" };
						currentModel.addRow(rowData);
						currentModel.addTableModelListener(new TableModelListener() {
							
							@Override
							public void tableChanged(TableModelEvent e) {
								// TODO Auto-generated method stub
								jtp.setText(pa.getDes().toString());
							}
						});
						table.setModel(currentModel);
						count++;
					} else {
						cp.vc_patrr.clear();
						table.setModel(currentModel);
						jp.updateUI();
						temp_model = currentModel;
						jtp.setText("");
						count = 1;
					}
				}
			//}
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
			cp.vc_patrr.clear();
		}
		else if (e.getActionCommand().equals("clear")) {
			DefaultTableModel model = new DefaultTableModel(null, entry);
			cp.vc_patrr.clear();
			jtp.setText("");
			table.setModel(model);
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		int row = table.getSelectedRow();
		//System.out.println(row);
		jtp.setText(vc_packet.get(row).getDes().toString());
		jtp.updateUI();
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
