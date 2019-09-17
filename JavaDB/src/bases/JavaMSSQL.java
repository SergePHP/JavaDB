package bases;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.*;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;


public class JavaMSSQL {

	private JFrame frame;
	private JTable parentTable;
	private JTable childTable;
	private JLabel statusLabel;
	private Connection con = null;       
	private Statement stmt = null;          
	private ResultSet rs = null;
	private JPanel statusBarPanel;
	/*
	 * ссылка для соединения с базой данных MSSQL,
	 * логин и пароль
	 */
	private String connectionUrl = "jdbc:sqlserver://srv-plm-01:1433";
    private String login = "infodba";
    private String password = "infodba";
    /*
     * Заголовки таблиц
     */
	private String[] parentTableHeader = {"ID Категории", "Наименование категории"};
	private String[] childTableHeader = {"ID Книги", "Наименование книги", "Автор"};
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JavaMSSQL window = new JavaMSSQL();
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
	public JavaMSSQL() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 724, 864);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/*
		 * Пользовательский обработчик
		 * события закрытия окна
		 */
		frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
            	/*
            	 * Если соединение было установлено,
            	 * то освобождаются задействованные
            	 * ресурсы и закрывается соединение
            	 */
    		    statusLabel.setText("Закрываю соединение...");
    		    
				if (rs != null) 
					try { 
						rs.close(); 
					}
					catch(Exception e1) { 
						statusLabel.setText(e1.getMessage()); 
					}
			    if (stmt != null) 
			    	try { 
			    		stmt.close(); 
			    	}
			    	catch(Exception e1) {
			    		statusLabel.setText(e1.getMessage()); 
			    		}
			    if (con != null) 
			    	try { 
			    		con.close(); 
			    		}
			    	catch(Exception e1) { 
			    		statusLabel.setText(e1.getMessage()); 
			    		}
                e.getWindow().dispose();
            }
        });
		
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel upPanel = new JPanel();
		frame.getContentPane().add(upPanel, BorderLayout.NORTH);
		
		JButton btnConnect = new JButton("Подключиться");
		/*
		 * Обработчик события нажатия кнопки "Подключить"
		 */
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getParentValues();
			}
		});
		upPanel.add(btnConnect);
		
		JPanel tablesPanel = new JPanel();
		frame.getContentPane().add(tablesPanel, BorderLayout.CENTER);
		tablesPanel.setLayout(new GridLayout(2, 0, 0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		tablesPanel.add(scrollPane);
		/*
		 * Отключаю возможность редактирования 
		 * таблицы из интерфейса 
		 */
		DefaultTableModel parentTableModel = new DefaultTableModel(parentTableHeader, 0) {
		    public boolean isCellEditable(int row, int column) {
		        return false;
		    }
		};
		
		parentTable = new JTable(parentTableModel);
		parentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parentTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		parentTable.getColumnModel().getColumn(0).setMaxWidth(100);
		/*
		 * Обработчик события выбора ячейки в таблице
		 */
		parentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	getChildValues();
	        }
	    });
		
		scrollPane.setViewportView(parentTable);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		tablesPanel.add(scrollPane_1);
		/*
		 * Отключаю возможность редактирования 
		 * таблицы из интерфейса 
		 */
		DefaultTableModel childTableModel = new DefaultTableModel(childTableHeader, 0) {
		    public boolean isCellEditable(int row, int column) {
		        return false;
		    }
		}; 
		childTable = new JTable(childTableModel);
		childTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		childTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		childTable.getColumnModel().getColumn(0).setMaxWidth(100);
		scrollPane_1.setViewportView(childTable);
		/*
		 * Формирую строку состояния
		 */
		statusBarPanel = new JPanel();
		statusBarPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusBarPanel.setPreferredSize(new Dimension(frame.getWidth(), 24));
		statusBarPanel.setLayout(new BoxLayout(statusBarPanel, BoxLayout.X_AXIS));
		
		statusLabel = new JLabel("Готов");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusBarPanel.add(statusLabel);
		frame.getContentPane().add(statusBarPanel, BorderLayout.SOUTH);
	}
	/*
	 * Метод выполняет подключение к базе данных
	 */
	private void connectToDB() {
		
		statusLabel.setText("Выполняю подключение к базе... ");
		try {
			/*
			 * Пытаюсь подключиться к базе, 
			 * используя адрес, логин и пароль
			 */
			con = DriverManager.getConnection(connectionUrl, login, password);
			/*
			 * Создаю объект, для отправки SQL инструкций
			 * в базу данных
			 */
			stmt = con.createStatement();
			
			statusLabel.setText(statusLabel.getText() + "Готово");
		} catch (Exception e) {
			/*
			 * Сообщение исключения вывожу в строку состояния
			 */
			statusLabel.setText(e.getMessage());
		}
	}
	/*
	 * Метод получает все строки родительской таблицы
	 */
	private void getParentValues() {
		/*
		 * SQL инструкция для получения
		 * данных родительской таблицы
		 */
		String SQL = "SELECT * FROM javaDB.dbo.Category";
		/*
		 * Подключаюсь к базе
		 */
		connectToDB();
		/*
		 * Если подключение удалось, выполняю
		 * SQL инструкцию и заполняю таблицу данными
		 */
		if(con != null || stmt != null) {
			
			try {
				rs = stmt.executeQuery(SQL);

				DefaultTableModel tableModel = (DefaultTableModel) parentTable.getModel(); 
				tableModel.setRowCount(0);
				
			    while (rs.next()) {
			    	Vector<String> row = new Vector<>();
			    	row.add(Integer.valueOf(rs.getInt("category_id")).toString());
			    	row.add(rs.getString("category_name"));

			    	tableModel.addRow(row);
			    }
			} catch (Exception e) {
				/*
				 * Сообщение исключения вывожу в строку состояния
				 */
				statusLabel.setText(e.getMessage());
			} 
		}
	}
	/*
	 * Метод получает строки дочерней таблицы,
	 * связанные с родительской таблицей
	 */
	private void getChildValues() {
		/*
		 * Получаю значение первичного
		 * ключа из выбранной строки
		 */
		int idCat = Integer.valueOf(parentTable.getValueAt(parentTable.getSelectedRow(), 0).toString());
		/*
		 * Формирую SQL инструкцию для выборки строк из дочерней таблицы,
		 * в которой внешний ключ задан первичным ключом выбранной строки 
		 */
		String SQL = "SELECT book_id, book_name, author FROM javaDB.dbo.Books WHERE category_id='" + idCat + "'";
		/*
		 * На данном этапе подключение к базе уже установлено,
		 * выполняю SQL инструкцию и заполняю таблицу данными
		 */
		try {

			statusLabel.setText("Данные получены, ID категории: " + idCat);
			
			rs = stmt.executeQuery(SQL);
			
			DefaultTableModel tableModel = (DefaultTableModel) childTable.getModel(); 
			tableModel.setRowCount(0);
			
		    while (rs.next()) {
		    	Vector<String> row = new Vector<>();
		    	row.add(Integer.valueOf(rs.getInt("book_id")).toString());
		    	row.add(rs.getString("book_name"));
		    	row.add(rs.getString("author"));

		    	tableModel.addRow(row);
		    }
			
		} catch (SQLException e) {
			/*
			 * Сообщение исключения вывожу в строку состояния
			 */
			statusLabel.setText(e.getMessage());
		}
	}
}
