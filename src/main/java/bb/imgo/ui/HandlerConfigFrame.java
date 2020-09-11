package bb.imgo.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import bb.imgo.handlers.MediaHandler;

public class HandlerConfigFrame extends JFrame implements ActionListener {

	static private Logger logger = Logger.getLogger(HandlerConfigFrame.class.getName());
	
	private static final long serialVersionUID = -6656379413107569819L;
	MediaHandler handler;
	EditHandlersFrame ehFrame;
	
	HashMap<String, JTextField> optionFields;
	
	public HandlerConfigFrame(MediaHandler handler, EditHandlersFrame ehFrame) {
		this.handler = handler;
		this.ehFrame = ehFrame;
		optionFields = new HashMap<String, JTextField>();
		init();
	}
	
	protected void init() {
		this.setLayout(new BorderLayout());
		JPanel checkPanel = new JPanel();
		Map<String, String> config = handler.getConfigurationOptions();
		if (config == null || config.size() == 0) {
			logger.error("No config options for "+handler);
			return;
		}
		
		checkPanel.setLayout(new GridLayout(config.size(), 1));
		Set<String> keys = config.keySet();
		ArrayList<String> keyList = new ArrayList<String>(keys);
		
		Collections.sort(keyList);
		for (String opt : keyList) {
			String val = config.get(opt);
			
			JPanel hPanel = new JPanel();
			hPanel.setLayout(new BorderLayout());
			JLabel keyLabel = new JLabel(opt);
			keyLabel.setFont(OverviewFrame.labelFont);
						
			JTextField valField = new JTextField(val);
			valField.setFont(OverviewFrame.labelFont);
			
			hPanel.add(keyLabel, BorderLayout.WEST);
			hPanel.add(valField, BorderLayout.CENTER);
			checkPanel.add(hPanel);
			optionFields.put(opt, valField);
			hPanel.setPreferredSize(new Dimension(400,40));
		}
		
		this.add(checkPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(this);
		okButton.setFont(OverviewFrame.labelFont);
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		buttonPanel.add(cancelButton);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (String opt : optionFields.keySet()) {
			JTextField field = optionFields.get(opt);
			if (field != null) {
				String val = field.getText();
				if (val != null) {
					val = val.trim();
				}
				logger.info("Setting option "+opt+" to "+val);
				handler.setConfigurationOption(opt, val);
			}
		}
		setVisible(false);
		dispose();
		// Reinit the EditHandlers frame to catch any changes we just made to descriptions to handlers
		ehFrame.init();		
	}
}
