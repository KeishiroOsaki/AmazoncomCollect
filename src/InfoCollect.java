import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JList;


public class InfoCollect {

	private JFrame frame;
	private JButton btnStart;
	private JButton btnPause;
	private JLabel lblState;
	private JProgressBar bar;
	private JList listProcess;

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
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 545, 353);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		btnStart = new JButton("開始");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnStart.setBounds(29, 19, 117, 29);
		frame.getContentPane().add(btnStart);
		
		btnPause = new JButton("中断");
		btnPause.setBounds(158, 19, 117, 29);
		frame.getContentPane().add(btnPause);
		
		lblState = new JLabel("状況");
		lblState.setBounds(313, 24, 210, 16);
		frame.getContentPane().add(lblState);
		
		bar = new JProgressBar();
		bar.setBounds(29, 53, 494, 29);
		frame.getContentPane().add(bar);
		
		listProcess = new JList();
		listProcess.setBounds(29, 94, 494, 214);
		frame.getContentPane().add(listProcess);
	}
}
