package servicos;

import entidades.Historico;
import entidades.Usuario;
import java.util.Date;
import java.util.List;
import persistencia.HibernateUtil;
import sessao.SessaoUsuario;

/** Registra e consulta a auditoria usando o usuário presente na sessão. */
public class HistoricoService {

    public void registrar(String acao, String descricao) {
        AutorizacaoService.exigirAutenticado();
        try {
            HibernateUtil.beginTransaction();
            registrarNaTransacao(acao, descricao);
            HibernateUtil.commitTransaction();
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public void registrarNaTransacao(String acao, String descricao) {
        Usuario responsavel = (Usuario) HibernateUtil.getSession()
                .get(Usuario.class, SessaoUsuario.getId());
        if (responsavel == null) {
            throw new SecurityException("Usuário autenticado não foi encontrado.");
        }
        Historico historico = new Historico();
        historico.setUsuario(responsavel);
        historico.setAcao(acao);
        historico.setDescricao(SessaoUsuario.getUsuario() + " " + descricao);
        historico.setDataHora(new Date());
        HibernateUtil.getSession().persist(historico);
    }

    @SuppressWarnings("unchecked")
    public List<Historico> listar() {
        return listar(null);
    }

    @SuppressWarnings("unchecked")
    public List<Historico> listar(String acao) {
        AutorizacaoService.exigirAdministrador();
        try {
            org.hibernate.Query consulta = HibernateUtil.getSession().createQuery(
                    acao == null || acao.length() == 0 ? "FROM Historico h ORDER BY h.dataHora DESC"
                    : "FROM Historico h WHERE h.acao = :acao ORDER BY h.dataHora DESC");
            if (acao != null && acao.length() > 0) consulta.setString("acao", acao);
            return consulta.list();
        } finally {
            HibernateUtil.closeSession();
        }
    }
}
