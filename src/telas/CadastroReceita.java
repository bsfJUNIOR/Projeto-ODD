/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package telas;

import entidades.Receita;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import persistencia.HibernateUtil;
import servicos.ReceitaService;
import servicos.FormatoUtil;
/**
 *
 * @author Usuario
 */
public class CadastroReceita extends javax.swing.JDialog {
    
    Receita receita = new Receita();
    List<Receita> listaReceitas = new ArrayList<>();
    boolean editando = false;
    private javax.swing.JTextField campoPesquisaReceita;
    
    /**
     * Creates new form CadastroReceita
     */
    public CadastroReceita(java.awt.Frame parent, boolean modal) {

        super(parent, modal);

        initComponents();
        TabelaCReceita.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabelaCReceitaMouseClicked(evt);
            }
        });
        configurarRadioButtons();
        configurarCampoValor();
        configurarPesquisaTabela();
        montaTabela();
        validaCampos("inicio");
        setLocationRelativeTo(null);
    }
    private void configurarPesquisaTabela() { javax.swing.JPanel painel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT)); campoPesquisaReceita = new javax.swing.JTextField(28); javax.swing.JButton limpar = new javax.swing.JButton("Limpar"); limpar.addActionListener(e -> campoPesquisaReceita.setText("")); campoPesquisaReceita.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() { public void insertUpdate(javax.swing.event.DocumentEvent e) { montaTabela(); } public void removeUpdate(javax.swing.event.DocumentEvent e) { montaTabela(); } public void changedUpdate(javax.swing.event.DocumentEvent e) { montaTabela(); } }); painel.add(new javax.swing.JLabel("Pesquisar receita:")); painel.add(campoPesquisaReceita); painel.add(limpar); jScrollPane1.setColumnHeaderView(painel); }

    public void configurarRadioButtons() {

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(cbFixaReceita);
        grupo.add(cbVariavelReceita);
    }
        
    public void configurarCampoValor() {

        ctValorReceita.addKeyListener(
                new java.awt.event.KeyAdapter() {

            public void keyTyped(java.awt.event.KeyEvent evt) {

                char c = evt.getKeyChar();

                if (!Character.isDigit(c)
                        && c != ','
                        && c != '.') {

                    evt.consume();
                }
            }
        });
    }
    
    public void limparCampos() {
        
    ctNomeReceita.setText("");
    ctDescricaoReceita.setText("");
    ctValorReceita.setText("");
    
    cbFixaReceita.setSelected(false);
    cbVariavelReceita.setSelected(false);
}

    private Double obterValorInformado() {
        return FormatoUtil.valor(ctValorReceita.getText());
    }

    private void tratarErro(String operacao, Exception e) {
        HibernateUtil.rollbackTransaction();
        HibernateUtil.closeSession();
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Não foi possível " + operacao + ".\nTente novamente.");
    }
    
    public void validaCampos(String operacao) {

        if (operacao.equals("inicio")) {

            ctNomeReceita.setEnabled(false);
            ctDescricaoReceita.setEnabled(false);
            ctValorReceita.setEnabled(false);

            cbFixaReceita.setEnabled(false);
            cbVariavelReceita.setEnabled(false);

            btNovoReceita.setEnabled(true);

            btSalvarReceita.setEnabled(false);
            btEditarReceita.setEnabled(false);
            btExcluirReceita.setEnabled(false);
            btCancelarReceita.setEnabled(false);

        } else if (operacao.equals("novo")) {

            ctNomeReceita.setEnabled(true);
            ctDescricaoReceita.setEnabled(true);
            ctValorReceita.setEnabled(true);

            cbFixaReceita.setEnabled(true);
            cbVariavelReceita.setEnabled(true);

            btNovoReceita.setEnabled(false);

            btSalvarReceita.setEnabled(true);
            btEditarReceita.setEnabled(false);
            btExcluirReceita.setEnabled(false);
            btCancelarReceita.setEnabled(true);

        } else if (operacao.equals("selecionado")) {

            ctNomeReceita.setEnabled(false);
            ctDescricaoReceita.setEnabled(false);
            ctValorReceita.setEnabled(false);

            cbFixaReceita.setEnabled(false);
            cbVariavelReceita.setEnabled(false);

            btNovoReceita.setEnabled(false);

            btSalvarReceita.setEnabled(false);
            btEditarReceita.setEnabled(true);
            btExcluirReceita.setEnabled(true);
            btCancelarReceita.setEnabled(true);
        }
    }
    
    public Boolean camposObrigatorios() {

        String mensagem = "";

        Boolean retorno = true;

        if (ctNomeReceita.getText().trim().equals("")) {
            mensagem += "Nome da receita obrigatório!\n";
            retorno = false;
        }

        if (obterValorInformado() == null) {
            mensagem += "Informe um valor maior que zero!\n";
            retorno = false;
        }

        if (!cbFixaReceita.isSelected()
                && !cbVariavelReceita.isSelected()) {
            mensagem += "Selecione o tipo!\n";
            retorno = false;
        }

        if (!mensagem.equals("")) {
            JOptionPane.showMessageDialog(
                    null,
                    mensagem
            );
        }

        return retorno;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
        
    public void montaTabela() {

        try {

            listaReceitas = new ReceitaService().listar();

            DefaultTableModel modelo = new DefaultTableModel(
                    new Object[]{"ID", "Nome", "Valor", "Tipo"}, 0) {
                @Override
                public Class<?> getColumnClass(int coluna) {
                    return coluna == 0 ? Long.class : String.class;
                }

                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }
            };

            for (Receita r : listaReceitas) {

                String pesquisa = campoPesquisaReceita == null ? "" : campoPesquisaReceita.getText().trim().toLowerCase();
                if (!pesquisa.isEmpty() && !r.getNome().toLowerCase().contains(pesquisa) && (r.getTipo() == null || !r.getTipo().toLowerCase().contains(pesquisa))) continue;

                modelo.addRow(new Object[]{

                    r.getId(),

                    r.getNome(),

                    NumberFormat.getCurrencyInstance(
                            new Locale("pt", "BR"))
                            .format(r.getValor() == null ? 0.0 : r.getValor()),

                    r.getTipo()
                });
            }

            TabelaCReceita.setModel(modelo);

        } catch (Exception e) {

            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Não foi possível carregar a tabela de receitas.");
        }
    }
    
        
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TabelaCReceita = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        ctNomeReceita = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ctDescricaoReceita = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        ctValorReceita = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        btSalvarReceita = new javax.swing.JButton();
        btEditarReceita = new javax.swing.JButton();
        btExcluirReceita = new javax.swing.JButton();
        btCancelarReceita = new javax.swing.JButton();
        btNovoReceita = new javax.swing.JButton();
        cbFixaReceita = new javax.swing.JCheckBox();
        cbVariavelReceita = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        btmnVoltarReceita = new javax.swing.JMenu();
        btmnReceitaVoltar = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jLabel1.setText("DADOS DA RECEITA");

        TabelaCReceita.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Valor", "Tipo", "Vencimento", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Double.class, java.lang.String.class, java.lang.Double.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(TabelaCReceita);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Nome da Receita:");

        ctNomeReceita.setText("Ex: Salario");
        ctNomeReceita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ctNomeReceitaActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel3.setText("Descrição:");

        ctDescricaoReceita.setColumns(20);
        ctDescricaoReceita.setRows(5);
        jScrollPane2.setViewportView(ctDescricaoReceita);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel4.setText("Valor:");

        ctValorReceita.setToolTipText("");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel5.setText("Tipo:");

        btSalvarReceita.setText("Salvar");
        btSalvarReceita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSalvarReceitaActionPerformed(evt);
            }
        });

        btEditarReceita.setText("Editar");
        btEditarReceita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEditarReceitaActionPerformed(evt);
            }
        });

        btExcluirReceita.setText("Excluir");
        btExcluirReceita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btExcluirReceitaActionPerformed(evt);
            }
        });

        btCancelarReceita.setText("Cancelar");
        btCancelarReceita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarReceitaActionPerformed(evt);
            }
        });

        btNovoReceita.setText("Novo");
        btNovoReceita.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btNovoReceitaActionPerformed(evt);
            }
        });

        cbFixaReceita.setText("Fixa");

        cbVariavelReceita.setText("Variavel");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(ctValorReceita, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ctNomeReceita, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
                                .addGap(0, 370, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btNovoReceita)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(btSalvarReceita)
                                .addGap(18, 18, 18)
                                .addComponent(btEditarReceita)
                                .addGap(18, 18, 18)
                                .addComponent(btExcluirReceita)
                                .addGap(18, 18, 18)
                                .addComponent(btCancelarReceita))
                            .addComponent(jLabel5)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cbFixaReceita)
                                .addGap(18, 18, 18)
                                .addComponent(cbVariavelReceita))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(ctNomeReceita, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ctValorReceita, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbFixaReceita)
                            .addComponent(cbVariavelReceita))
                        .addGap(214, 214, 214)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSalvarReceita)
                    .addComponent(btEditarReceita)
                    .addComponent(btExcluirReceita)
                    .addComponent(btCancelarReceita)
                    .addComponent(btNovoReceita))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        btmnVoltarReceita.setText("Voltar");

        btmnReceitaVoltar.setText("Voltar");
        btmnReceitaVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btmnReceitaVoltarActionPerformed(evt);
            }
        });
        btmnVoltarReceita.add(btmnReceitaVoltar);

        jMenuBar1.add(btmnVoltarReceita);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ctNomeReceitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ctNomeReceitaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ctNomeReceitaActionPerformed

    private void btmnReceitaVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btmnReceitaVoltarActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btmnReceitaVoltarActionPerformed

    private void btNovoReceitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btNovoReceitaActionPerformed
        // TODO add your handling code here:
        receita = new Receita();
        editando = false;
        limparCampos();
        validaCampos("novo");
    }//GEN-LAST:event_btNovoReceitaActionPerformed

    private void btSalvarReceitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarReceitaActionPerformed
        try {
            if (!camposObrigatorios()) {
                return;
            }

            receita.setNome(ctNomeReceita.getText().trim());
            receita.setDescricao(ctDescricaoReceita.getText());
            receita.setValor(obterValorInformado());

            if (cbFixaReceita.isSelected()) {

                receita.setTipo("Fixa");

            } else {

                receita.setTipo("Variável");
            }

            new ReceitaService().salvar(receita);

            editando = false;

            JOptionPane.showMessageDialog(
                    null,
                    "Receita salva com sucesso!"
            );

            montaTabela();

            receita = new Receita();
            limparCampos();

            validaCampos("inicio");
        } catch (Exception e) {
            tratarErro("salvar a receita", e);
        }
    }//GEN-LAST:event_btSalvarReceitaActionPerformed

    private void btEditarReceitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEditarReceitaActionPerformed
        if (receita == null || receita.getId() == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma receita para editar.");
            return;
        }
        editando = true;
        validaCampos("novo");
    }//GEN-LAST:event_btEditarReceitaActionPerformed

    private void btExcluirReceitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btExcluirReceitaActionPerformed
        if (receita == null || receita.getId() == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma receita para excluir.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja realmente excluir este registro?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {

            new ReceitaService().excluir(receita);

            JOptionPane.showMessageDialog(
                    null,
                    "Receita excluída!"
            );

            montaTabela();

            receita = new Receita();
            editando = false;
            limparCampos();

            validaCampos("inicio");

        } catch (Exception e) {
            tratarErro("excluir a receita", e);
        }
    }//GEN-LAST:event_btExcluirReceitaActionPerformed

    private void btCancelarReceitaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelarReceitaActionPerformed
        // TODO add your handling code here:
        limparCampos();
        TabelaCReceita.clearSelection();
        receita = new Receita();
        editando = false;
        validaCampos("inicio");
    }//GEN-LAST:event_btCancelarReceitaActionPerformed

        private void TabelaCReceitaMouseClicked(
            java.awt.event.MouseEvent evt) {

        int linhaSelecionada = TabelaCReceita.getSelectedRow();
        if (linhaSelecionada < 0 || linhaSelecionada >= listaReceitas.size()) {
            return;
        }

        receita = listaReceitas.get(linhaSelecionada);

        ctNomeReceita.setText(
                receita.getNome() == null ? "" : receita.getNome()
        );

        ctDescricaoReceita.setText(
                receita.getDescricao() == null ? "" : receita.getDescricao()
        );

        ctValorReceita.setText(
                String.valueOf(
                        receita.getValor() == null ? "" : receita.getValor()
                )
        );

        cbFixaReceita.setSelected(false);
        cbVariavelReceita.setSelected(false);
        if ("Fixa".equals(receita.getTipo())) {

            cbFixaReceita.setSelected(true);

        } else if ("Variável".equals(receita.getTipo())) {

            cbVariavelReceita.setSelected(true);
        }

        validaCampos("selecionado");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
            java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                CadastroReceita dialog =
                        new CadastroReceita(
                                new javax.swing.JFrame(),
                                true
                        );

                dialog.addWindowListener(
                        new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(
                            java.awt.event.WindowEvent e) {

                        System.exit(0);
                    }
                });

                dialog.setVisible(true);
            }
        });
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
            java.util.logging.Logger.getLogger(CadastroReceita.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CadastroReceita.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CadastroReceita.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CadastroReceita.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CadastroReceita dialog = new CadastroReceita(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable TabelaCReceita;
    private javax.swing.JButton btCancelarReceita;
    private javax.swing.JButton btEditarReceita;
    private javax.swing.JButton btExcluirReceita;
    private javax.swing.JButton btNovoReceita;
    private javax.swing.JButton btSalvarReceita;
    private javax.swing.JMenuItem btmnReceitaVoltar;
    private javax.swing.JMenu btmnVoltarReceita;
    private javax.swing.JCheckBox cbFixaReceita;
    private javax.swing.JCheckBox cbVariavelReceita;
    private javax.swing.JTextArea ctDescricaoReceita;
    private javax.swing.JTextField ctNomeReceita;
    private javax.swing.JTextField ctValorReceita;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
