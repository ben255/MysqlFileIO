import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;


public class MysqlFileIO extends JFrame{
	JMenuBar menuBar;
	JMenu menu;
	JMenuItem uploadItem, loadItem;
	UploadFile uFile;
	LoadFile lFile;
	Container content;
	public MysqlFileIO(){
		this.setVisible(true);
		this.setSize(700,700);		
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
		menuBar = new JMenuBar();
		menu = new JMenu("Menu");
		uploadItem = new JMenuItem("Upload Image");
		loadItem = new JMenuItem("Load Image");
		content = this.getContentPane();
		content.setVisible(true);
		uploadItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(content.getComponentCount() == 2)
					content.remove(1);
				content.add(new UploadFile());
				content.validate();
			}
		});
		loadItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(content.getComponentCount() == 2)
					content.remove(1);
				content.add(new LoadFile());
				content.validate();
			}
		});
		menuBar.add(menu);
		menu.add(uploadItem);
		menu.add(loadItem);
		this.add(menuBar, "North");
	}
	JFrame la(){
		return this;
	}
	public static void main(String[] args){
		MysqlFileIO con = new MysqlFileIO();
	}
	public class UploadFile extends JPanel{
		JButton uploadBtn;
		JLabel fileSize, fileName;
		JTextField description;
		JFileChooser fileChooser;
		File file;

		//SERVER ***********************************************
		String server, database, user, pass;
		MysqlDataSource ds;
		Connection con;
		public UploadFile(){
			this.setVisible(true);
			uploadBtn = new JButton("Upload");
			fileSize = new JLabel("Size");
			fileName = new JLabel("Name");
			description = new JTextField("Enter description");
			fileChooser = new JFileChooser();
			
			uploadBtn.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					setConnection("10.0.0.14", "file_storage", "client", "0411");
					try {
			            String sql = "INSERT INTO webapp_storage (description, data, filename, filesize, filetype) values (?, ?, ?, ?, ?)";
			            PreparedStatement statement = (PreparedStatement) con.prepareStatement(sql);
			            statement.setString(1, description.getText());
			            InputStream inputStream = new FileInputStream(file.getAbsoluteFile());
			            statement.setBlob(2, inputStream);
			            statement.setString(3, file.getName());
			            statement.setString(4, (int)file.length()+" Bytes");
			            String extension = "";
						int i = file.getName().lastIndexOf('.');
						if(i>0)
							extension = file.getName().substring(i+1);
						statement.setString(5, extension);
			 
			            int row = statement.executeUpdate();
			            if (row > 0) {
			                System.out.println("Uploaded");
			            }
			            closeConnection();
			        } catch (SQLException ex) {
			            ex.printStackTrace();
			        } catch (IOException ex) {
			            ex.printStackTrace();
			        }
				}
				
			});
			fileChooser.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					file = fileChooser.getSelectedFile();
					fileName.setText(file.getName());
					fileSize.setText(file.length()+" Bytes");
				}
			});				
			
			uploadBtn.setBounds(0,0,50,30);
			fileName.setBounds(50, 0, 30, 30);
			fileSize.setBounds(80,0,30,30);
			fileChooser.setBounds(0, 50, 100, 100);
			this.add(description);
			this.add(uploadBtn);
			this.add(fileName);
			this.add(fileSize);
			this.add(fileChooser);
		}
		void setConnection(String server, String database, String user, String pass){
			this.server = server;
			this.database = database;
			this.user = user;
			this.pass = pass;
			ds = new MysqlDataSource();
			ds.setServerName(server);
			ds.setDatabaseName(database);
			try{
				con = (Connection) ds.getConnection(user, pass);
				System.out.println("Online");
			}catch(Exception e){
				System.out.println("Error: "+e.toString());
			}
		}
		void closeConnection(){
			try {
				con.close();
				System.out.println("Connection Closed");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public class LoadFile extends JPanel{
		JButton loadFile;
		JButton showFile;
		JList<String> myList;
		JScrollPane scrollPane;
		String server, database, user, pass;
		Connection con;
		MysqlDataSource ds;
		ResultSet resultSet;
		ArrayList<byte[]> blobData = new ArrayList<byte[]>();
		Image image;
		JLabel imageLabel;
		LoadFile(){
			this.setVisible(true);
			loadFile = new JButton("Load");
			showFile = new JButton("ShowImage");
			myList = new JList<String>();
			scrollPane = new JScrollPane(myList);
			imageLabel = new JLabel();
			this.setLayout(new BorderLayout());
			
			loadFile.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					setConnection("10.0.0.14", "file_storage", "client", "0411");
					try {
			            String sql = "SELECT id, data, description, filename, filesize, filetype FROM webapp_storage";
			            PreparedStatement statement = (PreparedStatement) con.prepareStatement(sql);
			            resultSet = statement.executeQuery();
			            populateList(resultSet);
			            closeConnection();
			        } catch (SQLException ex) {
			            ex.printStackTrace();
			        }
				}
			});
			myList.addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent arg0) {
					int index = myList.locationToIndex(arg0.getPoint());
					byte[] data = blobData.get(index);
					image = getToolkit().createImage(data);
					setImage(new ImageIcon(image));
				}

				@Override
				public void mouseEntered(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mousePressed(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
			loadFile.setBounds(0, 0, 0, 0);
			this.add(scrollPane,"North");
			this.add(loadFile,"West");
			this.add(imageLabel, "Center");
		}
		void setImage(ImageIcon icon){
			imageLabel.setIcon(icon);
		}
		void populateList(ResultSet rs){
			 try {
				 ArrayList<String> data = new ArrayList<String>();
				while (rs.next()) {
				    data.add("ID: "+rs.getInt("id")+
				    		" FileName: "+ rs.getString("filename"));
				    byte[] blobByte = rs.getBytes("data");
				    blobData.add(blobByte);
				}
				String[] dataString = new String[data.size()];
				for(int x = 0; x < data.size(); x++)
					dataString[x] = data.get(x);
				myList.setListData(dataString);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		void setConnection(String server, String database, String user, String pass){
			this.server = server;
			this.database = database;
			this.user = user;
			this.pass = pass;
			ds = new MysqlDataSource();
			ds.setServerName(server);
			ds.setDatabaseName(database);
			try{
				con = (Connection) ds.getConnection(user, pass);
				System.out.println("Online");
			}catch(Exception e){
				System.out.println("Error: "+e.toString());
			}
		}
		void closeConnection(){
			try {
				con.close();
				System.out.println("Connection Closed");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
