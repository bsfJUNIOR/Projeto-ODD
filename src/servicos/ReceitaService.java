package servicos;

import entidades.Receita;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import model.FiltroReceita;
import persistencia.HibernateUtil;

public class ReceitaService {
    private final HistoricoService historicoService = new HistoricoService();

    public List<Receita> listar() { AutorizacaoService.exigirFinanceiro(); try { return listarAtivas(); } finally { HibernateUtil.closeSession(); } }
    /** Lista o período solicitado, expandindo receitas fixas apenas como ocorrências virtuais. */
    public List<Receita> listar(FiltroReceita filtro) { AutorizacaoService.exigirFinanceiro(); try { return filtrarEProjetar(listarAtivas(), filtro); } finally { HibernateUtil.closeSession(); } }
    /** Para indicadores acumulados, substitui cada origem fixa por suas ocorrências até o mês atual. */
    public List<Receita> listarComRecorrenciasAteAtual() { AutorizacaoService.exigirFinanceiro(); try { return projetarAteAtual(listarAtivas()); } finally { HibernateUtil.closeSession(); } }

    public void salvar(Receita receita) {
        AutorizacaoService.exigirFinanceiro(); boolean novo = receita.getId() == null;
        try {
            HibernateUtil.beginTransaction(); if (novo) receita.setAtivo(true); if (receita.getDataRecebimento() == null) receita.setDataRecebimento(new Date()); salvarEntidade(receita);
            historicoService.registrarNaTransacao(novo ? "CADASTRO" : "EDICAO", (novo ? "cadastrou a receita " : "editou a receita ") + receita.getNome() + "."); HibernateUtil.commitTransaction();
        } catch (RuntimeException e) { HibernateUtil.rollbackTransaction(); throw e; } finally { HibernateUtil.closeSession(); }
    }
    public void excluir(Receita receita) {
        AutorizacaoService.exigirFinanceiro();
        try { HibernateUtil.beginTransaction(); Receita gerenciada = (Receita) HibernateUtil.getSession().get(Receita.class, receita.getId()); if (gerenciada != null) { String nome = gerenciada.getNome(); gerenciada.setAtivo(false); salvarEntidade(gerenciada); historicoService.registrarNaTransacao("EXCLUSAO", "excluiu a receita " + nome + "."); } HibernateUtil.commitTransaction(); }
        catch (RuntimeException e) { HibernateUtil.rollbackTransaction(); throw e; } finally { HibernateUtil.closeSession(); }
    }

    @SuppressWarnings("unchecked") private List<Receita> listarAtivas() { return HibernateUtil.getSession().createQuery("FROM Receita r WHERE (r.ativo = true OR r.ativo IS NULL)").list(); }
    private List<Receita> filtrarEProjetar(List<Receita> origens, FiltroReceita filtro) {
        if (filtro == null || (filtro.inicio == null && filtro.fim == null)) return filtrarBasicas(origens, filtro);
        Calendar inicio = calendarioDoMes(filtro.inicio == null ? new Date(0) : filtro.inicio), fim = calendarioDoMes(filtro.fim == null ? new Date() : filtro.fim); List<Receita> resultado = new ArrayList<Receita>();
        for (Receita receita : origens) { if (!combina(receita, filtro)) continue; if (ehFixa(receita)) resultado.addAll(ocorrencias(receita, inicio, fim)); else if (dentroDoPeriodo(receita.getDataRecebimento(), inicio, fim)) resultado.add(receita); } return resultado;
    }
    private List<Receita> projetarAteAtual(List<Receita> origens) { List<Receita> resultado = new ArrayList<Receita>(); Calendar atual = calendarioDoMes(new Date()); for (Receita receita : origens) { if (ehFixa(receita)) resultado.addAll(ocorrencias(receita, calendarioDoMes(receita.getDataRecebimento()), atual)); else resultado.add(receita); } return resultado; }
    private List<Receita> filtrarBasicas(List<Receita> receitas, FiltroReceita filtro) { List<Receita> resultado = new ArrayList<Receita>(); for (Receita receita : receitas) if (combina(receita, filtro)) resultado.add(receita); return resultado; }
    private List<Receita> ocorrencias(Receita origem, Calendar inicio, Calendar fim) {
        List<Receita> resultado = new ArrayList<Receita>(); if (origem.getDataRecebimento() == null) return resultado; Calendar cursor = calendarioDoMes(origem.getDataRecebimento()); if (cursor.before(inicio)) cursor = (Calendar) inicio.clone(); Calendar limite = calendarioDoMes(new Date()); if (fim.after(limite)) fim = limite;
        while (!cursor.after(fim)) { resultado.add(copiarParaMes(origem, cursor)); cursor.add(Calendar.MONTH, 1); } return resultado;
    }
    private Receita copiarParaMes(Receita origem, Calendar referencia) {
        Receita ocorrencia = new Receita(); ocorrencia.setId(origem.getId()); ocorrencia.setNome(origem.getNome()); ocorrencia.setDescricao(origem.getDescricao()); ocorrencia.setValor(origem.getValor()); ocorrencia.setTipo(origem.getTipo()); ocorrencia.setAtivo(origem.isAtivo());
        Calendar data = (Calendar) referencia.clone(), origemData = Calendar.getInstance(); origemData.setTime(origem.getDataRecebimento()); data.set(Calendar.DAY_OF_MONTH, Math.min(origemData.get(Calendar.DAY_OF_MONTH), data.getActualMaximum(Calendar.DAY_OF_MONTH)));
        ocorrencia.setDataRecebimento(data.getTime()); ocorrencia.setOrigemId(origem.getId()); ocorrencia.setMesReferencia(referencia.get(Calendar.MONTH) + 1); ocorrencia.setAnoReferencia(referencia.get(Calendar.YEAR)); return ocorrencia;
    }
    private boolean ehFixa(Receita receita) { return "Fixa".equalsIgnoreCase(receita.getTipo()); }
    private boolean combina(Receita receita, FiltroReceita filtro) { if (filtro == null) return true; if (filtro.nome != null && !filtro.nome.trim().isEmpty() && (receita.getNome() == null || !receita.getNome().toLowerCase().contains(filtro.nome.toLowerCase()))) return false; return filtro.tipo == null || filtro.tipo.isEmpty() || filtro.tipo.equals(receita.getTipo()); }
    private boolean dentroDoPeriodo(Date data, Calendar inicio, Calendar fim) { if (data == null) return false; Calendar c = calendarioDoMes(data); return !c.before(inicio) && !c.after(fim); }
    private Calendar calendarioDoMes(Date data) { Calendar c = Calendar.getInstance(); c.setTime(data == null ? new Date() : data); c.set(Calendar.DAY_OF_MONTH, 1); c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); return c; }
    private void salvarEntidade(Receita receita) { if (receita.getId() == null) HibernateUtil.getSession().persist(receita); else HibernateUtil.getSession().merge(receita); }
}
