package servicos;

import entidades.Despesa;
import entidades.Receita;
import java.util.Calendar;
import java.util.List;
import model.FiltroDespesa;
import model.FiltroReceita;
import model.ResumoFinanceiro;
import model.StatusDespesa;

public class FinanceiroService {

    private final ReceitaService receitas = new ReceitaService();
    private final DespesaService despesas = new DespesaService();

    public ResumoFinanceiro calcularResumo() {
        AutorizacaoService.exigirFinanceiro();
        Calendar i = Calendar.getInstance();
        i.set(Calendar.DAY_OF_MONTH, 1);
        i.set(Calendar.HOUR_OF_DAY, 0);
        i.set(Calendar.MINUTE, 0);
        i.set(Calendar.SECOND, 0);
        i.set(Calendar.MILLISECOND, 0);
        Calendar f = (Calendar) i.clone();
        f.add(Calendar.MONTH, 1);
        f.add(Calendar.MILLISECOND, -1);
        FiltroReceita fr = new FiltroReceita();
        fr.inicio = i.getTime();
        fr.fim = f.getTime();
        FiltroDespesa fd = new FiltroDespesa();
        fd.inicio = i.getTime();
        fd.fim = f.getTime();
        ResumoFinanceiro r = new ResumoFinanceiro();
        List<Receita> lr = receitas.listar(fr);
        List<Despesa> ld = despesas.listar(fd);
        for (Receita x : lr) {
            r.setReceitas(r.getReceitas() + zero(x.getValor()));
        }
        for (Despesa x : ld) {
            double v = zero(x.getValor());
            r.setDespesas(r.getDespesas() + v);
            StatusDespesa s = despesas.calcularStatus(x);
            if (s == StatusDespesa.PAGO) {
                r.setPagas(r.getPagas() + v);
            } else if (s == StatusDespesa.ATRASADA) {
                r.setAtrasadas(r.getAtrasadas() + v);
            } else {
                r.setPendentes(r.getPendentes() + v);
            }
        }
        r.setSaldo(r.getReceitas() - r.getDespesas());
        return r;
    }

    private double zero(Double valor) {
        return valor == null ? 0 : valor;
    }
}
