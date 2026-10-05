package telas;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import servicos.AutorizacaoService;
import servicos.HistoricoService;

/** Consulta administrativa dos eventos de auditoria. */
public class Historico extends JFrame {

    private final JTable tabela = new JTable();
    private final HistoricoService historicoService = new HistoricoService();
    private final JComboBox<String> filtroAcao = new JComboBox<String>(new String[]{"Todas", "LOGIN", "LOGOUT", "CADASTRO", "EDICAO", "EXCLUSAO"});

    public Historico() {
        super("ODD - Histórico");
        AutorizacaoService.exigirAdministrador();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel filtro = new JPanel();
        filtro.add(new JLabel("Ação:"));
        filtro.add(filtroAcao);
        filtroAcao.addActionListener(e -> carregarTabela());
        add(filtro, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        carregarTabela();
        setSize(720, 360);
        setLocationRelativeTo(null);
    }

    private void carregarTabela() {
        String acao = "Todas".equals(filtroAcao.getSelectedItem()) ? null : (String) filtroAcao.getSelectedItem();
        List<entidades.Historico> registros = historicoService.listar(acao);
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Usuário", "Ação", "Descrição", "Data/hora"}, 0) {
            @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        };
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        for (entidades.Historico registro : registros) {
            modelo.addRow(new Object[]{
                registro.getUsuario().getUsuario(),
                registro.getAcao(),
                registro.getDescricao(),
                formato.format(registro.getDataHora())
            });
        }
        tabela.setModel(modelo);
    }
}
