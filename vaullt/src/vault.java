import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.print.DocFlavor.URL;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;

public class vault {
	private static String separator = "###########################################################################################################################################\n";
	private static int previousnumberofentries;
	private static long previouschecksum;
	private static long checksum;
	private static final int CRC_OFFSET = 0x08;
	private static final int NUM_OF_ENTRIES_OFFSET = 0x00;
	private static final int FIRST_ENTRY_OFFSET = 0x10;
	private static final int ENTRY_MEMBER_SIZE = 32;
	private static final int SITE_OFFSET = 0;
	private static final int EMAIL_OFFSET = ENTRY_MEMBER_SIZE;
	private static final int USER_OFFSET = ENTRY_MEMBER_SIZE * 2;
	private static final int PASSWORD_OFFSET = ENTRY_MEMBER_SIZE * 3;
	private static final int ENTRY_SIZE = ENTRY_MEMBER_SIZE * 4;

	private static Map<String, byte[]> entries;


	private static byte iv_password[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private static byte[] password;
	private static String filename;
	private static Frame frame;
	

	/* Main */
	public static void main(String[] args) {
		
		

		JPasswordField passwordField = new JPasswordField(20);
		passwordField.setEchoChar('*');

		JPanel panel = new JPanel();
		JLabel label = new JLabel("Enter a password:");

		panel.add(label);
		panel.add(passwordField);

		String[] options = { "OK" };

		JOptionPane pane = new JOptionPane(panel, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
				options[0]) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void selectInitialValue() {
				passwordField.requestFocusInWindow();
			}
		};

		// int option = JOptionPane.showOptionDialog(null, panel, "Enter
		// password",JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
		// options,options[0])

		try {
			pane.createDialog(null, "Password").setVisible(true);

			password = new String(passwordField.getPassword()).getBytes();
			// Still got problem with spaces that are being encoded as %20
			filename = (new File(ClassLoader.getSystemClassLoader().getResource(".").getPath())).getAbsolutePath()
					+ "/vault.data";

			Object opt = pane.getValue();
			if (opt != null && opt.toString().equals("OK"))
				
				if (password.length > 0) {
					
					// Check CRC
					frame = new Frame();
					loadData();
				}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	private static void loadData() {
		entries = new TreeMap<String, byte[]>();
		decryptEntries();
		
		if (checkCRC() == true) {
			
			frame.printMessage("Number of entries: " + previousnumberofentries + "\n");
			frame.printMessage("# Version compiled on 12.12.2017\n\n");
			commandPrintAll();
		
			
		} else {
			System.out.println("# Checksums didn'match: expected " + Long.toHexString(checksum));
			System.exit(1);

		
	}
		
	}
	/* Convert bytes to int */
	private static long convertByteArrayToLong(byte[] bytes) {
		long value = 0;
		if (bytes == null)
			return value;

		for (int i = 0; i < bytes.length; i++) {
			value = (value << 8) + (bytes[i] & 0xff);
		}
		return value;
	}

	/* Decrypt all entries */
	private static void decryptEntries() {
		byte[] tentry;
		byte[] entry;
		byte[] header_temp = new byte[16];
		InputStream is;
		try {
			File initialFile = new File(filename);
			is = new FileInputStream(initialFile);

			// read header

			is.read(header_temp, 0, header_temp.length);
			previouschecksum = convertByteArrayToLong(Arrays.copyOfRange(header_temp, CRC_OFFSET, FIRST_ENTRY_OFFSET));

			previousnumberofentries = header_temp[NUM_OF_ENTRIES_OFFSET];

			// TODO- print the rest of the header e.g. Date
			
			String tempkey,key;
			for (int i = 0; i < previousnumberofentries * ENTRY_SIZE; i = i + ENTRY_SIZE) {
				
				tentry = new byte[128];
				is.read(tentry, 0, tentry.length);

				entry = decrypt(tentry);
				tempkey = new String(Arrays.copyOfRange(entry, 0, EMAIL_OFFSET), "UTF-8");
				key = tempkey;
				int idx=0;
				
				while(entries.containsKey(tempkey)) {
					tempkey=key+"("+(++idx)+")";
				}
				//entries.put(new String(Arrays.copyOfRange(entry, 0, EMAIL_OFFSET), "UTF-8"), entry);
				entries.put(tempkey, entry);

			}
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}

	}

	/* Change entry of the member */
	private static void updateEntry(int offset, String key, byte[] values) {
		byte[] t;
		t = entries.get(key);
		for (int i = 0; i < ENTRY_MEMBER_SIZE; i++) {
			if (i < values.length) {
				t[i + offset] = values[i];
			} else
				t[i + offset] = 0;

		}
	}

	/* calculate checksum */
	private static boolean checkCRC() {
		byte[] t;
		Checksum cs = new CRC32();
		for (String key : entries.keySet()) {
			t = entries.get(key);
			cs.update(t, 0, t.length);
		}
		checksum = cs.getValue();
		return previouschecksum == checksum;
	}
	
	/* Copy password to clipboard*/
	private static void copyPassword(String key) { 
		
		String password ;
		try {
			password = new String(trim(Arrays.copyOfRange(entries.get(key), PASSWORD_OFFSET, ENTRY_SIZE)), "UTF-8").toString();
			StringSelection stringSelection = new StringSelection(password);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
			//System.out.println("Password "+password);
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
	}

	/* Wait for USER_OFFSET input */
	static void command(String text) {

		String tempkey, key;
		int temp;
		byte[] newentry;
		byte[] t;

		frame.printMessage(">" + text + "\n");

		String[] input = (text).split(" ");

		switch (input[0]) {

		case "print":
			if (input.length == 2) {
				if (input[1].equals("all"))
					commandPrintAll();
				else {
					int entrynumber = Integer.parseInt(input[1]);
					commandPrintEntry(entrynumber);
				}
			}
			break;

		case "ctc":
			if (input.length == 2) {
				temp = Integer.parseInt(input[1]);
				key = new Vector<String>(entries.keySet()).get(temp);
				copyPassword(key);
				frame.printMessage("# Password copied to Clipboard\n");
			}
			break;
			

		case "delete":
			if (input.length == 2) {
				temp = Integer.parseInt(input[1]);
				key = new Vector<String>(entries.keySet()).get(temp);
				entries.remove(key);
				frame.printMessage("# Entry deleted\n");
			}
			break;

		case "change":
			if (input.length == 4) {
				temp = Integer.parseInt(input[1]);
				if (temp < entries.size()) {
					key = new Vector<String>(entries.keySet()).get(temp);

					if (input[2].equals("site")) {

						updateEntry(SITE_OFFSET, key, input[3].getBytes());
					} else if (input[2].equals("email")) {

						updateEntry(EMAIL_OFFSET, key, input[3].getBytes());
					} else if (input[2].equals("user")) {

						updateEntry(USER_OFFSET, key, input[3].getBytes());
					} else if (input[2].equals("password")) {

						updateEntry(PASSWORD_OFFSET, key, input[3].getBytes());
					}

					frame.printMessage("# Changes applied\n");
				}
			}
			break;

		case "add":
			if (input.length == 5) {
				newentry = new byte[ENTRY_SIZE];

				t = input[1].getBytes();
				System.arraycopy(t, 0, newentry, SITE_OFFSET, t.length % ENTRY_MEMBER_SIZE);

				t = input[2].getBytes();
				System.arraycopy(t, 0, newentry, EMAIL_OFFSET, t.length % ENTRY_MEMBER_SIZE);

				t = input[3].getBytes();
				System.arraycopy(t, 0, newentry, USER_OFFSET, t.length % ENTRY_MEMBER_SIZE);

				t = input[4].getBytes();

				System.arraycopy(t, 0, newentry, PASSWORD_OFFSET, t.length % ENTRY_MEMBER_SIZE);
				/*
				 * byte[] test = encrypt(newentry); test = decrypt(test); for (int i = 0; i <
				 * 128; i++) frame.printMessage((char) test[i]);
				 */

				// get the key
				try {
					tempkey = new String(Arrays.copyOfRange(newentry, 0, EMAIL_OFFSET), "UTF-8");
					System.out.println(entries.size());
					key = tempkey;
					int idx=0;
					while(entries.containsKey(tempkey)) {
						tempkey=key+"("+(++idx)+")";
					}
					entries.put(tempkey, newentry);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				frame.printMessage("# New entry has been added to the list\n");
			}
			break;
		case "save":
			frame.printMessage("# Saving...\n");
			writeToFile();
			break;

		case "exit":
			frame.printMessage("# Exiting\n");
			System.exit(0);
			break;
		case "reload":
			loadData();
			break;
		default:
			frame.printMessage("# Unknown Command\n");
			break;
		}

	}

	// trim the strings, remove zero bytes
	static byte[] trim(byte[] bytes) {
		int i = bytes.length - 1;
		while (i >= 0 && bytes[i] == 0) {
			--i;
		}

		return Arrays.copyOf(bytes, i + 1);
	}

	// format string
	private static String formatEntries(byte[] entry1, byte[] entry2, byte[] entry3, byte[] entry4) {
		String fill1 = "";
		String fill2 = "";
		String fill3 = "";
		String out;
		int size = 100;

		byte[] trimmed_entry1 = trim(entry1);
		byte[] trimmed_entry2 = trim(entry2);
		byte[] trimmed_entry3 = trim(entry3);
		byte[] trimmed_entry4 = trim(entry4);

		int fill_num1 = (size - trimmed_entry1.length) % size;
		int fill_num2 = (size - trimmed_entry2.length) % size;
		int fill_num3 = (size - trimmed_entry3.length) % size;

		// int fill_num4= (size - entry4.length())%size;
		for (int i = 0; i < fill_num1; i++) {
			fill1 = fill1 + " ";
		}
		for (int i = 0; i < fill_num2; i++) {
			fill2 = fill2 + " ";
		}
		for (int i = 0; i < fill_num3; i++) {
			fill3 = fill3 + " ";
		}

		// out = new String(trimmed_entry1) + fill1 + new String(trimmed_entry2) +
		// fill2;
		// System.out.println(trimmed_entry1.length+fill_num1);
		// + new String(trimmed_entry3) + fill3 + new String(trimmed_entry4);
		out = String.format("%-34s\t%-34s\t%-34s\t%-34s", new String(trimmed_entry1), new String(trimmed_entry2),
				new String(trimmed_entry3), new String(trimmed_entry4));
		return out;
	}

	/* print All Entries */
	private static void commandPrintAll() {
		frame.printMessage(separator);
		int j = 0;
		// frame.printMessage(String.format("\t%-34s\t%-34s\t%-34s\t%-34s\n",
		// "SITE","EMAIL", "USER", "PASSWORD") + "\n");
		frame.printMessage(String.format("\t%-34s\n", "SITE") + "\n");
		for (String key : entries.keySet()) {

			
			frame.printMessage(j++ + ")\t");

			frame.printMessage(new String(trim(Arrays.copyOfRange(entries.get(key), SITE_OFFSET, EMAIL_OFFSET)))
					// ,Arrays.copyOfRange(entry, EMAIL_OFFSET, USER_OFFSET),
					// Arrays.copyOfRange(entry, USER_OFFSET, PASSWORD_OFFSET),
					// Arrays.copyOfRange(entry, PASSWORD_OFFSET, ENTRY_SIZE))
					+ "\n");

		}
		frame.printMessage(separator);
	}

	/* Print Entry by Number */
	private static void commandPrintEntry(int entrynumber) {
		String key;
		if (entrynumber < entries.size()) {

			frame.printMessage(separator);
			key = new Vector<String>(entries.keySet()).get(entrynumber);
			byte[] entry = entries.get(key);
			frame.printMessage(entrynumber + ")");

			try {
				frame.printMessage(
						"\t" + new String(Arrays.copyOfRange(entry, SITE_OFFSET, EMAIL_OFFSET), "UTF-8").toString());

				frame.printMessage(
						"\t" + new String(Arrays.copyOfRange(entry, EMAIL_OFFSET, USER_OFFSET), "UTF-8").toString());

				frame.printMessage(
						"\t" + new String(Arrays.copyOfRange(entry, USER_OFFSET, PASSWORD_OFFSET), "UTF-8").toString());

				frame.printMessage(
						"\t" + new String(Arrays.copyOfRange(entry, PASSWORD_OFFSET, ENTRY_SIZE), "UTF-8").toString()
								+ "\n");
				frame.printMessage(separator);

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	/* Flush the change Info to File */
	private static void writeToFile() {
		String backup = filename + ".bck";
		byte[] t;
		byte[] header = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		long checksum = 0;

		try {
			// rename the file
			frame.printMessage("Working Directory = " + System.getProperty("user.dir") + "\n");

			File f = new File(filename);
			f.renameTo(new File(backup));

			/* Calculate checksum */
			Checksum cs = new CRC32();

			for (Map.Entry<String, byte[]> keys : entries.entrySet()) {
				t = keys.getValue();

				cs.update(t, 0, t.length);
			}

			checksum = cs.getValue();

			frame.printMessage("# Checksum is " + Long.toHexString(checksum) + "\n");
			// set the header
			header[NUM_OF_ENTRIES_OFFSET] = (byte) entries.size();
			header[CRC_OFFSET] = (byte) (checksum >> 56);
			header[CRC_OFFSET + 1] = (byte) (checksum >> 48);
			header[CRC_OFFSET + 2] = (byte) (checksum >> 40);
			header[CRC_OFFSET + 3] = (byte) (checksum >> 32);
			header[CRC_OFFSET + 4] = (byte) (checksum >> 24);
			header[CRC_OFFSET + 5] = (byte) (checksum >> 16);
			header[CRC_OFFSET + 6] = (byte) (checksum >> 8);
			header[CRC_OFFSET + 7] = (byte) (checksum);

			// write header
			Files.createFile(Paths.get(filename));
			Files.write(Paths.get(filename), header, StandardOpenOption.APPEND);

			for (String key : entries.keySet()) {

				t = encrypt(entries.get(key));

				// byte[] test ; frame.printMessage("len "+t.length);
				// test = decrypt(t);
				// for (int i = 0; i < 128; i++)
				// frame.printMessage((char) test[i]);
				Files.write(Paths.get(filename), t, StandardOpenOption.APPEND);
			}
			// delete the old file
			Files.delete(Paths.get(backup));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* Encrypt bytes */
	private static byte[] encrypt(byte[] bytes_to_encrypt) {
		try {
			IvParameterSpec iv = new IvParameterSpec(iv_password);

			SecretKeySpec skeySpec = new SecretKeySpec(password, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			byte[] encrypted = cipher.doFinal(bytes_to_encrypt);

			return encrypted;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/* Decrypt bytes */
	private static byte[] decrypt(byte[] encrypted) {
		byte[] original;
		try {
			IvParameterSpec iv = new IvParameterSpec(iv_password);

			SecretKeySpec skeySpec = new SecretKeySpec(password, "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			original = cipher.doFinal(encrypted);

			return original;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
