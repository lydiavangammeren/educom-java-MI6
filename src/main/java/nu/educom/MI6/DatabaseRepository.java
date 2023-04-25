package nu.educom.MI6;

import org.hibernate.Session;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class DatabaseRepository {
  /**
   * Get database connection
   *
   * @return a Connection object
   */
  private Session session = HibernateUtil.openSession();

  public Connection connectWithDatabase() throws SQLException {

    Connection conn = null;

    try (FileInputStream f = new FileInputStream("C:\\Users\\Lydia van Gammeren\\IdeaProjects\\educom-java-MI6\\src\\main\\java\\nu\\educom\\MI6\\db.properties")) {

      // load the properties file
      Properties pros = new Properties();
      pros.load(f);

      // assign db parameters
      String url = pros.getProperty("url");

      // create a connection to the database
      conn = DriverManager.getConnection(url, pros);
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    return conn;
  }

  public Agent getAgentByServiceNumber(String serviceNr) {
    Agent agent = this.session.createQuery("from Agent WHERE service_number = :serviceNr", Agent.class)
      .setParameter("serviceNr", serviceNr).uniqueResultOptional().orElse(null);
    return agent;
  }

  Agent readAgentByServiceNr(String serviceNumber) {
    String sql = "SELECT * FROM agents WHERE `service_number`=?";

    try {
      Connection conn = connectWithDatabase();
      PreparedStatement preparedStmt = conn.prepareStatement(sql);
      preparedStmt.setString(1, serviceNumber);
      ResultSet rs = preparedStmt.executeQuery();
      boolean exists = rs.next();
      if (exists) {
        int id = rs.getInt("id");
        String secretCode = rs.getString("secret_code");
        boolean active = rs.getBoolean("active");
        boolean licenseToKill = rs.getBoolean("license_to_kill");
        LocalDate date = rs.getDate("license_valid_until").toLocalDate();
        return new Agent(id, serviceNumber, secretCode, active, licenseToKill, date);
      }
      conn.close();
      rs.close();
      preparedStmt.close();

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    return null;
  }

  public Agent readAgentByServiceNumAndSecretCode(String serviceNumber, String secret) {
    String sql = "SELECT * FROM agents WHERE `service_number`=? AND `secret_code`=?";

    try {
      Connection conn = connectWithDatabase();
      PreparedStatement preparedStmt = conn.prepareStatement(sql);
      preparedStmt.setString(1, serviceNumber);
      preparedStmt.setString(2, secret);
      ResultSet rs = preparedStmt.executeQuery();
      if (rs.next()) {
        int id = rs.getInt("id");
        String serviceNum = rs.getString("service_number");
        String secretCode = rs.getString("secret_code");
        boolean active = rs.getBoolean("active");
        boolean licenseToKill = rs.getBoolean("license_to_kill");
        LocalDate date = rs.getDate("license_valid_until").toLocalDate();
        return new Agent(id, serviceNum, secretCode, active, licenseToKill, date);
      }
      rs.close();
      preparedStmt.close();

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    return null;
  }

  public List<LoginAttempt> readLastFailedLoginAttempts(int agentId) {
    List<LoginAttempt> failedLoginAttempts = new ArrayList<>();

    // Find the latest successful login attempt
    String subquery = "SELECT MAX(login_time) FROM login_attempts WHERE agent_id = ? AND successful_attempt = true";
    // Combine with main query of finding the id of said login-attempt
    String query = String.format("SELECT attempt_id as 'max_id' FROM login_attempts WHERE agent_id = ? AND login_time = (%s)", subquery);

    try {
      Connection conn = connectWithDatabase();
      PreparedStatement preparedStmt = conn.prepareStatement(query);
      preparedStmt.setInt(1, agentId);
      preparedStmt.setInt(2, agentId);
      ResultSet rs = preparedStmt.executeQuery();

      // If there is no previous successful login, set value to 0
      int lastSuccessId;
      try {
        rs.next();
        lastSuccessId = rs.getInt("max_id");
      } catch (SQLException e) {
        lastSuccessId = 0;
      }

      query = "SELECT * FROM login_attempts WHERE agent_id = ?";
      if (lastSuccessId > 0) // if there has been a successful login before
      {
        query = String.format("SELECT * FROM login_attempts WHERE agent_id = ? AND attempt_id > %s", lastSuccessId);
      }

      preparedStmt = conn.prepareStatement(query);
      preparedStmt.setInt(1, agentId);
      rs = preparedStmt.executeQuery();

      while (rs.next()) {
        failedLoginAttempts.add(new LoginAttempt(rs.getInt("attempt_id"), rs.getInt("agent_id"), rs.getTimestamp("login_time").toLocalDateTime(), rs.getBoolean("successful_attempt")));
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());

    }

    return failedLoginAttempts;
  }

  public void createLoginAttempt(LoginAttempt attempt) {
    String sql = "INSERT INTO login_attempts(`agent_id`, login_time, successful_attempt) VALUES (?, ?, ?)";

    try {
      Connection conn = connectWithDatabase();
      PreparedStatement preparedStmt = conn.prepareStatement(sql);
      preparedStmt.setInt(1, attempt.getAgentId());
      preparedStmt.setTimestamp(2, Timestamp.valueOf(attempt.getLoginTime()));
      preparedStmt.setBoolean(3, attempt.isSuccessfulAttempt());
      preparedStmt.executeUpdate();

      preparedStmt.close();

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
