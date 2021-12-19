import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;

import java.awt.GridLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.FlowLayout;
import java.awt.Color;

import javax.swing.JLabel;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.miginfocom.swing.MigLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import java.awt.CardLayout;

import javax.swing.border.TitledBorder;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.jgoodies.forms.factories.FormFactory;

import javax.swing.SwingConstants;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;

public class Frame extends JFrame {
	private JTextField textField;
	private JTextArea textArea;

	/**
	 * Launch the application.
	 */

	public void printMessage(String message) {
		this.textArea.append(message);
		scrollDown();
	};

	public void scrollDown() {
		this.textArea.setCaretPosition(this.textArea.getText().length());
	}

	/**
	 * Create the frame.
	 */
	public Frame() {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1);
		panel_1.setLayout(new BorderLayout(10, 5));

		textField = new JTextField();
		panel_1.add(textField, BorderLayout.SOUTH);
		textField.setColumns(10);

		// input listener
		Action action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				String newText = new String(textField.getText());
				vault.command(newText);
				textField.setText("");

			}
		};
		textField.addActionListener(action);

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.CENTER);
		panel_2.setBorder(new TitledBorder(null, "Display Area", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBackground(Color.ORANGE);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.PINK);

		textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
		JScrollPane scrollPane = new JScrollPane(textArea);
		panel_2.add(scrollPane);

		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel.setLayout(new GridLayout(4, 2, 0, 0));

		JButton button = new JButton("Exit");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				vault.command("exit");

			}

		});
		panel.add(button);

		JButton btnNewButton = new JButton("Save");
		btnNewButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				vault.command("save");

			}

		});
		panel.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("Commands");

		btnNewButton_1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String newText = new String(textField.getText());
				vault.command(newText);
				textField.setText("add change reload exit delete ctc\n");

			}
		});

		panel.add(btnNewButton_1);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		pack();
		setLocation(100, 100);
		setSize(1100, 700);
		setVisible(true);
		setTitle("Password Bank");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		textField.requestFocusInWindow();

	}
}
