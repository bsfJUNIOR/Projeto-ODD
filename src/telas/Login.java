package telas;

import entidades.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import servicos.UsuarioService;
import servicos.HistoricoService;
import sessao.SessaoUsuario;

public class Login extends JFrame {

    private final JTextField campoUsuario = new JTextField(20);
    private final JPasswordField campoSenha = new JPasswordField(20);
    private final UsuarioService usuarioService = new UsuarioService();

    public Login() {
        super("ODD - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        montarTela();
        pack();
        setLocationRelativeTo(null);
    }

    private void montarTela() {
        JPanel campos = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0;
        campos.add(new JLabel("Usuário:"), c);
        c.gridx = 1;
        campos.add(campoUsuario, c);
        c.gridx = 0; c.gridy = 1;
        campos.add(new JLabel("Senha:"), c);
        c.gridx = 1;
        campos.add(campoSenha, c);

        JButton entrar = new JButton("Entrar");
        entrar.addActionListener(e -> entrar());
        JButton sair = new JButton("Sair");
        sair.addActionListener(e -> System.exit(0));
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(entrar);
        botoes.add(sair);

        add(new JLabel("Organizador de Despesas - ODD", JLabel.CENTER), BorderLayout.NORTH);
        add(campos, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(entrar);
    }

    private void entrar() {
        char[] senha = campoSenha.getPassword();
        try {
            Usuario usuario = usuarioService.autenticar(campoUsuario.getText(), senha);
            if (usuario == null) {
                JOptionPane.showMessageDialog(this, "Usuário ou senha inválidos.", "Login", JOptionPane.WARNING_MESSAGE);
                campoSenha.setText("");
                return;
            }
            SessaoUsuario.iniciar(usuario);
            new HistoricoService().registrar("LOGIN", "realizou login.");
            dispose();
            new MenuPrincipal().setVisible(true);
        } catch (RuntimeException e) {
            SessaoUsuario.encerrar();
            JOptionPane.showMessageDialog(this, "Não foi possível realizar o login. Verifique a conexão com o banco.", "Login", JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(senha, '\0');
        }
    }
}
