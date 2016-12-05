import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class mainFrame extends JFrame{
	private JPanel jp;
	private JPanel TablePanel;
	private JPanel desPanel;
	private JButton btn = new JButton("begin");
	JTable table = new JTable();
	String []entry = {"No.","Time","Source","Destination","Protocol","Length"};
	String [][]Data = {{"1","0.0000","172.24.2.224","224.0.0.252","TCP","66"}};
	String []d = {"1","0.0000","172.24.2.224","224.0.0.252","TCP","66"};
	private DefaultTableModel model = new DefaultTableModel(Data, entry);
	public mainFrame() {
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
		jp = (JPanel) getContentPane();
//		jp.setLayout(null);
		jp.setLayout(new GridLayout(2, 1));
		createTable();
		jp.add(TablePanel);
		createDes();
		jp.add(desPanel);
		setVisible(true);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addRow(d);
		table.setModel(model);
	}
	
	//��׽���б�
	public void createTable() {
		TablePanel = new JPanel();

		Vector<String[]> vc = new Vector<>();
		vc.add(d);
		
		//DefaultTableModel model = new DefaultTableModel(Data, entry);
		table.setModel(model);


		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		TablePanel.setLayout(new BoxLayout(TablePanel, BoxLayout.Y_AXIS));//��ֱ����
		TablePanel.add(Box.createVerticalStrut(10));//�붥�ľ���
		TablePanel.add(scrollPane);
		TablePanel.add(Box.createVerticalStrut(5));
		TablePanel.setSize(0, 100);
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
		jtp.setText("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
		desPanel.add(scrollPane);
	}
}