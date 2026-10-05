package servicos;

import entidades.Despesa;
import model.FiltroDespesa;
import model.StatusDespesa;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import persistencia.HibernateUtil;

public class DespesaService {

    private final HistoricoService historicoService = new HistoricoService();

    @SuppressWarnings("unchecked")
    public List<Despesa> listar() {
        AutorizacaoService.exigirFinanceiro();
        try {
            return listarAtivas(null);
        } finally {
            HibernateUtil.closeSession();
        }
    }
    public List<Despesa> listar(FiltroDespesa filtro) { AutorizacaoService.exigirFinanceiro(); try { List<Despesa> lista=listarAtivas(filtro); if(filtro!=null&&filtro.status!=null){java.util.Iterator<Despesa> it=lista.iterator();while(it.hasNext())if(calcularStatus(it.next())!=filtro.status)it.remove();} return lista; } finally { HibernateUtil.closeSession(); } }

    public StatusDespesa calcularStatus(Despesa d) {
        if ("PAGO".equalsIgnoreCase(d.getStatus()) || "Pago".equalsIgnoreCase(d.getStatus())) return StatusDespesa.PAGO;
        if (d.getDataVencimento() == null) return StatusDespesa.PENDENTE;
        Calendar hoje=Calendar.getInstance(); zerarHora(hoje); Calendar venc=Calendar.getInstance(); venc.setTime(d.getDataVencimento()); zerarHora(venc);
        if (venc.before(hoje)) return StatusDespesa.ATRASADA;
        Calendar limite=(Calendar)hoje.clone(); limite.add(Calendar.DAY_OF_YEAR,7);
        return !venc.after(limite) ? StatusDespesa.PROXIMA_DO_VENCIMENTO : StatusDespesa.PENDENTE;
    }
    private void zerarHora(Calendar c){c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);}

    @SuppressWarnings("unchecked")
    public List<Despesa> listarParaVencimentos() {
        AutorizacaoService.exigirFinanceiro();
        try {
            List<Despesa> lista = listarAtivas(null);
            java.util.Collections.sort(lista, new java.util.Comparator<Despesa>() { public int compare(Despesa a, Despesa b) { Date da=a.getDataVencimento(), db=b.getDataVencimento(); if(da==null)return 1;if(db==null)return -1;return da.compareTo(db); }});
            return lista;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public void salvar(Despesa despesa) {
        AutorizacaoService.exigirFinanceiro();
        boolean novo = despesa.getId() == null;
        try {
            HibernateUtil.beginTransaction();
            if (novo) despesa.setAtivo(true);
            if (despesa.getStatus() == null || "Selecionar".equals(despesa.getStatus())) despesa.setStatus("PENDENTE");
            salvarEntidade(despesa);
            historicoService.registrarNaTransacao(novo ? "CADASTRO" : "EDICAO",
                    (novo ? "cadastrou a despesa " : "editou a despesa ") + despesa.getDespesa() + ".");
            HibernateUtil.commitTransaction();
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public void excluir(Despesa despesa) {
        AutorizacaoService.exigirFinanceiro();
        try {
            HibernateUtil.beginTransaction();
            Despesa gerenciada = (Despesa) HibernateUtil.getSession().get(Despesa.class, despesa.getId());
            if (gerenciada != null) {
                String nome = gerenciada.getDespesa();
                gerenciada.setAtivo(false);
                salvarEntidade(gerenciada);
                historicoService.registrarNaTransacao("EXCLUSAO", "excluiu a despesa " + nome + ".");
            }
            HibernateUtil.commitTransaction();
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    @SuppressWarnings("unchecked") private List<Despesa> listarAtivas(FiltroDespesa f) { StringBuilder hql=new StringBuilder("FROM Despesa d WHERE (d.ativo = true OR d.ativo IS NULL)"); if(f!=null){if(f.nome!=null&&!f.nome.trim().isEmpty())hql.append(" AND lower(d.despesa) LIKE :nome");if(f.tipo!=null&&!f.tipo.isEmpty())hql.append(" AND d.tipo = :tipo");if(f.inicio!=null)hql.append(" AND d.dataVencimento >= :inicio");if(f.fim!=null)hql.append(" AND d.dataVencimento <= :fim");} org.hibernate.Query q=HibernateUtil.getSession().createQuery(hql.toString());if(f!=null){if(f.nome!=null&&!f.nome.trim().isEmpty())q.setString("nome","%"+f.nome.toLowerCase()+"%");if(f.tipo!=null&&!f.tipo.isEmpty())q.setString("tipo",f.tipo);if(f.inicio!=null)q.setDate("inicio",f.inicio);if(f.fim!=null)q.setDate("fim",f.fim);}return q.list(); }
    private void salvarEntidade(Despesa d){if(d.getId()==null)HibernateUtil.getSession().persist(d);else HibernateUtil.getSession().merge(d);}
}
