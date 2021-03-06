import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JList;
import javax.swing.JScrollPane;


public class InfoCollect {

	private JFrame frame;
	private JButton btnStart;
	private JButton btnPause;
	JLabel lblState;
	JProgressBar bar;
	private JList<String> listProcess;
	private DefaultListModel<String> listModel;
	private USamazonCrawler usAmazon;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InfoCollect window = new InfoCollect();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public InfoCollect() {
		initialize();
		usAmazon = new USamazonCrawler(bar,lblState,listModel);
		listProcess = new JList<String>(listModel);
		listProcess.setBounds(29, 94, 636, 334);
		frame.getContentPane().add(listProcess);
		usAmazon.start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("データ収集ツール");
		frame.setResizable(false);
		frame.setBounds(100, 100, 695, 469);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage("img/icon.png"));
		
		btnStart = new JButton("開始");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				usAmazon.processStart();
				
			}
		});
		btnStart.setBounds(29, 19, 117, 29);
		frame.getContentPane().add(btnStart);
		
		btnPause = new JButton("中断");
		btnPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	
				usAmazon.processPause();
			}
		});
		btnPause.setBounds(158, 19, 117, 29);
		frame.getContentPane().add(btnPause);
		
		lblState = new JLabel("起動しました");
		lblState.setBounds(313, 24, 352, 16);
		frame.getContentPane().add(lblState);
		
		bar = new JProgressBar();
		bar.setBounds(29, 60, 636, 22);
		frame.getContentPane().add(bar);
		
		listModel = new DefaultListModel<String>(); 
	}
}
