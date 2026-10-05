package telas;

import java.awt.GridLayout;
import javax.swing.*;
import model.*;
import servicos.FormatoUtil;

final class DialogoFiltros {

    private DialogoFiltros() {
    }

    static FiltroDespesa despesa(java.awt.Component p) {
        JTextField n = new JTextField(), i = new JTextField(), f = new JTextField();
        JComboBox<String> t = new JComboBox<String>(new String[]{"", "Fixa", "Variável"});
        JComboBox<StatusDespesa> s = new JComboBox<StatusDespesa>(StatusDespesa.values());
        s.insertItemAt(null, 0);
        s.setSelectedIndex(0);
        JPanel x = new JPanel(new GridLayout(0, 2, 5, 5));
        x.add(new JLabel("Nome:"));
        x.add(n);
        x.add(new JLabel("Tipo:"));
        x.add(t);
        x.add(new JLabel("Status:"));
        x.add(s);
        x.add(new JLabel("Início (dd/MM/yyyy):"));
        x.add(i);
        x.add(new JLabel("Fim (dd/MM/yyyy):"));
        x.add(f);
        if (JOptionPane.showConfirmDialog(p, x, "Filtrar despesas", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return null;
        }
        try {
            FiltroDespesa r = new FiltroDespesa();
            r.nome = n.getText();
            r.tipo = (String) t.getSelectedItem();
            r.status = (StatusDespesa) s.getSelectedItem();
            r.inicio = i.getText().trim().isEmpty() ? null : FormatoUtil.data(i.getText());
            r.fim = f.getText().trim().isEmpty() ? null : FormatoUtil.data(f.getText());
            return r;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(p, "Período inválido.");
            return null;
        }
    }

    static FiltroReceita receita(java.awt.Component p) {
        JTextField n = new JTextField(), i = new JTextField(), f = new JTextField();
        JComboBox<String> t = new JComboBox<String>(new String[]{"", "Fixa", "Variável"});
        JPanel x = new JPanel(new GridLayout(0, 2, 5, 5));
        x.add(new JLabel("Nome:"));
        x.add(n);
        x.add(new JLabel("Tipo:"));
        x.add(t);
        x.add(new JLabel("Início (dd/MM/yyyy):"));
        x.add(i);
        x.add(new JLabel("Fim (dd/MM/yyyy):"));
        x.add(f);
        if (JOptionPane.showConfirmDialog(p, x, "Filtrar receitas", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return null;
        }
        try {
            FiltroReceita r = new FiltroReceita();
            r.nome = n.getText();
            r.tipo = (String) t.getSelectedItem();
            r.inicio = i.getText().trim().isEmpty() ? null : FormatoUtil.data(i.getText());
            r.fim = f.getText().trim().isEmpty() ? null : FormatoUtil.data(f.getText());
            return r;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(p, "Período inválido.");
            return null;
        }
    }
}
