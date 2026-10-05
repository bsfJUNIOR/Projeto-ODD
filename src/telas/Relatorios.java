package telas;

import java.awt.*;
import java.util.Calendar;
import javax.swing.*;
import model.ResumoFinanceiro;
import servicos.*;

public class Relatorios extends JFrame {

    private JComboBox<String> mes = new JComboBox<String>(new String[]{"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"});
    private JComboBox<Integer> ano = new JComboBox<Integer>();
    private JTextArea resumo = new JTextArea();

    public Relatorios() {
        super("ODD - Relatórios Mensais");
        AutorizacaoService.exigirAdministrador();
        for (int a = 2020; a <= Calendar.getInstance().get(Calendar.YEAR) + 1; a++) {
            ano.addItem(a);
        }
        mes.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));
        ano.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR));
        JButton gerar = new JButton("Gerar");
        gerar.addActionListener(e -> gerar());
        JPanel topo = new JPanel();
        topo.add(new JLabel("Mês:"));
        topo.add(mes);
        topo.add(new JLabel("Ano:"));
        topo.add(ano);
        topo.add(gerar);
        resumo.setEditable(false);
        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(resumo), BorderLayout.CENTER);
        setSize(500, 300);
        setLocationRelativeTo(null);
        gerar();
    }

    private void gerar() {
        ResumoFinanceiro r = new RelatoriosService().resumoMensal(mes.getSelectedIndex(), (Integer) ano.getSelectedItem());
        resumo.setText("Receitas: " + FormatoUtil.moeda(r.getReceitas()) + "\nDespesas: " + FormatoUtil.moeda(r.getDespesas()) + "\nSaldo: " + FormatoUtil.moeda(r.getSaldo()) + "\nPagas: " + FormatoUtil.moeda(r.getPagas()) + "\nPendentes: " + FormatoUtil.moeda(r.getPendentes()) + "\nAtrasadas: " + FormatoUtil.moeda(r.getAtrasadas()));
    }
}
