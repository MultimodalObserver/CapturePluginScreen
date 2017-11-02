package screencaptureplugin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mo.core.ui.GridBConstraints;
import mo.core.ui.Utils;
import mo.organization.ProjectOrganization;

public class ScreenCaptureConfigurationDialog extends JDialog implements DocumentListener {

    JLabel errorLabel;
    JTextField nameField;
    JButton accept;
    JComboBox cbFPS;
    JComboBox cbDIM;
    JComboBox cbPantalla;
    public int fps_option;
    public int dim_option;
    public int pantalla_option;
    ProjectOrganization org;    

    boolean accepted = false;

    public ScreenCaptureConfigurationDialog() {
        super(null, "Screen Capture Configuration", Dialog.ModalityType.APPLICATION_MODAL);
    }

    public ScreenCaptureConfigurationDialog(ProjectOrganization organization) {
        super(null, "Screen Capture Configuration", Dialog.ModalityType.APPLICATION_MODAL);
        org = organization;
    }

    public boolean showDialog() {

        setLayout(new GridBagLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                accepted = false;
                super.windowClosing(e);
            }
        });

        setLayout(new GridBagLayout());
        GridBConstraints gbc = new GridBConstraints();

        JLabel label = new JLabel("Configuration name: ");
        JLabel fps = new JLabel("FPS:");
        String[] frames = {"15","30","45","60"};
        JLabel dim = new JLabel("Dimension:");
        String[] dimensiones = {"800x600","1024x768","1280x720","1366x768"};
        JLabel screen = new JLabel("Screen:");
        int count=0;
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            count++;
        }
        String[] pantallas;
        if(count>1){
            pantallas = new String[count+1];
            pantallas[0] = "Extended";
            count=1;
        }
        else{ 
            pantallas = new String[count];           
            count=0;
        }
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            pantallas[count]=gd.getIDstring();
            count++;
        } 
        cbFPS = new JComboBox(frames);
        cbDIM = new JComboBox(dimensiones);
        cbPantalla = new JComboBox(pantallas);
        cbFPS.setSelectedIndex(3);
        cbDIM.setSelectedIndex(3);
        nameField = new JTextField();
        nameField.getDocument().addDocumentListener(this);

        gbc.gx(0).gy(0).f(GridBConstraints.HORIZONTAL).a(GridBConstraints.FIRST_LINE_START).i(new Insets(5, 5, 5, 5));
        add(label, gbc);
        add(nameField, gbc.gx(2).wx(1).gw(3));
        add(fps,gbc.gx(0).gy(2));
        add(cbFPS,gbc.gx(2).gy(2).wx(1).gw(3));
        add(dim,gbc.gx(0).gy(4));
        add(cbDIM,gbc.gx(2).gy(4).wx(1).gw(3));
        add(screen,gbc.gx(0).gy(6));
        add(cbPantalla,gbc.gx(2).gy(6).wx(1).gw(3));
              

        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.red);
        add(errorLabel, gbc.gx(0).gy(7).gw(5).a(GridBConstraints.LAST_LINE_START).wy(1));

        accept = new JButton("Accept");
        
        accept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accepted = true;
                fps_option=cbFPS.getSelectedIndex(); //0==15; 1 == 30; 2== 45; 3==60
                dim_option=cbDIM.getSelectedIndex();
                pantalla_option=cbPantalla.getSelectedIndex();
                setVisible(false);
                dispose();
            }
        });

        gbc.gx(0).gy(8).a(GridBConstraints.LAST_LINE_END).gw(3).wy(1).f(GridBConstraints.NONE);
        add(accept, gbc);

        setMinimumSize(new Dimension(400, 150));
        setPreferredSize(new Dimension(400, 300));
        pack();
        Utils.centerOnScreen(this);
        updateState();
        setVisible(true);

        return accepted;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateState();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateState();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateState();
    }

    private void updateState() {
        if (nameField.getText().isEmpty()) {
            errorLabel.setText("A name for this configuration must be specified");
            accept.setEnabled(false);
        } else {
            errorLabel.setText("");
            accept.setEnabled(true);
        }
    }

    public String getConfigurationName() {
        return nameField.getText();
    }
}

