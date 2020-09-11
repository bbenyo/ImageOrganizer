package bb.imgo.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import bb.imgo.MediaHandlerController;
import bb.imgo.handlers.DeleteFileType;
import bb.imgo.handlers.DirectoryNameUnderscoreToDash;
import bb.imgo.handlers.FixFileExtension;
import bb.imgo.handlers.HEICConverter;
import bb.imgo.handlers.KeepOnlyFileType;
import bb.imgo.handlers.MediaHandler;
import bb.imgo.handlers.MoveToDateSubdirectory;
import bb.imgo.handlers.RemoveDuplicates;
import bb.imgo.handlers.RemoveEmptySubdirectory;
import bb.imgo.handlers.SeparateVideos;
import bb.imgo.handlers.Statistics;
import bb.imgo.handlers.UserChooser;
import bb.imgo.handlers.VerifyBackup;
import bb.imgo.handlers.VideoRenameAndTag;

public class EditHandlersFrame extends JFrame implements ActionListener {

	static private Logger logger = Logger.getLogger(EditHandlersFrame.class.getName());
	
	private static final long serialVersionUID = 8456659384079456968L;
	
	List<JCheckBox> handlers;
	MediaHandlerController controller;
	JPanel checkPanel = null;
	JPanel bottomPanel = null;
	
	public EditHandlersFrame(MediaHandlerController controller) {
		this.controller = controller;
		init();
	}
	
	protected void init() {
		if (controller == null) {
			logger.error("Null controller in EditHandlersFrame!");
			return;
		}
			
		List<MediaHandler> activeHandlers = controller.getHandlers();
		List<MediaHandler> allHandlers = getAllHandlers();
		List<MediaHandler> handlers = new ArrayList<MediaHandler>();
		List<MediaHandler> disabledHandlers = new ArrayList<MediaHandler>();
		handlers.addAll(activeHandlers);
		for (MediaHandler aHandler : allHandlers) {
			boolean addMe = true;
			for (MediaHandler handler : activeHandlers) {
				if (handler.getClass().equals(aHandler.getClass())) {
					addMe = false;
					break;
				}
			}
			if (addMe) {
				handlers.add(aHandler);
				disabledHandlers.add(aHandler);
			}
		}
		this.setLayout(new BorderLayout());
		
		if (checkPanel != null) {
			this.remove(checkPanel);
		}		
		checkPanel = new JPanel();
		checkPanel.setLayout(new GridLayout(handlers.size(), 1));
		
		for (MediaHandler handler : handlers) {
			JPanel hPanel = new JPanel();
			hPanel.setLayout(new BorderLayout());
			JCheckBox cb = new JCheckBox(handler.getLabel());
			
			// TODO: Allow you to turn on/off handlers
			cb.setEnabled(false);
			
			cb.setFont(OverviewFrame.labelFont);
			cb.setSelected(true);
			
			if (disabledHandlers.contains(handler)) {
				cb.setSelected(false);
			}
			
			JLabel desc = new JLabel(": "+handler.getDescription());
			desc.setFont(OverviewFrame.labelFont);
			hPanel.add(cb, BorderLayout.WEST);
			hPanel.add(desc, BorderLayout.CENTER);

			Map<String, String> cOptions = handler.getConfigurationOptions();
			if (cOptions != null && cOptions.size() > 0) {
				JButton config = new JButton("Configure");
				config.setFont(OverviewFrame.labelFont);
				config.setActionCommand(handler.getClass().getCanonicalName());
				config.addActionListener(this);
				hPanel.add(config, BorderLayout.EAST);
				config.setEnabled(true);
			}
			checkPanel.add(hPanel);
		}

		this.add(checkPanel, BorderLayout.CENTER);
		
		if (bottomPanel == null) {
			bottomPanel = new JPanel();

			JButton close = new JButton("Ok");
			close.setFont(OverviewFrame.labelFont);
			bottomPanel.add(close);
		
			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					
					// TODO:
					// Turn off any handler that is marked off
					
					// Add handlers that are marked on
				}
			});
		}

		this.add(bottomPanel, BorderLayout.SOUTH);
		this.pack();
	}

	protected List<MediaHandler> getAllHandlers() {
		// Register handlers here to get them to appear in the Edit Handler frame
		List<MediaHandler> handlerClasses = new ArrayList<MediaHandler>();
		
		handlerClasses.add(new DeleteFileType());
		handlerClasses.add(new DirectoryNameUnderscoreToDash());
		handlerClasses.add(new FixFileExtension());
		handlerClasses.add(new HEICConverter());
		handlerClasses.add(new KeepOnlyFileType());
		handlerClasses.add(new MoveToDateSubdirectory());
		handlerClasses.add(new RemoveDuplicates());
		handlerClasses.add(new RemoveEmptySubdirectory());
		handlerClasses.add(new SeparateVideos());
		handlerClasses.add(new Statistics());
		handlerClasses.add(new UserChooser());
		handlerClasses.add(new VerifyBackup());
		handlerClasses.add(new VideoRenameAndTag());		
		
		return handlerClasses;		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src instanceof JButton) {
			JButton btn = (JButton)src;
			String handlerCls = btn.getActionCommand();
			try {
				Class<?> handlerClass = Class.forName(handlerCls);
				MediaHandler handler = controller.getSpecificHandler(handlerClass);
				if (handler == null) {
					logger.error("Unable to find MediaHandler: "+handlerCls);
					return;
				}
				if (handler.getConfigurationOptions() == null || handler.getConfigurationOptions().size() == 0) {
					logger.warn("No config options for "+handlerCls);
					return;
				}
				HandlerConfigFrame configFrame = new HandlerConfigFrame(handler, this);
				configFrame.setLocationRelativeTo(this);
				configFrame.setVisible(true);
				
			} catch (ClassNotFoundException e1) {
				logger.error(e1.toString(), e1);
			}
		}
		
	}
}
