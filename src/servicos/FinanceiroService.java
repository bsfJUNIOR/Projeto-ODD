package servicos;
import entidades.Despesa; import entidades.Receita; import java.util.List; import model.ResumoFinanceiro; import model.StatusDespesa;
/** Centraliza os totais exibidos pela tela principal. */
public class FinanceiroService {
 private final ReceitaService receitas=new ReceitaService(); private final DespesaService despesas=new DespesaService();
 public ResumoFinanceiro calcularResumo(){ AutorizacaoService.exigirFinanceiro(); ResumoFinanceiro r=new ResumoFinanceiro(); List<Receita> lr=receitas.listar(); List<Despesa> ld=despesas.listar(); for(Receita x:lr)r.setReceitas(r.getReceitas()+zero(x.getValor())); for(Despesa x:ld){double v=zero(x.getValor());r.setDespesas(r.getDespesas()+v);StatusDespesa s=despesas.calcularStatus(x);if(s==StatusDespesa.PAGO)r.setPagas(r.getPagas()+v);else if(s==StatusDespesa.ATRASADA)r.setAtrasadas(r.getAtrasadas()+v);else r.setPendentes(r.getPendentes()+v);} r.setSaldo(r.getReceitas()-r.getDespesas());return r; }
 private double zero(Double valor){return valor==null?0:valor;}
}
