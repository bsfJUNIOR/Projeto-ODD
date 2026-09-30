package telas;

import entidades.NivelAcesso;
import entidades.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import servicos.UsuarioService;
import sessao.SessaoUsuario;

/** Tela de administração de usuários; o acesso é validado pelo MenuPrincipal. */
public class GerenciamentoUsuarios extends JFrame {

    private final UsuarioService usuarioService = new UsuarioService();
    private final JTextField campoUsuario = new JTextField(20);
    private final JPasswordField campoSenha = new JPasswordField(20);
    private final JComboBox<NivelAcesso> campoNivel = new JComboBox<NivelAcesso>(NivelAcesso.values());
    private final JTable tabela = new JTable();
    private List<Usuario> usuarios;
    private Usuario selecionado;

    public GerenciamentoUsuarios() {
        super("ODD - Gerenciamento de Usuários");
        if (!SessaoUsuario.ehAdministrador()) {
            throw new SecurityException("Acesso permitido somente para administradores.");
        }
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        montarTela();
        carregarTabela();
        pack();
        setLocationRelativeTo(null);
    }

    private void montarTela() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; formulario.add(new JLabel("Usuário:*"), c);
        c.gridx = 1; formulario.add(campoUsuario, c);
        c.gridx = 0; c.gridy = 1; formulario.add(new JLabel("Senha:*"), c);
        c.gridx = 1; formulario.add(campoSenha, c);
        c.gridx = 0; c.gridy = 2; formulario.add(new JLabel("Nível:*"), c);
        c.gridx = 1; formulario.add(campoNivel, c);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> selecionar());

        JButton novo = new JButton("Novo"); novo.addActionListener(e -> limparFormulario());
        JButton salvar = new JButton("Salvar"); salvar.addActionListener(e -> salvar());
        JButton excluir = new JButton("Excluir"); excluir.addActionListener(e -> excluir());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(novo); botoes.add(salvar); botoes.add(excluir);

        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
    }

    private void carregarTabela() {
        usuarios = usuarioService.listar();
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Usuário", "Nível"}, 0) {
            @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        };
        for (Usuario usuario : usuarios) {
            modelo.addRow(new Object[]{usuario.getId(), usuario.getUsuario(), usuario.getNivelAcesso()});
        }
        tabela.setModel(modelo);
    }

    private void selecionar() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || usuarios == null || linha >= usuarios.size()) return;
        selecionado = usuarios.get(linha);
        campoUsuario.setText(selecionado.getUsuario());
        campoSenha.setText("");
        campoNivel.setSelectedItem(selecionado.getNivelAcesso());
    }

    private void salvar() {
        char[] senha = campoSenha.getPassword();
        try {
            Usuario usuario = selecionado == null ? new Usuario() : selecionado;
            usuario.setUsuario(campoUsuario.getText().trim());
            usuario.setNivelAcesso((NivelAcesso) campoNivel.getSelectedItem());
            usuarioService.salvar(usuario, senha);
            JOptionPane.showMessageDialog(this, "Usuário salvo com sucesso.");
            limparFormulario();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validação", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Não foi possível salvar o usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(senha, '\0');
        }
    }

    private void excluir() {
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para excluir.");
            return;
        }
        if (selecionado.getId().equals(SessaoUsuario.getId())) {
            JOptionPane.showMessageDialog(this, "Não é permitido excluir o usuário que está autenticado.");
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja excluir o usuário '" + selecionado.getUsuario() + "'?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) return;
        try {
            usuarioService.excluir(selecionado);
            JOptionPane.showMessageDialog(this, "Usuário excluído com sucesso.");
            limparFormulario();
            carregarTabela();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Não foi possível excluir o usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        selecionado = null;
        tabela.clearSelection();
        campoUsuario.setText("");
        campoSenha.setText("");
        campoNivel.setSelectedItem(NivelAcesso.USUARIO);
        campoUsuario.requestFocusInWindow();
    }
}
