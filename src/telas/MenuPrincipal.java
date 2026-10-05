/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package telas;

import entidades.Despesa;
import entidades.Receita;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import servicos.DespesaService;
import servicos.HistoricoService;
import servicos.ReceitaService;
import servicos.FinanceiroService;
import model.ResumoFinanceiro;
import model.StatusDespesa;
import sessao.SessaoUsuario;
/**
 *
 * @author Usuario
 */
public class MenuPrincipal extends javax.swing.JFrame {
    private javax.swing.JTextField campoPesquisa;

    /**
     * Creates new form MenuPrincipal
     */
    public MenuPrincipal (){
        if (!SessaoUsuario.estaAutenticado()) {
            throw new IllegalStateException("É necessário realizar login para abrir o menu principal.");
        }
        initComponents();
        configurarPesquisa();
        configurarMenuUsuario();
        setLocationRelativeTo(null);
        
        carregarResumoFinanceiro();
        carregarTabelaVencimentos();
    }

    private void configurarMenuUsuario() {
        javax.swing.JMenu menuUsuario = new javax.swing.JMenu("Usuário: " + SessaoUsuario.getUsuario()
                + " (" + SessaoUsuario.getNivelAcesso() + ")");
        if (SessaoUsuario.ehAdministrador()) {
            javax.swing.JMenuItem gerenciar = new javax.swing.JMenuItem("Gerenciar usuários");
            gerenciar.addActionListener(e -> new GerenciamentoUsuarios().setVisible(true));
            menuUsuario.add(gerenciar);
            javax.swing.JMenuItem historico = new javax.swing.JMenuItem("Histórico");
            historico.addActionListener(e -> new Historico().setVisible(true));
            menuUsuario.add(historico);
            javax.swing.JMenuItem relatorios = new javax.swing.JMenuItem("Relatórios mensais");
            relatorios.addActionListener(e -> new Relatorios().setVisible(true));
            menuUsuario.add(relatorios);
        }
        javax.swing.JMenuItem logout = new javax.swing.JMenuItem("Logout");
        logout.addActionListener(e -> realizarLogout());
        menuUsuario.add(logout);
        jMenuBar1.add(menuUsuario);
    }
    private void configurarPesquisa() { javax.swing.JPanel painel=new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT)); campoPesquisa=new javax.swing.JTextField(28); javax.swing.JButton limpar=new javax.swing.JButton("Limpar"); limpar.addActionListener(e->campoPesquisa.setText("")); campoPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){carregarTabelaVencimentos();}public void removeUpdate(javax.swing.event.DocumentEvent e){carregarTabelaVencimentos();}public void changedUpdate(javax.swing.event.DocumentEvent e){carregarTabelaVencimentos();}});painel.add(new javax.swing.JLabel("Pesquisar despesa:"));painel.add(campoPesquisa);painel.add(limpar);jScrollPane1.setColumnHeaderView(painel); }

    private void realizarLogout() {
        try {
            new HistoricoService().registrar("LOGOUT", "realizou logout.");
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Não foi possível registrar o logout.", "ODD", JOptionPane.WARNING_MESSAGE);
        } finally {
            SessaoUsuario.encerrar();
        }
        dispose();
        new Login().setVisible(true);
    }
    
    public void carregarResumoFinanceiro() {

    try {

        ResumoFinanceiro resumo = new FinanceiroService().calcularResumo();

        ctReceitaMenu.setText(
                String.format(
                        "R$ %.2f",
                        resumo.getReceitas()
                )
        );

        ctDespesaMenu.setText(
                String.format(
                        "R$ %.2f",
                        resumo.getDespesas()
                )
        );

        ctReceitaSaldo.setText(
                String.format(
                        "R$ %.2f",
                        resumo.getSaldo()
                )
        );

    } catch (Exception e) {

        e.printStackTrace();

        JOptionPane.showMessageDialog(this, "Não foi possível carregar o resumo financeiro.");
    }
}

    private double valorOuZero(Double valor) {
        return valor == null ? 0.0 : valor;
    }

    private Date inicioDoDia(Date data) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(data);
        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
    }

    private String formatarData(Date data) {
        return data == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(data);
    }
    
    public void carregarTabelaVencimentos() {

    try {

        List<Despesa> lista = new DespesaService().listarParaVencimentos();
        String pesquisa = campoPesquisa == null ? "" : campoPesquisa.getText().trim().toLowerCase();

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Nome", "Vencimento", "Valor", "Status"}, 0) {
            @Override
            public Class<?> getColumnClass(int coluna) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };

        Date hoje = inicioDoDia(new Date());

        for (Despesa d : lista) {

            StatusDespesa situacao = new DespesaService().calcularStatus(d);
            String status = situacao.toString().replace('_', ' ');
            if (!pesquisa.isEmpty() && !d.getDespesa().toLowerCase().contains(pesquisa) && !status.toLowerCase().contains(pesquisa)) continue;

            modelo.addRow(new Object[]{

                d.getDespesa(),

                formatarData(d.getDataVencimento()),

                String.format(
                        "R$ %.2f",
                        valorOuZero(d.getValor())
                ),

                status
            });
        }

        TabelaPrincipal.setModel(modelo);

    } catch (Exception e) {

        e.printStackTrace();

        JOptionPane.showMessageDialog(this, "Não foi possível carregar os vencimentos.");
    }
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        TabelaPrincipal = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        ctReceitaMenu = new javax.swing.JTextField();
        ctReceitaSaldo = new javax.swing.JTextField();
        ctDespesaMenu = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btCadastroDespesa = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        btCadastroReceita = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        btmnMenuSair = new javax.swing.JMenu();
        btmnMenuPrincipalSair = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        TabelaPrincipal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Nome", "Vencimento", "Valor", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(TabelaPrincipal);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("RESUMO FINANCEIRO");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Despesas:");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel3.setText("Saldo Atual:");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel4.setText("Receitas:");

        ctReceitaMenu.setEditable(false);

        ctReceitaSaldo.setEditable(false);

        ctDespesaMenu.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(ctDespesaMenu, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .addComponent(ctReceitaMenu, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ctReceitaSaldo))))
                .addContainerGap(102, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ctReceitaMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ctDespesaMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(ctReceitaSaldo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(119, Short.MAX_VALUE))
        );

        jLabel5.setFont(new java.awt.Font("Arial", 2, 36)); // NOI18N
        jLabel5.setText("Organizador de Despesas - ODD");

        jLabel6.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel6.setText("PROXIMOS VENCIMENTOS");

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        btCadastroDespesa.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btCadastroDespesa.setText("Cadastrar Despesa");
        btCadastroDespesa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCadastroDespesaActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jLabel7.setText("FUNÇÕES");

        btCadastroReceita.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btCadastroReceita.setText("Cadastrar Receitas");
        btCadastroReceita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCadastroReceitaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btCadastroDespesa)
                    .addComponent(btCadastroReceita))
                .addGap(0, 457, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(btCadastroDespesa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btCadastroReceita, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btmnMenuSair.setText("Sair");

        btmnMenuPrincipalSair.setText("Sair");
        btmnMenuPrincipalSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btmnMenuPrincipalSairActionPerformed(evt);
            }
        });
        btmnMenuSair.add(btmnMenuPrincipalSair);

        jMenuBar1.add(btmnMenuSair);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1274, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(2, 2, 2)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btCadastroDespesaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCadastroDespesaActionPerformed
        // TODO add your handling code here:
        CadastroDespesas tela =
        new CadastroDespesas(this, true);
        tela.setLocationRelativeTo(null);
        tela.setVisible(true);
        carregarResumoFinanceiro();
        carregarTabelaVencimentos();
    }//GEN-LAST:event_btCadastroDespesaActionPerformed

    private void btmnMenuPrincipalSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btmnMenuPrincipalSairActionPerformed
        realizarLogout();
    }//GEN-LAST:event_btmnMenuPrincipalSairActionPerformed

    private void btCadastroReceitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCadastroReceitaActionPerformed
        // TODO add your handling code here:
        CadastroReceita tela =
        new CadastroReceita(this, true);
        tela.setLocationRelativeTo(null);
        tela.setVisible(true);
        carregarResumoFinanceiro();
    }//GEN-LAST:event_btCadastroReceitaActionPerformed

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
            java.util.logging.Logger.getLogger(MenuPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MenuPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MenuPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MenuPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        sistemaodd.SistemaODD.main(args);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable TabelaPrincipal;
    private javax.swing.JButton btCadastroDespesa;
    private javax.swing.JButton btCadastroReceita;
    private javax.swing.JMenuItem btmnMenuPrincipalSair;
    private javax.swing.JMenu btmnMenuSair;
    private javax.swing.JTextField ctDespesaMenu;
    private javax.swing.JTextField ctReceitaMenu;
    private javax.swing.JTextField ctReceitaSaldo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
