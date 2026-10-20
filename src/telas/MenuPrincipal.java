/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package telas;

import entidades.Despesa;
import java.awt.HeadlessException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import servicos.DespesaService;
import servicos.HistoricoService;
import servicos.FinanceiroService;
import model.ResumoFinanceiro;
import model.StatusDespesa;
import sessao.SessaoUsuario;

/**
 *
 * @author Usuario
 */
public final class MenuPrincipal extends javax.swing.JFrame {

    private List<Despesa> despesasExibidas = new java.util.ArrayList<>();

    public MenuPrincipal() {
        if (!SessaoUsuario.estaAutenticado()) {
            throw new IllegalStateException("É necessário realizar login para abrir o menu principal.");
        }
        initComponents();
        configurarAcessosDaTela();
        configurarFiltrosDaTela();
        setLocationRelativeTo(null);

        carregarResumoFinanceiro();
        carregarTabelaVencimentos();
    }

    private void configurarAcessosDaTela() {
        boolean admin = SessaoUsuario.ehAdministrador();
        btCadastroUsuario.setEnabled(admin); btHistorico.setEnabled(admin); btRelatorioMensal.setEnabled(admin);
        btCadastroUsuario.addActionListener(e -> { if (admin) abrirJanelaSecundaria(new GerenciadorUsuario()); });
        btHistorico.addActionListener(e -> { if (admin) abrirJanelaSecundaria(new HistoricoUsuario()); });
        btRelatorioMensal.addActionListener(e -> { if (admin) abrirJanelaSecundaria(new RelatorioFinanceiro()); });
        btMenuLogout.addMouseListener(new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { realizarLogout(); } });
    }

    private void abrirJanelaSecundaria(final javax.swing.JFrame tela) {
        setEnabled(false);
        tela.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        tela.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                setEnabled(true);
                toFront();
                carregarResumoFinanceiro();
                carregarTabelaVencimentos();
            }
        });
        tela.setLocationRelativeTo(this);
        tela.setVisible(true);
    }

    private void configurarFiltrosDaTela() {
        Calendar agora = Calendar.getInstance(); cbxTabela.setSelectedIndex(agora.get(Calendar.MONTH)); ctAnoTabela.setText(String.valueOf(agora.get(Calendar.YEAR)));
        btBuscar.addActionListener(e -> carregarTabelaVencimentos());
        ctBuscaTabela.addActionListener(e -> carregarTabelaVencimentos());
    }

    private void realizarLogout() {
        if (JOptionPane.showConfirmDialog(this, "Deseja realmente sair da conta atual?", "Confirmar logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            new HistoricoService().registrar("LOGOUT", "realizou logout.");
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Não foi possível registrar o logout.", "ODD", JOptionPane.WARNING_MESSAGE);
        } finally {
            SessaoUsuario.encerrar();
        }
        dispose();
        new LoginUsuario().setVisible(true);
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
            despesasExibidas = new java.util.ArrayList<>();
            String pesquisa = ctBuscaTabela.getText().trim().toLowerCase();
            int mes = cbxTabela.getSelectedIndex(); int ano;
            try { ano = Integer.parseInt(ctAnoTabela.getText().trim()); } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Informe um ano válido."); return; }

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
                Calendar periodo = Calendar.getInstance(); if (d.getDataVencimento() == null) continue; periodo.setTime(d.getDataVencimento());
                if (periodo.get(Calendar.MONTH) != mes || periodo.get(Calendar.YEAR) != ano) continue;

                StatusDespesa situacao = new DespesaService().calcularStatus(d);
                String status = situacao.toString().replace('_', ' ');
                if (!pesquisa.isEmpty() && !d.getDespesa().toLowerCase().contains(pesquisa) && !status.toLowerCase().contains(pesquisa)) {
                    continue;
                }

                despesasExibidas.add(d);

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

        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this, "Não foi possível carregar os vencimentos.");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton3 = new javax.swing.JButton();
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
        btEditarResumo = new javax.swing.JButton();
        btHistorico = new javax.swing.JButton();
        btRelatorioMensal = new javax.swing.JButton();
        btCadastroUsuario = new javax.swing.JButton();
        ctBuscaTabela = new javax.swing.JTextField();
        btBuscar = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        cbxTabela = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        ctAnoTabela = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        btmnMenuSair = new javax.swing.JMenu();
        btmnMenuPrincipalSair = new javax.swing.JMenuItem();
        btMenuLogout = new javax.swing.JMenu();

        jButton3.setText("jButton1");

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

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("RESUMO FINANCEIRO");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Despesas Pendentes:");

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel5.setFont(new java.awt.Font("Arial", 2, 36)); // NOI18N
        jLabel5.setText("Organizador de Despesas - ODD");

        jLabel6.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel6.setText("PROXIMOS VENCIMENTOS");

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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

        btEditarResumo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btEditarResumo.setText("Editar");
        btEditarResumo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEditarResumoActionPerformed(evt);
            }
        });

        btHistorico.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btHistorico.setText("Historico");

        btRelatorioMensal.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btRelatorioMensal.setText("Relatorios mensais");

        btCadastroUsuario.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        btCadastroUsuario.setText("Usuarios");

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
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btCadastroDespesa)
                        .addGap(18, 18, 18)
                        .addComponent(btCadastroUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btEditarResumo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btCadastroReceita, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btRelatorioMensal, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 247, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btCadastroDespesa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btCadastroUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btCadastroReceita, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 119, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btEditarResumo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btRelatorioMensal, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        btBuscar.setText("Buscar");

        jLabel8.setText("Mês:");

        cbxTabela.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" }));

        jLabel9.setText("Ano:");

        btmnMenuSair.setText("Sair");

        btmnMenuPrincipalSair.setText("Sair");
        btmnMenuPrincipalSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btmnMenuPrincipalSairActionPerformed(evt);
            }
        });
        btmnMenuSair.add(btmnMenuPrincipalSair);

        jMenuBar1.add(btmnMenuSair);

        btMenuLogout.setText("Logout");
        jMenuBar1.add(btMenuLogout);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(ctBuscaTabela, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbxTabela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ctAnoTabela, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btBuscar))
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1274, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel5)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxTabela)
                    .addComponent(jLabel9)
                    .addComponent(ctAnoTabela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ctBuscaTabela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btBuscar))
                .addGap(20, 20, 20)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btCadastroDespesaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCadastroDespesaActionPerformed
        // TODO add your handling code here:
        CadastroDespesas tela
                = new CadastroDespesas(this, true);
        tela.setLocationRelativeTo(null);
        tela.setVisible(true);
        carregarResumoFinanceiro();
        carregarTabelaVencimentos();
    }//GEN-LAST:event_btCadastroDespesaActionPerformed

    private void btmnMenuPrincipalSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btmnMenuPrincipalSairActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Deseja realmente encerrar o sistema?", "Confirmar encerramento", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }//GEN-LAST:event_btmnMenuPrincipalSairActionPerformed

    private void btCadastroReceitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCadastroReceitaActionPerformed
        // TODO add your handling code here:
        CadastroReceita tela
                = new CadastroReceita(this, true);
        tela.setLocationRelativeTo(null);
        tela.setVisible(true);
        carregarResumoFinanceiro();
    }//GEN-LAST:event_btCadastroReceitaActionPerformed

    private void btEditarResumoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEditarResumoActionPerformed
                int linha = TabelaPrincipal.getSelectedRow();
        if (linha < 0 || linha >= despesasExibidas.size()) {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa na tabela para editar.", "Editar despesa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CadastroDespesas tela = new CadastroDespesas(this, true);
        tela.editarDespesa(despesasExibidas.get(linha));
        tela.setLocationRelativeTo(this);
        tela.setVisible(true);
        carregarResumoFinanceiro();
        carregarTabelaVencimentos();
    }//GEN-LAST:event_btEditarResumoActionPerformed

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
    private javax.swing.JButton btBuscar;
    private javax.swing.JButton btCadastroDespesa;
    private javax.swing.JButton btCadastroReceita;
    private javax.swing.JButton btCadastroUsuario;
    private javax.swing.JButton btEditarResumo;
    private javax.swing.JButton btHistorico;
    private javax.swing.JMenu btMenuLogout;
    private javax.swing.JButton btRelatorioMensal;
    private javax.swing.JMenuItem btmnMenuPrincipalSair;
    private javax.swing.JMenu btmnMenuSair;
    private javax.swing.JComboBox<String> cbxTabela;
    private javax.swing.JTextField ctAnoTabela;
    private javax.swing.JTextField ctBuscaTabela;
    private javax.swing.JTextField ctDespesaMenu;
    private javax.swing.JTextField ctReceitaMenu;
    private javax.swing.JTextField ctReceitaSaldo;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
