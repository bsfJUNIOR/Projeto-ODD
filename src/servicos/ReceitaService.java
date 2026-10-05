package servicos;

import entidades.Receita;
import model.FiltroReceita;
import java.util.Date;
import java.util.List;
import persistencia.HibernateUtil;

public class ReceitaService {

    private final HistoricoService historicoService = new HistoricoService();

    @SuppressWarnings("unchecked")
    public List<Receita> listar() {
        AutorizacaoService.exigirFinanceiro();
        try {
            return listarAtivas(null);
        } finally {
            HibernateUtil.closeSession();
        }
    }
    public List<Receita> listar(FiltroReceita filtro) { AutorizacaoService.exigirFinanceiro(); try { return listarAtivas(filtro); } finally { HibernateUtil.closeSession(); } }

    public void salvar(Receita receita) {
        AutorizacaoService.exigirFinanceiro();
        boolean novo = receita.getId() == null;
        try {
            HibernateUtil.beginTransaction();
            if (novo) { receita.setAtivo(true); if (receita.getDataRecebimento()==null) receita.setDataRecebimento(new Date()); }
            salvarEntidade(receita);
            historicoService.registrarNaTransacao(novo ? "CADASTRO" : "EDICAO",
                    (novo ? "cadastrou a receita " : "editou a receita ") + receita.getNome() + ".");
            HibernateUtil.commitTransaction();
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public void excluir(Receita receita) {
        AutorizacaoService.exigirFinanceiro();
        try {
            HibernateUtil.beginTransaction();
            Receita gerenciada = (Receita) HibernateUtil.getSession().get(Receita.class, receita.getId());
            if (gerenciada != null) {
                String nome = gerenciada.getNome();
                gerenciada.setAtivo(false);
                salvarEntidade(gerenciada);
                historicoService.registrarNaTransacao("EXCLUSAO", "excluiu a receita " + nome + ".");
            }
            HibernateUtil.commitTransaction();
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } finally {
            HibernateUtil.closeSession();
        }
    }
    @SuppressWarnings("unchecked") private List<Receita> listarAtivas(FiltroReceita f){StringBuilder h=new StringBuilder("FROM Receita r WHERE (r.ativo = true OR r.ativo IS NULL)");if(f!=null){if(f.nome!=null&&!f.nome.trim().isEmpty())h.append(" AND lower(r.nome) LIKE :nome");if(f.tipo!=null&&!f.tipo.isEmpty())h.append(" AND r.tipo = :tipo");if(f.inicio!=null)h.append(" AND r.dataRecebimento >= :inicio");if(f.fim!=null)h.append(" AND r.dataRecebimento <= :fim");}org.hibernate.Query q=HibernateUtil.getSession().createQuery(h.toString());if(f!=null){if(f.nome!=null&&!f.nome.trim().isEmpty())q.setString("nome","%"+f.nome.toLowerCase()+"%");if(f.tipo!=null&&!f.tipo.isEmpty())q.setString("tipo",f.tipo);if(f.inicio!=null)q.setDate("inicio",f.inicio);if(f.fim!=null)q.setDate("fim",f.fim);}return q.list();} private void salvarEntidade(Receita r){if(r.getId()==null)HibernateUtil.getSession().persist(r);else HibernateUtil.getSession().merge(r);}
}
