package puzzles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class SecretCode {

	static private String Code="10:20:30:40";		
	static private String NoPic = "src/resources/ho-ho-no.png";
	static private String YesPic = "src/resources/Kupo.jpg";
	
	protected String[] codeList;
	protected JTextField[] codeInput;
	
	public SecretCode(String code) {
		codeList = code.split(":");
		jInit();		
	}
	
	private void jInit() {
		JFrame mainFrame = new JFrame();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel codePanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		JLabel topLabel = new JLabel("Enter the code:");
		topLabel.setHorizontalAlignment(SwingConstants.CENTER);
		topLabel.setFont(new Font("Helvetica", Font.BOLD, 30));
		
		//codePanel.setLayout(new FlowLayout());
		codeInput = new JTextField[codeList.length];
		for (int i=0; i<codeList.length; ++i) {
			codeInput[i] = new JTextField("");
			codeInput[i].setBackground(Color.lightGray);
			codeInput[i].setForeground(Color.blue);
			codeInput[i].setFont(new Font("Arial", Font.BOLD, 120));
			codeInput[i].setPreferredSize(new Dimension(200,225));
			final int myIndex = i;
			codeInput[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String val = codeInput[myIndex].getText();
					if (val.trim().equalsIgnoreCase(codeList[myIndex])) {
						codeInput[myIndex].setForeground(Color.green);
						//checkAll();
					} else {
						codeInput[myIndex].setForeground(Color.red);
						System.out.println("Got: "+val+" Expected: "+codeList[myIndex]);
					}
				}
			});
			codePanel.add(codeInput[i]);
			JSeparator s1 = new JSeparator();
			s1.setBackground(Color.black);
			codePanel.add(s1);
		}
		
		mainPanel.add(topLabel, BorderLayout.NORTH);
		mainPanel.add(codePanel, BorderLayout.CENTER);
		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		buttonPanel.add(quitButton);
		JButton submitButton = new JButton("Check Answer");
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!checkAll()) {
					ImageFrame noFrame = new ImageFrame(NoPic, 500, 520);
					noFrame.setVisible(true);
				} else {
					ImageFrame yesFrame = new ImageFrame(YesPic, 500, 520);
					yesFrame.setVisible(true);
				}
			}
		});
		buttonPanel.add(submitButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		mainFrame.setContentPane(mainPanel);
		mainFrame.setSize(new Dimension(50 + (codeList.length * 225), 400));
		mainFrame.setVisible(true);
	}
	
	protected boolean checkAll() {
		boolean wrong = false;
		for (int i=0; i<codeList.length; ++i) {
			String iVal = codeInput[i].getText();
			String eVal = codeList[i];
			if (!iVal.trim().equalsIgnoreCase(eVal)) {
				System.err.println(i+" expected "+eVal+" got "+iVal);
				codeInput[i].setForeground(Color.red);
				wrong = true;
			} else {
				codeInput[i].setForeground(Color.green);
			}
		}
		return !wrong;
	}
	
	public static void main(String[] args) {
		new SecretCode(Code);
	}
	
}
