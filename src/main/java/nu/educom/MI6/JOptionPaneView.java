package nu.educom.MI6;

import javax.swing.*;
import java.awt.*;

public class JOptionPaneView implements IView {
  private final JFrame mainWindow;
  private final JTextField serviceNumberField;
  private final JPasswordField secretCodeField;
  private final JButton submit;
  private IPresenter presenter;

  public JOptionPaneView() {

    serviceNumberField = new JTextField(20);
    secretCodeField = new JPasswordField(20);

    JLabel lblServiceNumber = new JLabel("Enter your service number");
    JLabel lblSecretCode = new JLabel("Enter your secret code");

    submit = new JButton("Log in");

    JOptionPane pane = new JOptionPane("test");

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    mainWindow = new JFrame("Secret Service Login");
    mainWindow.setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    mainWindow.add(lblServiceNumber, gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 5;
    gbc.weightx = 1;
    mainWindow.add(serviceNumberField, gbc);

    gbc.gridy = 1;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    mainWindow.add(lblSecretCode, gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 5;
    mainWindow.add(secretCodeField, gbc);

    gbc.gridy = 2;
    gbc.gridwidth = 5;
    mainWindow.add(submit, gbc);

    gbc.gridy = 3;
    gbc.gridwidth = 5;
    pane.setVisible(false);
    mainWindow.add(pane, gbc);

    mainWindow.pack();
    mainWindow.setVisible(true);
    mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  }

  @Override
  public void showMessage(String msg) {
    JOptionPane.showMessageDialog(mainWindow, msg);
  }

  @Override
  public void triggerAskLogIn() {
    submit.addActionListener(e -> presenter.handleLogin());
  }

  @Override
  public String getServiceNumber() {
    return serviceNumberField.getText();
  }

  @Override
  public void triggerAskSecretCode() {
    presenter.handleSecretCode();
  }

  @Override
  public String getSecretCode() {
    return String.valueOf(secretCodeField.getPassword());

  }
  @Override
  public void addPresenterListener(IPresenter p) {
    this.presenter = p;
  }

  @Override
  public void close() {
    mainWindow.dispose();
    System.exit(0);
  }
}


