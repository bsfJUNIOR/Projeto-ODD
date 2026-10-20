/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package telas;

import entidades.NivelAcesso;
import entidades.Usuario;
import java.awt.Cursor;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import servicos.AutorizacaoService;
import servicos.UsuarioService;
import sessao.SessaoUsuario;

/**
 *
 * @author Usuario
 */
public class GerenciadorUsuario extends javax.swing.JFrame {

    private final UsuarioService usuarioService = new UsuarioService();
    private List<Usuario> usuarios;
    private Usuario selecionado;

    public GerenciadorUsuario() {
        AutorizacaoService.exigirAdministrador();
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        configurarTela();
        carregarTabela();
        setLocationRelativeTo(null);
    }

    private void configurarTela() {
        tbAdmUsuario.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbAdmUsuario.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionar();
            }
        });
        btAdmNovo.addActionListener(e -> limparFormulario());
        USUARIO.addActionListener(e -> salvar());
        jButton1.addActionListener(e -> excluir());
        btAdmVoltar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
            }
        });
        jLabel2.setToolTipText("Clique para mostrar ou ocultar a senha");
        jLabel2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                alternarSenha();
            }
        });
        limparFormulario();
    }

    private void alternarSenha() {
        jTextField2.setEchoChar(jTextField2.getEchoChar() == (char) 0 ? '\u2022' : (char) 0);
    }

    private void carregarTabela() {
        usuarios = usuarioService.listar();
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"ID", "Usuário", "Nível"}, 0) {
            @Override
            public boolean isCellEditable(int l, int c) {
                return false;
            }
        };
        for (Usuario usuario : usuarios) {
            modelo.addRow(new Object[]{usuario.getId(), usuario.getUsuario(), usuario.getNivelAcesso()});
        }
        tbAdmUsuario.setModel(modelo);
    }

    private void selecionar() {
        int linha = tbAdmUsuario.getSelectedRow();
        if (linha < 0 || usuarios == null || linha >= usuarios.size()) {
            return;
        }
        selecionado = usuarios.get(linha);
        jTextField1.setText(selecionado.getUsuario());
        jTextField2.setText("");
        cbxAdmUsuario.setSelectedItem(selecionado.getNivelAcesso().name());
    }

    private void salvar() {
        char[] senha = jTextField2.getPassword();
        try {
            Usuario usuario = selecionado == null ? new Usuario() : selecionado;
            usuario.setUsuario(jTextField1.getText().trim());
            String nivel = (String) cbxAdmUsuario.getSelectedItem();
            usuario.setNivelAcesso("ADMIN".equals(nivel) ? NivelAcesso.ADMIN : "USUARIO".equals(nivel) ? NivelAcesso.USUARIO : null);
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
            JOptionPane.showMessageDialog(this, "Não é permitido excluir o usuário autenticado.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja excluir o usuário '" + selecionado.getUsuario() + "'?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
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
        tbAdmUsuario.clearSelection();
        jTextField1.setText("");
        jTextField2.setText("");
        cbxAdmUsuario.setSelectedItem("USUARIO");
        jTextField1.requestFocusInWindow();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        USUARIO = new javax.swing.JButton();
        btAdmNovo = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JPasswordField();
        cbxAdmUsuario = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbAdmUsuario = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        btAdmVoltar = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText("Usuario:");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Senha:");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel3.setText("Nível:");

        jButton1.setText("Excluir");

        USUARIO.setText("Salvar");

        btAdmNovo.setText("Novo");

        cbxAdmUsuario.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        cbxAdmUsuario.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecionar", "ADMIN", "USUARIO" }));
        cbxAdmUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxAdmUsuarioActionPerformed(evt);
            }
        });

        tbAdmUsuario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Usuário", "Nível"
            }
        ));
        jScrollPane1.setViewportView(tbAdmUsuario);

        btAdmVoltar.setText("Voltar");
        jMenuBar1.add(btAdmVoltar);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(138, 138, 138)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbxAdmUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jTextField2))))
                .addGap(305, 305, 305))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btAdmNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(USUARIO, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(cbxAdmUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(USUARIO, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btAdmNovo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbxAdmUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxAdmUsuarioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxAdmUsuarioActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GerenciadorUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GerenciadorUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GerenciadorUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GerenciadorUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new GerenciadorUsuario().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton USUARIO;
    private javax.swing.JButton btAdmNovo;
    private javax.swing.JMenu btAdmVoltar;
    private javax.swing.JComboBox<String> cbxAdmUsuario;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPasswordField jTextField2;
    private javax.swing.JTable tbAdmUsuario;
    // End of variables declaration//GEN-END:variables
}
