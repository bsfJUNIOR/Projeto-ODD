package servicos;

import entidades.Despesa;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import model.FiltroDespesa;
import model.StatusDespesa;
import persistencia.HibernateUtil;

public class DespesaService {

    private final HistoricoService historicoService = new HistoricoService();

    public List<Despesa> listar() {
        AutorizacaoService.exigirFinanceiro();
        try {
            return listarAtivas();
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public List<Despesa> listar(FiltroDespesa filtro) {
        AutorizacaoService.exigirFinanceiro();
        try {
            return filtrarEProjetar(listarAtivas(), filtro);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public List<Despesa> listarComRecorrenciasAteAtual() {
        AutorizacaoService.exigirFinanceiro();
        try {
            return projetarAteAtual(listarAtivas());
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public StatusDespesa calcularStatus(Despesa d) {
        if ("PAGO".equalsIgnoreCase(d.getStatus()) || "Pago".equalsIgnoreCase(d.getStatus())) {
            return StatusDespesa.PAGO;
        }
        if (d.getDataVencimento() == null) {
            return StatusDespesa.PENDENTE;
        }
        Calendar hoje = Calendar.getInstance(), venc = Calendar.getInstance();
        zerarHora(hoje);
        venc.setTime(d.getDataVencimento());
        zerarHora(venc);
        if (venc.before(hoje)) {
            return StatusDespesa.ATRASADA;
        }
        Calendar limite = (Calendar) hoje.clone();
        limite.add(Calendar.DAY_OF_YEAR, 7);
        return !venc.after(limite) ? StatusDespesa.PROXIMA_DO_VENCIMENTO : StatusDespesa.PENDENTE;
    }

    private void zerarHora(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    public List<Despesa> listarParaVencimentos() {
        AutorizacaoService.exigirFinanceiro();
        try {
            List<Despesa> lista = listarAtivas();
            Collections.sort(lista, (Despesa a, Despesa b) -> {
                Date da = a.getDataVencimento(), db = b.getDataVencimento();
                if (da == null) {
                    return 1;
                }
                if (db == null) {
                    return -1;
                }
                return da.compareTo(db);
            });
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
            if (novo) {
                despesa.setAtivo(true);
            }
            if (despesa.getStatus() == null || "Selecionar".equals(despesa.getStatus())) {
                despesa.setStatus("PENDENTE");
            }
            salvarEntidade(despesa);
            historicoService.registrarNaTransacao(novo ? "CADASTRO" : "EDICAO", (novo ? "cadastrou a despesa " : "editou a despesa ") + despesa.getDespesa() + ".");
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

    @SuppressWarnings("unchecked")
    private List<Despesa> listarAtivas() {
        return HibernateUtil.getSession().createQuery("FROM Despesa d WHERE (d.ativo = true OR d.ativo IS NULL)").list();
    }

    private List<Despesa> filtrarEProjetar(List<Despesa> origens, FiltroDespesa filtro) {
        if (filtro == null || (filtro.inicio == null && filtro.fim == null)) {
            return filtrarBasicas(origens, filtro);
        }
        Calendar inicio = mes(filtro.inicio == null ? new Date(0) : filtro.inicio), fim = mes(filtro.fim == null ? new Date() : filtro.fim);
        List<Despesa> resultado = new ArrayList<>();
        for (Despesa despesa : origens) {
            if (!combina(despesa, filtro)) {
                continue;
            }
            if (ehFixa(despesa)) {
                resultado.addAll(ocorrencias(despesa, inicio, fim));
            } else if (dentro(despesa.getDataVencimento(), inicio, fim)) {
                resultado.add(despesa);
            }
        }
        return filtrarStatus(resultado, filtro);
    }

    private List<Despesa> projetarAteAtual(List<Despesa> origens) {
        List<Despesa> resultado = new ArrayList<>();
        Calendar atual = mes(new Date());
        for (Despesa despesa : origens) {
            if (ehFixa(despesa)) {
                resultado.addAll(ocorrencias(despesa, mes(despesa.getDataVencimento()), atual));
            } else {
                resultado.add(despesa);
            }
        }
        return resultado;
    }

    private List<Despesa> filtrarBasicas(List<Despesa> despesas, FiltroDespesa filtro) {
        List<Despesa> resultado = new ArrayList<>();
        for (Despesa despesa : despesas) {
            if (combina(despesa, filtro)) {
                resultado.add(despesa);
            }
        }
        return filtrarStatus(resultado, filtro);
    }

    private List<Despesa> filtrarStatus(List<Despesa> despesas, FiltroDespesa filtro) {
        if (filtro != null && filtro.status != null) {
            java.util.Iterator<Despesa> it = despesas.iterator();
            while (it.hasNext()) {
                if (calcularStatus(it.next()) != filtro.status) {
                    it.remove();
                }
            }
        }
        return despesas;
    }

    private List<Despesa> ocorrencias(Despesa origem, Calendar inicio, Calendar fim) {
        List<Despesa> resultado = new ArrayList<>();
        if (origem.getDataVencimento() == null) {
            return resultado;
        }
        Calendar cursor = mes(origem.getDataVencimento());
        if (cursor.before(inicio)) {
            cursor = (Calendar) inicio.clone();
        }
        Calendar limite = mes(new Date());
        if (fim.after(limite)) {
            fim = limite;
        }
        while (!cursor.after(fim)) {
            resultado.add(copiarParaMes(origem, cursor));
            cursor.add(Calendar.MONTH, 1);
        }
        return resultado;
    }

    private Despesa copiarParaMes(Despesa origem, Calendar referencia) {
        Despesa ocorrencia = new Despesa();
        ocorrencia.setId(origem.getId());
        ocorrencia.setDespesa(origem.getDespesa());
        ocorrencia.setDescricao(origem.getDescricao());
        ocorrencia.setTipo(origem.getTipo());
        ocorrencia.setValor(origem.getValor());
        ocorrencia.setAtivo(origem.isAtivo());
        Calendar data = (Calendar) referencia.clone(), origemData = Calendar.getInstance();
        origemData.setTime(origem.getDataVencimento());
        data.set(Calendar.DAY_OF_MONTH, Math.min(origemData.get(Calendar.DAY_OF_MONTH), data.getActualMaximum(Calendar.DAY_OF_MONTH)));
        ocorrencia.setDataVencimento(data.getTime());
        ocorrencia.setStatus(mes(origem.getDataVencimento()).equals(mes(referencia.getTime())) ? origem.getStatus() : "PENDENTE");
        ocorrencia.setOrigemId(origem.getId());
        ocorrencia.setMesReferencia(referencia.get(Calendar.MONTH) + 1);
        ocorrencia.setAnoReferencia(referencia.get(Calendar.YEAR));
        return ocorrencia;
    }

    private boolean ehFixa(Despesa despesa) {
        return "Fixa".equalsIgnoreCase(despesa.getTipo());
    }

    private boolean combina(Despesa despesa, FiltroDespesa filtro) {
        if (filtro == null) {
            return true;
        }
        if (filtro.nome != null && !filtro.nome.trim().isEmpty() && (despesa.getDespesa() == null || !despesa.getDespesa().toLowerCase().contains(filtro.nome.toLowerCase()))) {
            return false;
        }
        return filtro.tipo == null || filtro.tipo.isEmpty() || filtro.tipo.equals(despesa.getTipo());
    }

    private boolean dentro(Date data, Calendar inicio, Calendar fim) {
        if (data == null) {
            return false;
        }
        Calendar c = mes(data);
        return !c.before(inicio) && !c.after(fim);
    }

    private Calendar mes(Date data) {
        Calendar c = Calendar.getInstance();
        c.setTime(data == null ? new Date() : data);
        c.set(Calendar.DAY_OF_MONTH, 1);
        zerarHora(c);
        return c;
    }

    private void salvarEntidade(Despesa despesa) {
        if (despesa.getId() == null) {
            HibernateUtil.getSession().persist(despesa);
        } else {
            HibernateUtil.getSession().merge(despesa);
        }
    }
}
